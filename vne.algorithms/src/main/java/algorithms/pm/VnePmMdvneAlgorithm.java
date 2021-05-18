package algorithms.pm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import algorithms.AbstractAlgorithm;
import ilp.wrapper.IlpDelta;
import ilp.wrapper.IlpSolverException;
import ilp.wrapper.IncrementalIlpSolver;
import ilp.wrapper.Statistics;
import ilp.wrapper.impl.IncrementalGurobiSolver;
import model.Link;
import model.Node;
import model.SubstrateElement;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstratePath;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualElement;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualNode;
import model.VirtualServer;
import model.VirtualSwitch;
import patternmatching.IncrementalPatternMatcher;
import patternmatching.PatternMatchingDelta;
import patternmatching.PatternMatchingDelta.Match;
import patternmatching.emoflon.EmoflonPatternMatcherFactory;

/**
 * Implementation of the model-driven virtual network algorithm that uses pattern matching as a way
 * to reduce the search space of the ILP solver. Keep in mind that this particular implementation
 * only embeds one virtual network at a time.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithm extends AbstractAlgorithm {

  private class IlpDeltaGenerator {
    final IlpDelta delta = new IlpDelta();

    public void addLinkServerMatch(final Match match) {
      final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();
      final VirtualLink vLink = (VirtualLink) facade.getLinkById(match.getVirtual().getName());
      delta.addVariable(varName,
          getLinkToNodeEmbeddingCost(vLink, (SubstrateNode) match.getSubstrate()));
      delta.setVariableWeightForConstraint("vl" + match.getVirtual().getName(), 1, varName);
      delta.addLessOrEqualsConstraint("req" + varName, 0, new int[] {2, -1, -1},
          new String[] {varName, vLink.getSource().getName() + "_" + match.getSubstrate().getName(),
              vLink.getSource().getName() + "_" + match.getSubstrate().getName()});
      variablesToMatch.put(varName, match);
    }

    public void addLinkPathMatch(final Match match) {
      final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();

      final VirtualLink vLink = (VirtualLink) facade.getLinkById(match.getVirtual().getName());
      final SubstratePath sPath =
          (SubstratePath) facade.getPathById(match.getSubstrate().getName());

      delta.addVariable(varName, getLinkToPathEmbeddingCost(vLink, sPath));
      delta.setVariableWeightForConstraint("vl" + match.getVirtual().getName(), 1, varName);
      delta.addLessOrEqualsConstraint("req" + varName, 0, new int[] {2, -1, -1},
          new String[] {varName, vLink.getSource().getName() + "_" + sPath.getSource().getName(),
              vLink.getTarget().getName() + "_" + sPath.getTarget().getName()});
      forEachLink(sPath, l -> delta.setVariableWeightForConstraint("sl" + l.getName(),
          vLink.getBandwidth(), varName));
      variablesToMatch.put(varName, match);
    }

    public void addServerMatch(final Match match) {
      final VirtualServer vServer =
          (VirtualServer) facade.getServerById(match.getVirtual().getName());
      final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();
      delta.addVariable(varName,
          getServerEmbeddingCost(vServer, (SubstrateServer) match.getSubstrate()));
      delta.setVariableWeightForConstraint("vs" + match.getVirtual().getName(), 1, varName);

      delta.setVariableWeightForConstraint("cpu" + match.getSubstrate().getName(), vServer.getCpu(),
          varName);
      delta.setVariableWeightForConstraint("mem" + match.getSubstrate().getName(),
          vServer.getMemory(), varName);
      delta.setVariableWeightForConstraint("sto" + match.getSubstrate().getName(),
          vServer.getStorage(), varName);
      variablesToMatch.put(varName, match);
    }

    public void addServerSwitchMatch(final Match match) {
      final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();
      // TODO: This should be changed:
      delta.addVariable(varName, Integer.MAX_VALUE);
      delta.setVariableWeightForConstraint("vs" + match.getVirtual().getName(), 1, varName);
      variablesToMatch.put(varName, match);
    }

    public void addSwitchMatch(final Match match) {
      final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();
      delta.addVariable(varName, getSwitchEmbeddingCost((VirtualNode) match.getVirtual(),
          (SubstrateNode) match.getSubstrate()));
      delta.setVariableWeightForConstraint("vw" + match.getVirtual().getName(), 1, varName);
      variablesToMatch.put(varName, match);
    }

    public void addNewSubstrateServer(final SubstrateServer server) {
      // TODO: Residual resources here?
      delta.addLessOrEqualsConstraint("cpu" + server.getName(), server.getCpu());
      delta.addLessOrEqualsConstraint("mem" + server.getName(), server.getMemory());
      delta.addLessOrEqualsConstraint("sto" + server.getName(), server.getStorage());
    }

    public void addNewSubstrateLink(final SubstrateLink link) {
      // TODO: Residual resources here?
      delta.addLessOrEqualsConstraint("sl" + link.getName(), link.getBandwidth());
    }

    public void addNewVirtualServer(final VirtualServer server) {
      delta.addEqualsConstraint("vs" + server.getName(), 1);
      delta.setVariableWeightForConstraint("vs" + server.getName(), 1,
          "rej" + server.getNetwork().getName());
    }

    public void addNewVirtualSwitch(final VirtualSwitch sw) {
      delta.addEqualsConstraint("vw" + sw.getName(), 1);
      delta.setVariableWeightForConstraint("vw" + sw.getName(), 1,
          "rej" + sw.getNetwork().getName());
    }

    public void addNewVirtualLink(final VirtualLink link) {
      delta.addEqualsConstraint("vl" + link.getName(), 1);
      delta.setVariableWeightForConstraint("vl" + link.getName(), 1,
          "rej" + link.getNetwork().getName());
    }

    public void addNewNetworkMatch(final Match match) {
      delta.addVariable("rej" + match.getVirtual().getName(),
          getNetworkRejectionCost(match.getVirtual().getName()));
      variablesToMatch.put("rej" + match.getVirtual().getName(), match);
    }

    public void apply() {
      delta.apply(ilpSolver);
    }

  }

  // private static VnePmMdvneAlgorithm instance;

  private IncrementalPatternMatcher patternMatcher;
  private IncrementalIlpSolver ilpSolver;
  private final Map<String, Match> variablesToMatch = new HashMap<>();

  private Set<String> previousMappings = new HashSet<>();
  private Set<String> currentMappings = new HashSet<>();

  public VnePmMdvneAlgorithm(final SubstrateNetwork sNet, final VirtualNetwork vNet) {
    super(sNet, vNet);
    // TODO Auto-generated constructor stub
  }

  public double getSwitchEmbeddingCost(final VirtualNode virtual, final SubstrateNode substrate) {
    if (substrate instanceof SubstrateSwitch) {
      return 1;
    } else if (substrate instanceof SubstrateServer) {
      return 2;
    }
    throw new IllegalArgumentException();
  }

  public double getServerEmbeddingCost(final VirtualServer vServer, final SubstrateServer sServer) {
    return 1;
  }

  public void forEachLink(final SubstratePath sPath, final Consumer<? super Link> operation) {
    sPath.getLinks().stream().forEach(operation);
  }

  public double getLinkToPathEmbeddingCost(final VirtualLink vLink, final SubstratePath sPath) {
    if (sPath.getHops() == 1) {
      return 2;
    } else {
      return Math.pow(4, sPath.getHops());
    }
  }

  public double getLinkToNodeEmbeddingCost(final VirtualLink vLink, final SubstrateNode substrate) {
    return 1;
  }

  private double getNetworkRejectionCost(final String network) {
    return 1_000_000.0;
  }

  public void dispose() {
    // if (instance == null) {
    // return;
    // }
    if (this.ilpSolver != null) {
      this.ilpSolver.dispose();
    }
    if (this.patternMatcher != null) {
      this.patternMatcher.dispose();
    }
    // instance = null;
  }

  @Override
  public boolean execute() {
    init();
    final PatternMatchingDelta delta = patternMatcher.run();

    final IlpDeltaGenerator gen = new IlpDeltaGenerator();

    // add new elements
    // delta.getNewSubstrateServers().forEach(gen::addNewSubstrateServer);
    // delta.getNewSubstrateLinks().forEach(gen::addNewSubstrateLink);
    // delta.getNewVirtualServers().forEach(gen::addNewVirtualServer);
    // delta.getNewVirtualSwitches().forEach(gen::addNewVirtualSwitch);
    // delta.getNewVirtualLinks().forEach(gen::addNewVirtualLink);
    addElementsToSolver(gen);

    // add new matches
    delta.getNewNetworkMatches().forEach(gen::addNewNetworkMatch);
    delta.getNewServerMatchPositives().forEach(gen::addServerMatch);
    delta.getNewServerMatchNegatives().forEach(gen::addServerMatch);
    // TODO: This has to be changed:
    delta.getNewServerMatchSwitchNegatives().forEach(gen::addServerSwitchMatch);
    delta.getNewSwitchMatchPositives().forEach(gen::addSwitchMatch);
    delta.getNewLinkPathMatchPositives().forEach(gen::addLinkPathMatch);
    delta.getNewLinkPathMatchNegatives().forEach(gen::addLinkPathMatch);
    delta.getNewLinkServerMatchPositives().forEach(gen::addLinkServerMatch);

    // apply delta in ILP generator
    gen.apply();

    final Statistics solve = ilpSolver.solve();
    if (isFeasible(solve)) {
      updateMappingsAndEmbed(ilpSolver.getMappings());
      // Lock all variables (no migration implemented, yet)
      ilpSolver.lockVariables(e -> true);
    } else {
      throw new IlpSolverException();
    }

    return true;
  }

  private void addElementsToSolver(final IlpDeltaGenerator gen) {
    // Substrate network
    for (final Node n : sNet.getNodes()) {
      if (n instanceof SubstrateServer) {
        gen.addNewSubstrateServer((SubstrateServer) n);
      } else if (n instanceof SubstrateSwitch) {
        // Nothing to do here
      }
    }

    for (final Link l : sNet.getLinks()) {
      if (l instanceof SubstrateLink) {
        gen.addNewSubstrateLink((SubstrateLink) l);
      }
    }

    // Virtual network
    for (final Node n : vNet.getNodes()) {
      if (n instanceof VirtualServer) {
        gen.addNewVirtualServer((VirtualServer) n);
      } else if (n instanceof VirtualSwitch) {
        gen.addNewVirtualSwitch((VirtualSwitch) n);
      }
    }

    for (final Link l : vNet.getLinks()) {
      if (l instanceof VirtualLink) {
        gen.addNewVirtualLink((VirtualLink) l);
      }
    }
  }

  private void updateMappingsAndEmbed(final Map<String, Boolean> mappings) {
    previousMappings = currentMappings;
    currentMappings = new HashSet<>();

    final List<String> newMappings = new LinkedList<>();
    for (final Entry<String, Boolean> entry : mappings.entrySet()) {

      // TODO: Remove me:
      // System.out.println(entry.getKey());

      if (entry.getValue()) {
        final String key = entry.getKey();
        currentMappings.add(key);
        if (!previousMappings.contains(key)) {
          newMappings.add(key);
        }
      }
    }

    for (final String s : newMappings) {
      final Match m = variablesToMatch.get(s);

      final VirtualElement virtualElement = (VirtualElement) m.getVirtual();
      final SubstrateElement substrateElement = (SubstrateElement) m.getSubstrate();

      // Network -> Network
      if (virtualElement instanceof VirtualNetwork) {
        facade.embedNetworkToNetwork(substrateElement.getName(), virtualElement.getName());
      }

      // Server -> Server
      if (virtualElement instanceof VirtualServer) {
        facade.embedServerToServer(substrateElement.getName(), virtualElement.getName());
      }

      // Switch -> Node
      if (virtualElement instanceof VirtualSwitch) {
        facade.embedSwitchToNode(substrateElement.getName(), virtualElement.getName());
      }

      // Link -> Path || Link -> Server
      if (virtualElement instanceof VirtualLink) {
        if (substrateElement instanceof SubstratePath) {
          facade.embedLinkToPath(substrateElement.getName(), virtualElement.getName());
        } else if (substrateElement instanceof SubstrateServer) {
          facade.embedLinkToServer(substrateElement.getName(), virtualElement.getName());
        }
      }
    }
  }

  public void init() {
    if (ilpSolver == null) {
      // TODO: Make configurable.
      ilpSolver = new IncrementalGurobiSolver(Integer.MAX_VALUE, 0);
    }

    if (patternMatcher == null) {
      patternMatcher = new EmoflonPatternMatcherFactory().create();
    }
  }

  /**
   * Returns true if the given statistics object was feasible.
   * 
   * @param ilpStatistics Statistics object to get information from.
   * @return True if given statistics object was feasible.
   */
  private static boolean isFeasible(final Statistics ilpStatistics) {
    if (ilpStatistics.getStatus() == ilp.wrapper.SolverStatus.INF_OR_UNBD
        || ilpStatistics.getStatus() == ilp.wrapper.SolverStatus.INFEASIBLE) {
      return false;
    }
    return true;
  }

}
