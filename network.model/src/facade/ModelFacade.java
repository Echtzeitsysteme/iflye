package facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.moflon.core.utilities.eMoflonEMFUtil;
import com.google.common.collect.Lists;
import facade.config.ModelFacadeConfig;
import facade.pathgen.Dijkstra;
import facade.pathgen.IPathGen;
import facade.pathgen.Yen;
import model.Link;
import model.ModelFactory;
import model.Network;
import model.Node;
import model.Path;
import model.Root;
import model.Server;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstratePath;
import model.SubstrateServer;
import model.Switch;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Facade to access and manipulate the underlying model.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacade {

  /**
   * The singleton instance of this class.
   */
  private static ModelFacade instance;

  /**
   * Counter for generating new IDs.
   */
  private static AtomicInteger counter = new AtomicInteger();

  /**
   * Path to import and export models.
   */
  private static final String PERSISTANT_MODEL_PATH = "./model.xmi";

  /*
   * Collections for the path creation methods.
   */
  private final Set<Node> visitedNodes = new HashSet<Node>();
  private final List<SubstratePath> generatedMetaPaths = new LinkedList<SubstratePath>();
  private final Set<Link> linksUntilNode = new HashSet<Link>();
  private final Map<SubstrateNode, Set<SubstratePath>> pathSourceMap =
      new HashMap<SubstrateNode, Set<SubstratePath>>();

  /**
   * Private constructor to disable direct object instantiation.
   */
  private ModelFacade() {}

  /**
   * Returns the singleton instance of this class.
   * 
   * @return Singleton instance.
   */
  public static synchronized ModelFacade getInstance() {
    if (ModelFacade.instance == null) {
      ModelFacade.instance = new ModelFacade();
    }
    return ModelFacade.instance;
  }

  /**
   * Root (entry point of the model).
   */
  private Root root = ModelFactory.eINSTANCE.createRoot();

  private Map<String, Path> paths = new HashMap<String, Path>();
  private Map<String, Link> links = new HashMap<String, Link>();

  // TODO: Remove me later on.
  public void dummy() {
    System.out.println("=> Dummy method called.");
  }

  /**
   * Returns the root node.
   * 
   * @return Root node.
   */
  public Root getRoot() {
    return root;
  }

  /**
   * Returns a collection of all networks from the model.
   * 
   * @return Collection of all networks from the model.
   */
  public Collection<Network> getAllNetworks() {
    return root.getNetworks();
  }

  /**
   * Returns a list of nodes with all servers of a given network ID.
   * 
   * @param networkId Network ID.
   * @return List of nodes with all servers of the given network ID.
   */
  public List<Node> getAllServersOfNetwork(final String networkId) {
    checkStringValid(networkId);

    return getNetworkById(networkId).getNodes().stream().filter(n -> n instanceof Server)
        .collect(Collectors.toList());
  }

  /**
   * Returns a list of nodes with all switches of a given network ID.
   * 
   * @param networkId Network ID.
   * @return List of nodes with all switches of the given network ID.
   */
  public List<Node> getAllSwitchesOfNetwork(final String networkId) {
    checkStringValid(networkId);

    return getNetworkById(networkId).getNodes().stream().filter(n -> n instanceof Switch)
        .collect(Collectors.toList());
  }

  /**
   * Returns a list of all links of a given network ID.
   * 
   * @param networkId Network ID.
   * @return List of all links of the given network ID.
   */
  public List<Link> getAllLinksOfNetwork(final String networkId) {
    checkStringValid(networkId);

    return getNetworkById(networkId).getLinks();
  }

  /**
   * Returns a list of all paths of a given network ID.
   * 
   * @param networkId Network ID.
   * @return List of all paths of the given network ID.
   */
  public List<Path> getAllPathsOfNetwork(final String networkId) {
    checkStringValid(networkId);

    return getNetworkById(networkId).getPaths();
  }

  /**
   * Returns a network object by its ID.
   * 
   * @param id ID to return network object for.
   * @return Network object for given ID.
   */
  public Network getNetworkById(final String id) {
    checkStringValid(id);

    return root.getNetworks().stream().filter(n -> n.getName().equals(id))
        .collect(Collectors.toList()).get(0);
  }

  /**
   * Returns true if a network for a given ID exists.
   * 
   * @param id ID to check network existence for.
   * @return True if network does exist in model.
   */
  public boolean networkExists(final String id) {
    checkStringValid(id);

    return getAllNetworks().stream().filter(n -> n.getName().equals(id))
        .collect(Collectors.toList()).size() != 0;
  }

  /**
   * Returns a server object for a given ID.
   * 
   * @param id ID to return server object for.
   * @return Server object for given ID.
   */
  public Server getServerById(final String id) {
    checkStringValid(id);
    return (Server) getNodeById(id);
  }

  /**
   * Returns a switch object for a given ID.
   * 
   * @param id ID to return switch object for.
   * @return Switch object for given ID.
   */
  public Switch getSwitchById(final String id) {
    checkStringValid(id);
    return (Switch) getNodeById(id);
  }

  /**
   * Returns a node object for a given ID.
   * 
   * @param id ID to return node object for.
   * @return Node object for given ID.
   */
  private Node getNodeById(final String id) {
    checkStringValid(id);

    List<Network> nets = root.getNetworks();
    List<Node> nodes = new ArrayList<Node>();
    nets.stream().forEach(net -> {
      net.getNodes().stream().filter(n -> n instanceof Node).filter(n -> n.getName().equals(id))
          .forEach(n -> nodes.add(n));
    });
    return nodes.get(0);
  }

  /**
   * Returns a link object for a given ID.
   * 
   * @param id ID to return link object for.
   * @return Link object for given ID.
   */
  public Link getLinkById(final String id) {
    checkStringValid(id);

    // List<Network> nets = root.getNetworks();
    // List<Link> links = new ArrayList<Link>();
    // nets.stream().forEach(net -> {
    // net.getLinks().stream().filter(l -> l.getName().equals(id)).forEach(l -> links.add(l));
    // });
    // return links.get(0);
    return links.get(id);
  }

  /**
   * Returns a path object for a given ID.
   * 
   * @param id ID to return path object for.
   * @return Path object for given ID.
   */
  public Path getPathById(final String id) {
    checkStringValid(id);

    // List<Network> nets = root.getNetworks();
    // List<Path> paths = new ArrayList<Path>();
    // nets.stream().forEach(net -> {
    // net.getPaths().stream().filter(p -> p.getName().equals(id)).forEach(p -> paths.add(p));
    // });
    //
    // return paths.get(0);
    return paths.get(id);
  }

  /**
   * Creates and adds a new (substrate or virtual) network object with given ID to the root node of
   * the model.
   * 
   * @param id ID of the new network to create.
   * @param isVirtual True if new network should be virtual.
   * @return True if creation was successful.
   */
  public boolean addNetworkToRoot(final String id, final boolean isVirtual) {
    checkStringValid(id);

    if (networkExists(id)) {
      throw new IllegalArgumentException("A network with id " + id + " already exists!");
    }

    Network net;
    if (isVirtual) {
      net = ModelFactory.eINSTANCE.createVirtualNetwork();
    } else {
      net = ModelFactory.eINSTANCE.createSubstrateNetwork();
    }

    net.setName(id);
    net.setRoot(root);
    return root.getNetworks().add(net);
  }

  /**
   * Creates and adds a new server to the network model.
   * 
   * @param id ID of the new server to create.
   * @param networkId Network ID to add the new server to.
   * @param cpu CPU amount.
   * @param memory Memory amount.
   * @param storage Storage amount.
   * @param depth Depth inside the network.
   * @return True if creation was successful.
   */
  public boolean addServerToNetwork(final String id, final String networkId, final int cpu,
      final int memory, final int storage, final int depth) {
    checkStringValid(new String[] {id, networkId});
    checkIntValid(new int[] {cpu, memory, storage, depth});

    if (doesNodeIdExist(id, networkId)) {
      throw new IllegalArgumentException("A node with id " + id + " already exists!");
    }

    final Network net = getNetworkById(networkId);
    Server server;

    if (net instanceof VirtualNetwork) {
      server = ModelFactory.eINSTANCE.createVirtualServer();
    } else {
      server = ModelFactory.eINSTANCE.createSubstrateServer();
    }
    server.setName(id);
    server.setNetwork(net);
    server.setCpu(cpu);
    server.setMemory(memory);
    server.setStorage(storage);
    server.setDepth(depth);

    // Add residual values to server if it is a substrate server
    if (server instanceof SubstrateServer) {
      SubstrateServer subServer = (SubstrateServer) server;
      subServer.setResidualCpu(cpu);
      subServer.setResidualMemory(memory);
      subServer.setResidualStorage(storage);
    }

    return net.getNodes().add(server);
  }

  /**
   * Creates and adds a new switch to the network model.
   * 
   * @param id ID of the new switch to create.
   * @param networkId Network ID to add the new server to.
   * @param depth Depth inside the network.
   * @return True if creation was successful.
   */
  public boolean addSwitchToNetwork(final String id, final String networkId, final int depth) {
    checkStringValid(new String[] {id, networkId});
    checkIntValid(depth);

    if (doesNodeIdExist(id, networkId)) {
      throw new IllegalArgumentException("A node with id " + id + " already exists!");
    }

    final Network net = getNetworkById(networkId);
    Switch sw;

    if (net instanceof VirtualNetwork) {
      sw = ModelFactory.eINSTANCE.createVirtualSwitch();
    } else {
      sw = ModelFactory.eINSTANCE.createSubstrateSwitch();
    }
    sw.setName(id);
    sw.setNetwork(net);
    sw.setDepth(depth);

    return net.getNodes().add(sw);
  }

  /**
   * Creates and adds a new link to a network.
   * 
   * @param id ID of the new link to create.
   * @param networkId Network ID to add link to.
   * @param bandwidth Bandwidth amount.
   * @param sourceId ID of the source node.
   * @param targetId ID of the target node.
   * @return
   */
  public boolean addLinkToNetwork(final String id, final String networkId, final int bandwidth,
      final String sourceId, final String targetId) {
    checkStringValid(new String[] {id, networkId, sourceId, targetId});
    checkIntValid(bandwidth);

    if (doesLinkIdExist(id, networkId)) {
      throw new IllegalArgumentException("A link with id " + id + " already exists!");
    }

    if (!doesNodeIdExist(sourceId, networkId) || !doesNodeIdExist(targetId, networkId)) {
      throw new IllegalArgumentException("A node with given id does not exist!");
    }

    final Network net = getNetworkById(networkId);
    Link link;
    if (net instanceof VirtualNetwork) {
      link = ModelFactory.eINSTANCE.createVirtualLink();
    } else {
      link = ModelFactory.eINSTANCE.createSubstrateLink();
    }
    link.setName(id);
    link.setNetwork(net);
    link.setBandwidth(bandwidth);
    link.setSource(getNodeById(sourceId));
    link.setTarget(getNodeById(targetId));

    // Add residual values to link if it is a substrate link
    if (link instanceof SubstrateLink) {
      SubstrateLink subLink = (SubstrateLink) link;
      subLink.setResidualBandwidth(bandwidth);
    }

    // TODO: Check successful adding to model before adding it to look up data structure
    links.put(id, link);
    return net.getLinks().add(link);
  }

  /**
   * This method creates all necessary paths *after* all other components are added to the network.
   * 
   * Assumptions: Every server of the given network is only connected to one switch.
   * 
   * @param networkdId Network ID to add paths to.
   */
  public void createAllPathsForNetwork(final String networkdId) {
    checkStringValid(networkdId);
    final Network net = getNetworkById(networkdId);

    if (net instanceof VirtualNetwork) {
      throw new UnsupportedOperationException(
          "Given network ID is virtual," + " which is not supported!");
    }

    final SubstrateNetwork snet = (SubstrateNetwork) net;

    getAllServersOfNetwork(networkdId).parallelStream().forEach((s) -> {
      final SubstrateServer srv = (SubstrateServer) s;
      final IPathGen gen;

      // Decide whether the paths should be generated using the algorithm of Dijkstra (shortest
      // path) or the algorithm of Yen (K shortest paths).
      if (ModelFacadeConfig.YEN_PATH_GEN) {
        gen = new Yen();

        final Map<SubstrateNode, List<List<SubstrateLink>>> actMap =
            gen.getAllKFastestPaths(snet, srv, ModelFacadeConfig.YEN_K);

        // Iterate over all "paths" of the current node
        for (final SubstrateNode n : actMap.keySet()) {
          for (final List<SubstrateLink> l : actMap.get(n)) {
            createBidirectionalPathFromLinks(l);
          }
        }
      } else {
        gen = new Dijkstra();

        final Map<SubstrateNode, List<SubstrateLink>> actMap = gen.getAllFastestPaths(snet, srv);

        // Iterate over all "paths" of the current node
        for (final SubstrateNode n : actMap.keySet()) {
          createBidirectionalPathFromLinks(actMap.get(n));
        }
      }
    });
  }

  /**
   * Creates the bidirectional path (forward and backward) from a given list of links. The order of
   * the list elements is important: The source node of the forward path is determined by the source
   * node of the first link and the target node of the forward path is determined by the target node
   * of the last link. For the backward path, both nodes described above are swapped.
   * 
   * @param links Input list of links to generate paths from.
   */
  private synchronized void createBidirectionalPathFromLinks(final List<SubstrateLink> links) {
    // Check path limits
    if (links.size() < ModelFacadeConfig.MIN_PATH_LENGTH
        || links.size() > ModelFacadeConfig.MAX_PATH_LENGTH) {
      return;
    }

    // Check if a server is used as forwarding node (which is forbidden)
    for (int i = 0; i < links.size(); i++) {
      if (i != 0 && i != links.size() - 1) {
        if (links.get(i).getSource() instanceof Server) {
          return;
        }

        if (links.get(i).getTarget() instanceof Server) {
          return;
        }
      }
    }

    // Get all nodes from links
    final List<Node> nodes = new LinkedList<Node>();

    for (final SubstrateLink l : links) {
      nodes.add(l.getSource());
      // nodes.add(l.getTarget());
    }
    nodes.add(links.get(links.size() - 1).getTarget());

    final int lastIndex = links.size() - 1;
    final Node source = links.get(0).getSource();
    final Node target = links.get(lastIndex).getTarget();

    // Create forward path
    if (!doesSpecificPathWithSourceAndTargetExist(source, target, nodes, links)) {
      final SubstratePath forward = genMetaPath(source, target);
      forward.setHops(links.size());
      forward.setNetwork(links.get(0).getNetwork());
      final String name = concatNodeNames(nodes);
      forward.setName(name);
      forward.getNodes().addAll(nodes);
      forward.getLinks().addAll(links);

      // Determine bandwidth
      final int bw = getMinimumBandwidthFromSubstrateLinks(links);
      forward.setBandwidth(bw);
      forward.setResidualBandwidth(bw);

      paths.put(name, forward);

      // Add path to lookup map
      if (!pathSourceMap.containsKey(source)) {
        pathSourceMap.put((SubstrateNode) source, new HashSet<SubstratePath>());
      }
      pathSourceMap.get(source).add(forward);
    }

    // Create reverse path
    final List<Node> reversedNodes = Lists.reverse(nodes);
    // Get all opposite links
    final List<SubstrateLink> oppositeLinks = getOppositeLinks(links);

    if (!doesSpecificPathWithSourceAndTargetExist(target, source, reversedNodes,
        Lists.reverse(oppositeLinks))) {
      final SubstratePath reverse = genMetaPath(target, source);
      reverse.getLinks().addAll(Lists.reverse(oppositeLinks));

      reverse.setHops(links.size());
      reverse.setNetwork(links.get(0).getNetwork());
      final String name = concatNodeNames(reversedNodes);
      reverse.setName(name);
      reverse.getNodes().addAll(reversedNodes);

      final int revBw = getMinimumBandwidthFromSubstrateLinks(oppositeLinks);
      reverse.setBandwidth(revBw);
      reverse.setResidualBandwidth(revBw);

      paths.put(name, reverse);

      // Add path to lookup map
      if (!pathSourceMap.containsKey(target)) {
        pathSourceMap.put((SubstrateNode) target, new HashSet<SubstratePath>());
      }
      pathSourceMap.get(target).add(reverse);
    }
  }

  /**
   * Creates a string with all names of given node list.
   * 
   * @param nodes Input list of nodes.
   * @return String with all names of given node list.
   */
  private String concatNodeNames(final List<Node> nodes) {
    String name = "path";

    for (final Node n : nodes) {
      name += "-";
      name += n.getName();
    }

    return name;
  }

  /**
   * Calculates the minimum bandwidth found in a collection of links. This method is used to
   * calculate the actual bandwidth of a path.
   * 
   * @param links Collection of links to search the minimal value in.
   * @return Minimal bandwidth value of all links from the collection.
   */
  private int getMinimumBandwidthFromSubstrateLinks(final Collection<SubstrateLink> links) {
    int val = Integer.MAX_VALUE;

    for (final Link l : links) {
      if (l.getBandwidth() < val) {
        val = l.getBandwidth();
      }
    }

    return val;
  }

  /**
   * Takes a given link and searches for the opposite one. The opposite link has the original target
   * as source and vice versa.
   * 
   * @param link Link to search opposite link for.
   * @return Opposite link for given link.
   */
  private Link getOppositeLink(final Link link) {
    final Node source = link.getSource();
    final Node target = link.getTarget();

    final Network net = link.getNetwork();
    final List<Link> allLinks = net.getLinks();

    for (Link l : allLinks) {
      if (l.getSource().equals(target) && l.getTarget().equals(source)) {
        return l;
      }
    }

    throw new UnsupportedOperationException("Opposite link could not be found!");
  }

  /**
   * Returns a list of all opposite links for a given set of links. Basically, it calls the method
   * {@link #getOppositeLink(Link)} for every link in the incoming collection.
   * 
   * @param links Collection of links to get opposites for.
   * @return List of opposite links.
   */
  private List<SubstrateLink> getOppositeLinks(final Collection<SubstrateLink> links) {
    final List<SubstrateLink> opposites = new LinkedList<SubstrateLink>();

    for (Link l : links) {
      opposites.add((SubstrateLink) getOppositeLink(l));
    }

    return opposites;
  }

  /**
   * Generates a meta path that has only the source and the target node set up. This is a utility
   * method for the path creation.
   * 
   * @param source Source node for the path.
   * @param target Target node for the path.
   * @return Generated substrate (meta-)path.
   */
  private SubstratePath genMetaPath(final Node source, final Node target) {
    SubstratePath path = ModelFactory.eINSTANCE.createSubstratePath();
    path.setSource(source);
    path.setTarget(target);
    return path;
  }

  /**
   * This method checks the availability of a path with given source and target node.
   * 
   * @param source Source to search path for.
   * @param target Target to search path for.
   * @return True if a path with given parameters already exists.
   */
  @Deprecated
  public boolean doesPathWithSourceAndTargetExist(final Node source, final Node target) {
    return getPathFromSourceToTarget(source, target) != null;
  }

  /**
   * This method checks the availability of a specific path with given source and target node.
   * 
   * @param source Source to search path for.
   * @param target Target to search path for.
   * @param nodes List of nodes that must be contained in the found path.
   * @param links List of links that must be contained in the found path.
   * @return True if a path with given parameters already exists.
   */
  private boolean doesSpecificPathWithSourceAndTargetExist(final Node source, final Node target,
      final List<Node> nodes, final List<SubstrateLink> links) {
    final Set<Path> foundPaths = getPathsFromSourceToTarget(source, target);

    for (final Path p : foundPaths) {
      // Check that both "sets" of nodes and links are equal
      if (nodes.containsAll(p.getNodes()) && p.getNodes().containsAll(nodes)
          && links.containsAll(p.getLinks()) && p.getLinks().containsAll(links)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns true, if a given node ID exists in a given network model.
   * 
   * @param id Node ID to check.
   * @param networkId Network ID to check node ID in.
   * @return True, if the given node ID exists.
   */
  public boolean doesNodeIdExist(final String id, final String networkId) {
    checkStringValid(new String[] {id, networkId});

    return !getNetworkById(networkId).getNodes().stream().filter(n -> n.getName().equals(id))
        .collect(Collectors.toList()).isEmpty();
  }

  /**
   * Returns true, if a given link ID exists in a given network model.
   * 
   * @param id Link ID to check.
   * @param networkId Network ID to check node ID in.
   * @return True, if the given link ID exists.
   */
  public boolean doesLinkIdExist(final String id, final String networkId) {
    checkStringValid(id);
    checkStringValid(networkId);

    return !getNetworkById(networkId).getLinks().stream().filter(l -> l.getName().equals(id))
        .collect(Collectors.toList()).isEmpty();
  }

  /**
   * Completely resets the network model. This method clears the collection of networks of the root
   * node.
   */
  public void resetAll() {
    root.getNetworks().clear();
    generatedMetaPaths.clear();
    visitedNodes.clear();
    linksUntilNode.clear();
    counter.set(0);
  }

  /**
   * Returns a path from source node to target node if such a path exists. Else it returns null.
   * 
   * @param source Source node.
   * @param target Target node.
   * @return Path if a path between source and target does exist.
   */
  public Path getPathFromSourceToTarget(final Node source, final Node target) {
    final Set<SubstratePath> allPaths = pathSourceMap.get(source);

    // Check if there are any paths from source node to any other node
    if (allPaths == null) {
      return null;
    }

    for (final Path p : allPaths) {
      if (p.getSource().equals(source) && p.getTarget().equals(target)) {
        return p;
      }
    }

    return null;
  }

  /**
   * Returns a path from source node ID to target node ID if such a path exists. Else it returns
   * null.
   * 
   * @param sourceId Source node ID.
   * @param targetId Target node ID.
   * @return Path if a path between source and target does exist.
   */
  public Path getPathFromSourceToTarget(final String sourceId, final String targetId) {
    final Node source = getNodeById(sourceId);
    final Node target = getNodeById(targetId);
    return getPathFromSourceToTarget(source, target);
  }

  /**
   * Returns all paths from source node to target node if any exists. Else it returns an empty set.
   * 
   * @param source Source node.
   * @param target Target node.
   * @return Set of paths if any exists or else an empty set.
   */
  public Set<Path> getPathsFromSourceToTarget(final Node source, final Node target) {
    final Set<SubstratePath> allPaths = pathSourceMap.get(source);

    // Check if there are any paths from source node to any other node
    if (allPaths == null) {
      return new HashSet<Path>();
    }

    final Set<Path> foundPaths = Collections.synchronizedSet(new HashSet<Path>());

    allPaths.stream().forEach(p -> {
      if (p.getSource().equals(source) && p.getTarget().equals(target)) {
        foundPaths.add(p);
      }
    });

    return foundPaths;
  }

  /**
   * Returns a link from source node to target node if such a link exists. Else it returns null.
   * 
   * @param source Source node.
   * @param target Target node.
   * @return Link if a link between source and target does exist.
   */
  public Link getLinkFromSourceToTarget(final Node source, final Node target) {
    final List<Link> allLinks = getAllLinksOfNetwork(source.getNetwork().getName());

    for (final Link l : allLinks) {
      if (l.getSource().equals(source) && l.getTarget().equals(target)) {
        return l;
      }
    }

    return null;
  }

  /**
   * Checks string validity (null and blank).
   * 
   * @param strings Possible array of strings to check.
   */
  public void checkStringValid(final String... strings) {
    if (strings == null) {
      throw new IllegalArgumentException("Provided String(-array) was null!");
    }

    for (String string : strings) {
      if (string == null) {
        throw new IllegalArgumentException("Provided String was null!");
      }

      if (string.isBlank()) {
        throw new IllegalArgumentException("Provided String was blank!");
      }
    }
  }

  /**
   * Checks integer validity (<0).
   * 
   * @param ints Possible array of integers to check.
   */
  public void checkIntValid(final int... ints) {
    if (ints == null) {
      throw new IllegalArgumentException("Provided int(-array) was null!");
    }

    for (int cInt : ints) {
      if (cInt < 0) {
        throw new IllegalArgumentException("Provided int was smaller than zero!");
      }
    }
  }

  /**
   * Returns the next ID.
   * 
   * @return Next free ID.
   */
  public String getNextId() {
    return String.valueOf(counter.getAndIncrement());
  }

  /**
   * Saves the model to file.
   */
  public void persistModel() {
    eMoflonEMFUtil.saveModel(root, PERSISTANT_MODEL_PATH);
  }

  /**
   * Loads the model from file.
   */
  public void loadModel() {
    loadModel(PERSISTANT_MODEL_PATH);
  }

  /**
   * Loads the model from given file path.
   * 
   * @param path File path as string.
   */
  public void loadModel(final String path) {
    checkStringValid(path);
    // TODO: Figure out, why the load mechanism does not work if there wasn't
    // any save operation beforehand.
    eMoflonEMFUtil.saveModel(root, "/dev/null");
    root = (Root) eMoflonEMFUtil.loadModel(path);
  }

  /*
   * Embedding related methods.
   */

  /**
   * Adds an embedding of one virtual network to one substrate network.
   * 
   * @param substrateId Substrate network id.
   * @param virtualId Virtual network id.
   * @return True if embedding was successful.
   */
  public boolean embedNetworkToNetwork(final String substrateId, final String virtualId) {
    // Check that both networks exist
    if (!networkExists(substrateId) || !networkExists(virtualId)) {
      throw new IllegalArgumentException("One of the two networks does not exist.");
    }

    final SubstrateNetwork subNet = (SubstrateNetwork) getNetworkById(substrateId);
    final VirtualNetwork virtNet = (VirtualNetwork) getNetworkById(virtualId);

    // Check that the virtual network was not embedded before
    if (virtNet.getHost() != null) {
      throw new IllegalArgumentException("Virtual network was embedded before.");
    }

    virtNet.setHost(subNet);
    return subNet.getGuests().add(virtNet);
  }

  /**
   * Adds an embedding of one virtual server to one substrate server.
   * 
   * @param substrateId Substrate server id.
   * @param virtualId Virtual server id.
   * @return True if embedding was successful.
   */
  public boolean embedServerToServer(final String substrateId, final String virtualId) {
    final SubstrateServer subServ = (SubstrateServer) getServerById(substrateId);
    final VirtualServer virtServ = (VirtualServer) getServerById(virtualId);
    boolean success = true;

    if (subServ.getResidualCpu() >= virtServ.getCpu()
        && subServ.getResidualMemory() >= virtServ.getMemory()
        && subServ.getResidualStorage() >= virtServ.getStorage()) {
      success &= subServ.getGuestServers().add(virtServ);
      virtServ.setHost(subServ);

      // Update residual values of the host
      final int oldResCpu = subServ.getResidualCpu();
      final int oldResMem = subServ.getResidualMemory();
      final int oldResStor = subServ.getResidualStorage();
      subServ.setResidualCpu(oldResCpu - virtServ.getCpu());
      subServ.setResidualMemory(oldResMem - virtServ.getMemory());
      subServ.setResidualStorage(oldResStor - virtServ.getStorage());
    } else {
      throw new UnsupportedOperationException(
          "Embedding of server not possible due resource " + "constraint violation.");
    }

    return success;
  }

  /**
   * Adds an embedding of one virtual switch to one substrate node. The substrate node may either be
   * a substrate switch or a substrate server.
   * 
   * @param substrateId Substrate Id.
   * @param virtualId Virtual Id.
   * @return True if embedding was successful.
   */
  public boolean embedSwitchToNode(final String substrateId, final String virtualId) {
    final SubstrateNode subNode = (SubstrateNode) getNodeById(substrateId);
    final VirtualSwitch virtSwitch = (VirtualSwitch) getSwitchById(virtualId);
    virtSwitch.setHost(subNode);
    return subNode.getGuestSwitches().add(virtSwitch);
  }

  /**
   * Adds an embedding of one virtual link to one substrate server. There are no constraints to
   * check in this particular case.
   * 
   * @param substrateId Substrate Id.
   * @param virtualId Virtual Id.
   * @return True if embedding was successful.
   */
  public boolean embedLinkToServer(final String substrateId, final String virtualId) {
    final SubstrateServer subServ = (SubstrateServer) getServerById(substrateId);
    final VirtualLink virtLink = (VirtualLink) getLinkById(virtualId);

    // // Check conditions
    // // Source
    // if (virtLink.getSource() instanceof VirtualServer) {
    // if (((VirtualServer) virtLink.getSource()).getHost() == null) {
    // throw new UnsupportedOperationException("Virtual link source host is null.");
    // }
    // if (!((VirtualServer) virtLink.getSource()).getHost().equals(subServ)) {
    // throw new UnsupportedOperationException();
    // }
    // } else if (virtLink.getSource() instanceof VirtualSwitch) {
    // if (((VirtualSwitch) virtLink.getSource()).getHost() == null) {
    // throw new UnsupportedOperationException("Virtual link source host is null.");
    // }
    // if (!((VirtualSwitch) virtLink.getSource()).getHost().equals(subServ)) {
    // throw new UnsupportedOperationException();
    // }
    // }
    //
    // // Target
    // if (virtLink.getTarget() instanceof VirtualServer) {
    // if (((VirtualServer) virtLink.getTarget()).getHost() == null) {
    // throw new UnsupportedOperationException("Virtual link target host is null.");
    // }
    // if (!((VirtualServer) virtLink.getTarget()).getHost().equals(subServ)) {
    // throw new UnsupportedOperationException();
    // }
    // } else if (virtLink.getTarget() instanceof VirtualSwitch) {
    // if (((VirtualSwitch) virtLink.getTarget()).getHost() == null) {
    // throw new UnsupportedOperationException("Virtual link target host is null.");
    // }
    // if (!((VirtualSwitch) virtLink.getTarget()).getHost().equals(subServ)) {
    // throw new UnsupportedOperationException();
    // }
    // }

    // No constraints to check!
    virtLink.setHost(subServ);
    return subServ.getGuestLinks().add(virtLink);
  }

  /**
   * Adds an embedding of one virtual link to one substrate path.
   * 
   * @param substrateId Substrate Id.
   * @param virtualId Virtual Id.
   * @return True if embedding was successful.
   */
  public boolean embedLinkToPath(final String substrateId, final String virtualId) {
    final SubstratePath subPath = (SubstratePath) getPathById(substrateId);
    final VirtualLink virtLink = (VirtualLink) getLinkById(virtualId);
    boolean success = true;

    if (!ModelFacadeConfig.IGNORE_BW) {
      if (subPath.getResidualBandwidth() < virtLink.getBandwidth()) {
        throw new UnsupportedOperationException(
            "Embeding of link not possible due resource constraint violation.");
      }
    }

    success &= subPath.getGuestLinks().add(virtLink);
    virtLink.setHost(subPath);

    // Add guest link to all substrate links contained in the path?
    if (ModelFacadeConfig.LINK_HOST_EMBED_PATH) {
      for (final Link l : subPath.getLinks()) {
        final SubstrateLink sl = (SubstrateLink) l;
        sl.getGuestLinks().add(virtLink);
      }
    }

    // Update residual values of the host path
    if (!ModelFacadeConfig.IGNORE_BW) {
      final int oldResBw = subPath.getResidualBandwidth();
      subPath.setResidualBandwidth(oldResBw - virtLink.getBandwidth());

      // Update all residual bandwidths of all links of the path
      // This should only be done, if the virtual links are *not* embedded to the substrate ones
      // before, because else we would subtract the virtual bandwidth twice.
      if (!ModelFacadeConfig.LINK_HOST_EMBED_PATH) {
        for (Link actLink : subPath.getLinks()) {
          SubstrateLink actSubLink = (SubstrateLink) actLink;
          final int resBw = actSubLink.getResidualBandwidth();
          actSubLink.setResidualBandwidth(resBw - virtLink.getBandwidth());
        }
      }
    }

    return success;
  }

}
