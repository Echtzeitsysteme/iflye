package algorithms.ilp;

import static org.cardygan.ilp.api.util.ExprDsl.eq;
import static org.cardygan.ilp.api.util.ExprDsl.leq;
import static org.cardygan.ilp.api.util.ExprDsl.mult;
import static org.cardygan.ilp.api.util.ExprDsl.param;
import static org.cardygan.ilp.api.util.ExprDsl.sum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.cardygan.ilp.api.Result;
import org.cardygan.ilp.api.Result.SolverStatus;
import org.cardygan.ilp.api.Result.Statistics;
import org.cardygan.ilp.api.model.ArithExpr;
import org.cardygan.ilp.api.model.BinaryVar;
import org.cardygan.ilp.api.model.Model;
import org.cardygan.ilp.api.model.Param;
import org.cardygan.ilp.api.solver.GurobiSolver;
import algorithms.AbstractAlgorithm;
import model.Link;
import model.Node;
import model.Path;
import model.SubstrateElement;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstratePath;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualNode;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Implementation of the ILP formulation of paper [1]. Keep in mind that this particular
 * implementation only embeds one virtual network at a time.
 * 
 * [1] Tomaszek S., Leblebici E., Wang L., Schürr A. (2018) Virtual Network Embedding: Reducing the
 * Search Space by Model Transformation Techniques. In: Rensink A., Sánchez Cuadrado J. (eds) Theory
 * and Practice of Model Transformation. ICMT 2018. Lecture Notes in Computer Science, vol 10888.
 * Springer, Cham
 *
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VneIlpPathAlgorithm extends AbstractAlgorithm {

  private class VneIlpPath {

    private static final String PREFIX = "Path_";

    private final String id;
    private final SubstrateNode sourceNode;
    private final SubstrateNode targetNode;
    private final int residualBandwidth;
    private final List<SubstrateLink> links = new LinkedList<>();
    private final Set<SubstrateNode> nodes = new HashSet<>();

    private SubstrateServer server;

    public VneIlpPath(final SubstrateServer server) {
      this.server = server;
      id = PREFIX + server.getName();
      sourceNode = server;
      targetNode = server;
      residualBandwidth = Integer.MAX_VALUE;
      nodes.add(server);
    }

    public VneIlpPath(final SubstrateLink link) {
      id = PREFIX + link.getName();
      sourceNode = (SubstrateNode) link.getSource();
      targetNode = (SubstrateNode) link.getTarget();
      residualBandwidth = link.getResidualBandwidth();
      links.add(link);
      nodes.add(sourceNode);
      nodes.add(targetNode);
    }

    public VneIlpPath(final SubstratePath path) {
      id = PREFIX + path.getName();
      sourceNode = (SubstrateNode) path.getSource();
      targetNode = (SubstrateNode) path.getTarget();
      residualBandwidth = path.getResidualBandwidth();
      // path.getHops(); // Why?
      for (final Link link : path.getLinks()) {
        links.add((SubstrateLink) link);
      }
      nodes.add(sourceNode);
      nodes.add(targetNode);

      for (final Node node : path.getNodes()) {
        nodes.add((SubstrateNode) node);
      }
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof VneIlpPath)) {
        return false;
      }
      final VneIlpPath other = (VneIlpPath) obj;
      if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
        return false;
      }
      return Objects.equals(id, other.id);
    }

    private VneIlpPathAlgorithm getEnclosingInstance() {
      return VneIlpPathAlgorithm.this;
    }

    // public int getHops() {
    // if (links.isEmpty()) {
    // return 0;
    // } else {
    // return links.size();
    // }
    // }

    public List<SubstrateLink> getLinks() {
      return links;
    }

    public List<SubstrateElement> getLinksOrServer() {
      if (links.isEmpty()) {
        return Arrays.asList(server);
      } else {
        final List<SubstrateElement> elems = new ArrayList<>();
        elems.addAll(links);
        return elems;
      }
    }

    // public Set<SubstrateNode> getNodeInformations() {
    // return new HashSet<>(nodes);
    // }

    public int getResidualBandwidth() {
      return residualBandwidth;
    }

    public SubstrateServer getServer() {
      return server;
    }

    public SubstrateNode getSourceNode() {
      return sourceNode;
    }

    public SubstrateNode getTargetNode() {
      return targetNode;
    }

  }

  // ILP variables
  // final IncrementalIlpSolver solver = new IncrementalGurobiSolver(-1, 0);
  // private int[][] nodeVariables;
  // private int[][] pathVariables;
  private Model model;
  private BinaryVar[][] nodeVariables;
  private BinaryVar[][] pathVariables;
  private AtomicLong idGen = new AtomicLong(0);

  private Result ilpResult;

  // Virtual network
  private final List<VirtualNode> virtualNodes = new LinkedList<>();
  private final List<VirtualLink> virtualLinks = new LinkedList<>();

  // Substrate network
  private final List<SubstrateNode> substrateNodes = new LinkedList<>();
  private final List<VneIlpPath> allSubstratePaths = new LinkedList<>();
  private final List<SubstrateLink> substrateLinks = new LinkedList<>();

  /*
   * Results
   */
  private final Map<VirtualNode, SubstrateNode> resultVirtualToSubstrateNodes = new HashMap<>();
  private final Map<VirtualLink, List<SubstrateElement>> resultVirtualToSubstrateLink =
      new HashMap<>();

  public VneIlpPathAlgorithm(final SubstrateNetwork sNet, final VirtualNetwork vNet) {
    super(sNet, vNet);
    // TODO!
  }

  @Override
  public boolean execute() {
    model = new Model();
    createNetworkInformation();
    createAllVariables();

    // Every virtual link is mapped to one substrate link (path) and that the start/target node
    // of the virtual link are also mapped to the start/target node of the substrate node.
    // Every virtual node must be mapped exactly to one substrate node
    addConstraintsEveryVirtualElementIsMappedExactlyOnce();

    createAllNodeConstraints();
    createAllLinkConstraints();

    createMinOveralCostsObjective();

    // TODO: Make solver configurable.
    ilpResult = model.solve(new GurobiSolver());

    if (isFeasible(ilpResult.getStatistics())) {
      // Node results
      for (int v = 0; v < nodeVariables.length; v++) {
        for (int s = 0; s < nodeVariables[v].length; s++) {
          if (ilpResult.getSolutions().getOrDefault(nodeVariables[v][s], -1.0) > 0.5) {
            // The value 0.5 is important because the ilp variables are internally represented as
            // double values and, therefore, rounding errors must be taken into account
            resultVirtualToSubstrateNodes.put(virtualNodes.get(v), substrateNodes.get(s));
          }
        }
      }

      // Link results
      for (int v = 0; v < pathVariables.length; v++) {
        for (int s = 0; s < pathVariables[v].length; s++) {
          if (ilpResult.getSolutions().getOrDefault(pathVariables[v][s], -1.0) > 0.5) {
            // The value 0.5 is important because the ILP variables are internally represented as
            // double values and, therefore, rounding errors must be taken into account
            resultVirtualToSubstrateLink.put(virtualLinks.get(v),
                allSubstratePaths.get(s).getLinksOrServer());
          }
        }
      }
    }

    createEmbeddings();

    return isFeasible(ilpResult.getStatistics());
  }

  private void createEmbeddings() {
    // Network
    facade.embedNetworkToNetwork(sNet.getName(), vNet.getName());

    // Nodes
    for (final VirtualNode vn : resultVirtualToSubstrateNodes.keySet()) {
      final SubstrateNode sn = resultVirtualToSubstrateNodes.get(vn);

      if (vn instanceof VirtualServer) {
        facade.embedServerToServer(sn.getName(), vn.getName());
      } else {
        facade.embedSwitchToNode(sn.getName(), vn.getName());
      }
    }

    // Links
    for (final VirtualLink vl : resultVirtualToSubstrateLink.keySet()) {
      final List<SubstrateElement> hosts = resultVirtualToSubstrateLink.get(vl);

      if (hosts.size() > 1) {
        // If size is larger than 1, there must be links/a path
        final List<SubstrateLink> links = new LinkedList<SubstrateLink>();
        for (final SubstrateElement e : hosts) {
          links.add((SubstrateLink) e);
        }

        final Path p = facade.getPathFromSourceToTarget(links.get(0).getSource(),
            links.get(links.size() - 1).getTarget());
        facade.embedLinkToPath(p.getName(), vl.getName());
      } else {
        facade.embedLinkToServer(hosts.get(0).getName(), vl.getName());
      }
    }
  }

  private void createMinOveralCostsObjective() {
    final List<ArithExpr> expr = new LinkedList<>();
    for (int v = 0; v < nodeVariables.length; v++) {
      for (int s = 0; s < nodeVariables[v].length; s++) {
        expr.add(mult(param(getNodeCost(virtualNodes.get(v), substrateNodes.get(s))),
            nodeVariables[v][s]));
      }
    }

    for (int v = 0; v < pathVariables.length; v++) {
      for (int s = 0; s < pathVariables[v].length; s++) {
        expr.add(mult(param(getLinkCost(virtualLinks.get(v), allSubstratePaths.get(s))),
            pathVariables[v][s]));
      }
    }

    model.newObjective(false, sum(expr));
  }

  private double getNodeCost(final VirtualNode virtualNode, final SubstrateNode substrateNode) {
    if (virtualNode instanceof VirtualSwitch) {
      if (substrateNode instanceof SubstrateSwitch) {
        return 1;
      } else if (substrateNode instanceof SubstrateServer) {
        return 2;
      } else {
        throw new UnsupportedOperationException();
      }
    } else if (virtualNode instanceof VirtualServer) {
      if (substrateNode instanceof SubstrateServer) {
        return 1;
      } else {
        return 0;
      }
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private double getLinkCost(final VirtualLink virtualLink, final VneIlpPath substratePath) {
    double cost = 0.0;
    if (substratePath.getServer() != null) {
      cost = 1;
    } else {
      final int hops = substratePath.getLinks().size();
      cost = (hops == 1) ? 2 : Math.pow(4, hops);
    }
    return cost;
  }

  private void createNetworkInformation() {
    // Virtual network
    for (final Node n : facade.getAllServersOfNetwork(vNet.getName())) {
      virtualNodes.add((VirtualNode) n);
    }

    for (final Node n : facade.getAllSwitchesOfNetwork(vNet.getName())) {
      virtualNodes.add((VirtualNode) n);
    }

    for (final Link l : facade.getAllLinksOfNetwork(vNet.getName())) {
      virtualLinks.add((VirtualLink) l);
    }

    // Substrate network
    for (final Node n : facade.getAllServersOfNetwork(sNet.getName())) {
      substrateNodes.add((SubstrateNode) n);
    }

    for (final Node n : facade.getAllSwitchesOfNetwork(sNet.getName())) {
      substrateNodes.add((SubstrateNode) n);

      if (n instanceof SubstrateServer) {
        allSubstratePaths.add(new VneIlpPath((SubstrateServer) n));
      }
    }

    for (final Link l : facade.getAllLinksOfNetwork(sNet.getName())) {
      allSubstratePaths.add(new VneIlpPath((SubstrateLink) l));
      substrateLinks.add((SubstrateLink) l);
    }

    for (final Path p : facade.getAllPathsOfNetwork(sNet.getName())) {
      allSubstratePaths.add(new VneIlpPath((SubstratePath) p));
    }
  }


  private void createAllVariables() {
    nodeVariables = new BinaryVar[virtualNodes.size()][substrateNodes.size()];
    pathVariables = new BinaryVar[virtualLinks.size()][allSubstratePaths.size()];

    for (int v = 0; v < nodeVariables.length; v++) {
      for (int s = 0; s < nodeVariables[v].length; s++) {
        nodeVariables[v][s] = model.newBinaryVar("v" + getNextId());
      }
    }

    for (int v = 0; v < pathVariables.length; v++) {
      for (int s = 0; s < pathVariables[v].length; s++) {
        pathVariables[v][s] = model.newBinaryVar("v" + getNextId());
      }
    }
  }

  private void createAllNodeConstraints() {
    // Every substrate node must be able to host the service type of the virtual node
    for (int v = 0; v < nodeVariables.length; v++) {
      for (int s = 0; s < nodeVariables[v].length; s++) {
        Param virParamServer;
        Param virParamSwitch;
        if (virtualNodes.get(v) instanceof VirtualServer) {
          virParamServer = param(1);
          virParamSwitch = param(0);
        } else {
          virParamServer = param(0);
          virParamSwitch = param(1);
        }

        Param subParamServer;
        Param subParamSwitch;
        if (substrateNodes.get(s) instanceof VirtualSwitch) {
          subParamServer = param(1);
          subParamSwitch = param(0);
        } else {
          subParamServer = param(0);
          subParamSwitch = param(1);
        }

        model.newConstraint(leq(mult(virParamServer, nodeVariables[v][s]), subParamServer));
        model.newConstraint(
            leq(mult(virParamSwitch, nodeVariables[v][s]), sum(subParamServer, subParamSwitch)));
      }
    }

    // The resources of the substrate node are not over booked by the demands of all virtual
    // nodes mapped to this substrate node.
    for (int s = 0; s < substrateNodes.size(); s++) {
      if (substrateNodes.get(s) instanceof SubstrateServer) {
        final List<ArithExpr> exprCpu = new ArrayList<>();
        final List<ArithExpr> exprMemory = new ArrayList<>();
        final List<ArithExpr> exprStorage = new ArrayList<>();
        final SubstrateServer substrateServer = (SubstrateServer) substrateNodes.get(s);
        for (int v = 0; v < virtualNodes.size(); v++) {
          if (virtualNodes.get(v) instanceof VirtualServer) {
            final VirtualServer virtualServer = (VirtualServer) virtualNodes.get(v);
            exprCpu.add(mult(param(virtualServer.getCpu()), nodeVariables[v][s]));
            exprMemory.add(mult(param(virtualServer.getMemory()), nodeVariables[v][s]));
            exprStorage.add(mult(param(virtualServer.getStorage()), nodeVariables[v][s]));
          }
        }
        model.newConstraint(substrateServer.getName() + "_CPU",
            leq(sum(exprCpu), param(substrateServer.getResidualCpu())));
        model.newConstraint(substrateServer.getName() + "_Memory",
            leq(sum(exprMemory), param(substrateServer.getResidualMemory())));
        model.newConstraint(substrateServer.getName() + "_Storage",
            leq(sum(exprStorage), param(substrateServer.getResidualStorage())));
      }
    }
  }

  private void createAllLinkConstraints() {
    // Constraints for source/target node
    for (int v = 0; v < virtualLinks.size(); v++) {
      for (int s = 0; s < allSubstratePaths.size(); s++) {

        final VirtualLink virtualLink = virtualLinks.get(v);
        final VneIlpPath subtratePath = allSubstratePaths.get(s);

        // Source to source constraint
        model.newConstraint(leq(pathVariables[v][s], getNodeVariable(
            virtualLink.getSource().getName(), subtratePath.getSourceNode().getName())));

        // Target to target constraint
        model.newConstraint(leq(pathVariables[v][s], getNodeVariable(
            virtualLink.getTarget().getName(), subtratePath.getTargetNode().getName())));
      }
    }

    // Add path constraints
    for (final SubstrateLink subLink : substrateLinks) {
      final List<ArithExpr> sumBandwidthExpr = new ArrayList<>();
      for (int s = 0; s < allSubstratePaths.size(); s++) {
        for (final SubstrateLink tempLink : allSubstratePaths.get(s).getLinks()) {
          if (tempLink.equals(subLink)) {
            for (int v = 0; v < virtualLinks.size(); v++) {
              sumBandwidthExpr
                  .add(mult(pathVariables[v][s], param(virtualLinks.get(v).getBandwidth())));
            }
          }
        }
      }
      model.newConstraint("virtualLink-SubstratePath-SubstrateLinks-Bandwidth",
          leq(sum(sumBandwidthExpr), param(subLink.getResidualBandwidth())));
    }

    // Bandwidth constraint
    for (int s = 0; s < allSubstratePaths.size(); s++) {
      final List<ArithExpr> exprBandwidth = new ArrayList<>();
      for (int v = 0; v < virtualLinks.size(); v++) {
        exprBandwidth.add(mult(param(virtualLinks.get(v).getBandwidth()), pathVariables[v][s]));
      }
      model.newConstraint(
          leq(sum(exprBandwidth), param(allSubstratePaths.get(s).getResidualBandwidth())));
    }
  }

  private void addConstraintsEveryVirtualElementIsMappedExactlyOnce() {
    final List<ArithExpr> expr = new LinkedList<ArithExpr>();

    for (final BinaryVar[] virtualVars : nodeVariables) {
      expr.clear();
      for (final BinaryVar subVar : virtualVars) {
        expr.add(mult(param(1), subVar));
      }
      model.newConstraint(eq(param(1), sum(expr)));
    }

    for (final BinaryVar[] virtualVars : pathVariables) {
      expr.clear();
      for (final BinaryVar subVar : virtualVars) {
        expr.add(mult(param(1), subVar));
      }
      model.newConstraint(eq(param(1), sum(expr)));
    }
  }

  private BinaryVar getNodeVariable(final String virtualNodeId, final String substrateNodeId) {
    int virtual = -1;
    int substrate = -1;
    for (int i = 0; i < virtualNodes.size(); i++) {
      if (virtualNodes.get(i).getName().equals(virtualNodeId)) {
        virtual = i;
      }
    }
    for (int i = 0; i < substrateNodes.size(); i++) {
      if (substrateNodes.get(i).getName().equals(substrateNodeId)) {
        substrate = i;
      }
    }
    if (virtual != -1 && substrate != -1) {
      return nodeVariables[virtual][substrate];
    }
    throw new IllegalArgumentException();
  }

  private String getNextId() {
    return String.valueOf(idGen.getAndIncrement());
  }

  private static boolean isFeasible(final Statistics ilpStatistics) {
    if (ilpStatistics.getStatus() == SolverStatus.INF_OR_UNBD
        || ilpStatistics.getStatus() == SolverStatus.INFEASIBLE) {
      return false;
    }
    return true;
  }

}
