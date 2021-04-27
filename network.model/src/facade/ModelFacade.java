package facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.moflon.core.utilities.eMoflonEMFUtil;

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
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
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
	final Set<Node> visitedNodes = new HashSet<Node>();
	final List<SubstratePath> generatedMetaPaths = new LinkedList<SubstratePath>();
	
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
	
	// TODO: Remove me later on.
	public void dummy() {
		System.out.println("=> Dummy method called.");
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
		
		return getNetworkById(networkId).getNodes().stream()
				.filter(n -> n instanceof Server)
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
		
		return getNetworkById(networkId).getNodes().stream()
				.filter(n -> n instanceof Switch)
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
		
		return (Network) root.getNetworks().stream()
				.filter(n -> n.getName().equals(id))
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
		
		return getAllNetworks().stream()
		.filter(n -> n.getName().equals(id))
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
		nets.stream()
		.forEach(net -> {
			net.getNodes().stream()
			.filter(n -> n instanceof Node)
			.filter(n -> n.getName().equals(id))
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
		
		List<Network> nets = root.getNetworks();
		List<Link> links = new ArrayList<Link>();
		nets.stream()
		.forEach(net -> {
			net.getLinks().stream()
			.filter(l -> l.getName().equals(id))
			.forEach(l -> links.add(l));
		});
		return links.get(0);
	}
	
	/**
	 * Returns a path object for a given ID.
	 * 
	 * @param id ID to return path object for.
	 * @return Path object for given ID.
	 */
	public Path getPathById(final String id) {
		checkStringValid(id);
		
		List<Network> nets = root.getNetworks();
		List<Path> paths = new ArrayList<Path>();
		nets.stream()
		.forEach(net -> {
			net.getPaths().stream()
			.filter(p -> p.getName().equals(id))
			.forEach(p -> paths.add(p));
		});
		return paths.get(0);
	}
	
	/**
	 * Creates and adds a new (substrate or virtual) network object with given ID to the root
	 * node of the model.
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
		
		return net.getLinks().add(link);
	}
	
	/**
	 * This method creates all necessary paths *after* all other components are added to the
	 * network. Please notice: Currently, the method is only suited for tree-based networks that
	 * do not have any cycles.
	 * 
	 * Assumptions: Every server of the given network is only connected to one switch.
	 * 
	 * @param networkdId Network ID to add paths to.
	 */
	public void createAllPathsForNetwork(final String networkdId) {
		checkStringValid(networkdId);
		
		if (getNetworkById(networkdId) instanceof VirtualNetwork) {
			throw new UnsupportedOperationException("Given network ID is virtual,"
					+ " which is not supported!");
		}
		
		// Iterate over all servers
		for (Node s : getAllServersOfNetwork(networkdId)) {
			final SubstrateServer srv = (SubstrateServer) s;
			recursivePathGen(srv, srv);
			
			// Reset visited nodes -> This collection has to be empty for every new server
			// we start the recursion with.
			visitedNodes.clear();
		}
		
		// Add attributes to meta paths and add them to model after all.
		// The attributes are: (1) bandwidth, (2) hops, (3) network.
		for (Path m : generatedMetaPaths) {
			// Check if path with specific source and target already exist.
			// TODO: This is quite a workaround and should be replaced in the future.
			if (doesPathWithSourceAndTargetExist(networkdId, m.getSource().getName(), 
					m.getTarget().getName())) {
				continue;
			}
			
			// (1) bandwidth
			int minFoundBw = Integer.MAX_VALUE;
			for (Link l : m.getLinks()) {
				if (l.getBandwidth() < minFoundBw) {
					minFoundBw = l.getBandwidth();
				}
			}
			m.setBandwidth(minFoundBw);
			
			// (2) hops
			m.setHops(m.getLinks().size());
			
			// (3) Network, this also adds the paths to the network model
			m.setNetwork(getNetworkById(networkdId));
		}
		
		System.out.println("=> Dummy.");
	}
	
	private void recursivePathGen(final Node source, final Node node) {
		
		// End of recursion: The given node was already visited before.
		if (visitedNodes.contains(node)) {
			return;
		}
		
//		// Duplicate and extend every path known before that ends at the current node
//		Set<SubstratePath> toAdd = new HashSet<SubstratePath>();
//		for (SubstratePath prev : generatedMetaPaths) {
//			if (prev.getTarget().equals(node)) {
//				SubstratePath newPath = genMetaPath(prev.getSource(), node);
//				newPath.getLinks().addAll(prev.getLinks());
//				newPath.getLinks().add();
//				toAdd.add(newPath);
//			}
//		}
//		generatedMetaPaths.addAll(toAdd);
		
		// Add current node to set of visited nodes
		visitedNodes.add(node);
		
		// Iterate over all outgoing links
		for(Link l : node.getOutgoingLinks()) {
//			generatedMetaPaths.add(genMetaPath(node, l.getTarget()));
			
			// Extend all paths that end on current node
//			Set<SubstratePath> toAdd = new HashSet<SubstratePath>();
//			for (SubstratePath prev : generatedMetaPaths) {
//				if (prev.getTarget().equals(node)) {
//					SubstratePath newPath = genMetaPath(prev.getSource(), l.getTarget());
//					newPath.getLinks().add(l);
//					newPath.getNodes().add(l.getTarget());
//					toAdd.add(newPath);
//				}
//			}
//			generatedMetaPaths.addAll(toAdd);
			
			// Create path from current node to target of current link
			if (!visitedNodes.contains(l.getTarget())) {
				SubstratePath current = genMetaPath(source, l.getTarget());
				current.getLinks().add(l);
				current.getNodes().add(source);
				current.getNodes().add(l.getTarget());
				generatedMetaPaths.add(current);
				
				// If target node is a link, also create the opposite path
				// (It will not be created automatically!)
				if (l.getTarget() instanceof Switch) {
					SubstratePath opposite = genMetaPath(l.getTarget(), source);
					opposite.getLinks().add(l);
					opposite.getNodes().add(source);
					opposite.getNodes().add(l.getTarget());
					generatedMetaPaths.add(opposite);
				}
			}

			
			// Call method for target of current link
			recursivePathGen(source, l.getTarget());
		}
	}
	
	private SubstratePath genMetaPath(final Node source, final Node target) {
		SubstratePath path = ModelFactory.eINSTANCE.createSubstratePath();
		path.setSource(source);
		path.setTarget(target);
		return path;
	}
	
//	private void addPathToNetwork(final Path path) {
//		// TODO: Null check
//		
//		final Network network = path.getNetwork();
//		final int bandwidth = path.getBandwidth();
//		final Node source = path.getSource();
//		final Node target = path.getTarget();
//		final List<Link> links = path.getLinks();
//		
//		addPathToNetwork(getNextId(), network, bandwidth, source, target, links);
//	}
	
//	private void addPathToNetwork(final String id, final Network network, final int bandwidth,
//			final Node source, final Node target, final List<Link> links) {
//		// TODO: Fix and extend null checks
////		checkStringValid(new String[] {id, networkId, sourceId, targetId});
////		checkStringValid(linkIds);
//		checkIntValid(bandwidth);
//		
//		SubstratePath path;
//		if (network instanceof SubstrateNetwork) {
//			path = ModelFactory.eINSTANCE.createSubstratePath();
//		} else {
//			throw new UnsupportedOperationException("Path creation on virtual networks "
//					+ "is not possible.");
//		}
//		path.setName(id);
//		path.setNetwork(network);
//		path.setBandwidth(bandwidth);
//		path.setSource(source);
//		path.setTarget(target);
//		
//		// Get all nodes from links
//		final Set<Node> allNodes = new HashSet<Node>();
//		for (Link l : links) {
//			allNodes.add(l.getSource());
//			allNodes.add(l.getTarget());
//		}
//		
//		if (!allNodes.contains(source)) {
//			allNodes.add(source);
//		}
//		
//		if (!allNodes.contains(target)) {
//			allNodes.add(target);
//		}
//		
//		// Add all links and all nodes to path
//		path.getNodes().addAll(allNodes);
//		path.getLinks().addAll(links);
//		
//		// As all paths are substrate paths, we have to set the residual bandwidth value.
//		path.setResidualBandwidth(bandwidth);
//		
//		// Set number of hops equals to number of links
//		path.setHops(links.size());
//		
//		// Add path to the network
//		network.getPaths().add(path);
//	}
	
//	/**
//	 * Creates and adds a (one) new path to a network. The network has to be a substrate network.
//	 * 
//	 * @param id ID of the new path to create.
//	 * @param networkId Network ID to add path to.
//	 * @param bandwidth Bandwidth amount.
//	 * @param sourceId ID of the source node.
//	 * @param targetId ID of the target node.
//	 * @param linkIds Array of link IDs that are part of the new path.
//	 */
//	private void addPathToNetwork(final String id, final String networkId, final int bandwidth,
//			final String sourceId, final String targetId, final String... linkIds) {
//		checkStringValid(new String[] {id, networkId, sourceId, targetId});
//		checkStringValid(linkIds);
//		checkIntValid(bandwidth);
//		
//		final Network net = getNetworkById(networkId);
//		SubstratePath path;
//		if (net instanceof SubstrateNetwork) {
//			path = ModelFactory.eINSTANCE.createSubstratePath();
//		} else {
//			throw new UnsupportedOperationException("Path creation on virtual networks "
//					+ "is not possible.");
//		}
//		path.setName(id);
//		path.setNetwork(net);
//		path.setBandwidth(bandwidth);
//		path.setSource(getNodeById(sourceId));
//		path.setTarget(getNodeById(targetId));
//		
//		// Get all links
//		final List<Link> allLinks = new LinkedList<Link>();
//		for (String l : linkIds) {
//			allLinks.add(getLinkById(l));
//		}
//		
//		// Get all nodes from links
//		final Set<Node> allNodes = new HashSet<Node>();
//		for (Link l : allLinks) {
//			allNodes.add(l.getSource());
//			allNodes.add(l.getTarget());
//		}
//		
//		if (!allNodes.contains(getNodeById(sourceId))) {
//			allNodes.add(getNodeById(sourceId));
//		}
//		
//		if (!allNodes.contains(getNodeById(targetId))) {
//			allNodes.add(getNodeById(targetId));
//		}
//		
//		// Add all links and all nodes to path
//		path.getNodes().addAll(allNodes);
//		path.getLinks().addAll(allLinks);
//		
//		// As all paths are substrate paths, we have to set the residual bandwidth value.
//		path.setResidualBandwidth(bandwidth);
//		
//		// Set number of hops equals to number of links
//		path.setHops(allLinks.size());
//		
//		// Add path to the network
//		ModelFacade.getInstance().getNetworkById(networkId).getPaths().add(path);
//	}
	
	public boolean doesPathWithSourceAndTargetExist(final String networkdId, 
			final String sourceId, final String targetId) {
		SubstrateNetwork net = (SubstrateNetwork) getNetworkById(networkdId);
		for (Path p : net.getPaths()) {
			if (p.getSource().getName().equals(sourceId)
					&& p.getTarget().getName().equals(targetId)) {
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
		
		return !getNetworkById(networkId).getNodes().stream()
				.filter(n -> n.getName().equals(id))
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
		
		return !getNetworkById(networkId).getLinks().stream()
				.filter(l -> l.getName().equals(id))
				.collect(Collectors.toList()).isEmpty();
	}
	
	/**
	 * Completely resets the network model. This method clears the collection of networks
	 * of the root node.
	 */
	public void resetAll() {
		root.getNetworks().clear();
		generatedMetaPaths.clear();
		visitedNodes.clear();
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
//		eMoflonEMFUtil.saveModel(root, "/dev/null");
		root = (Root) eMoflonEMFUtil.loadModel(PERSISTANT_MODEL_PATH);
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
		final SubstrateNetwork subNet = (SubstrateNetwork) getNetworkById(substrateId);
		final VirtualNetwork virtNet = (VirtualNetwork) getNetworkById(virtualId);
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
		
		if (subServ.getResidualCpu() >= virtServ.getCpu() &&
				subServ.getResidualMemory() >= virtServ.getMemory() &&
				subServ.getResidualStorage() >= virtServ.getStorage()) {
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
			throw new UnsupportedOperationException("Embedding of server not possible due resource "
					+ "constraint violation.");
		}
		
		return success;
	}
	
	/**
	 * Adds an embedding of one virtual switch to one substrate node. The substrate node
	 * may either be a substrate switch or a substrate server.
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
	 * Adds an embedding of one virtual link to one substrate server. There are no
	 * constraints to check in this particular case.
	 * 
	 * @param substrateId Substrate Id.
	 * @param virtualId Virtual Id.
	 * @return True if embedding was successful.
	 */
	public boolean embedLinkToServer(final String substrateId, final String virtualId) {
		final SubstrateServer subServ = (SubstrateServer) getServerById(substrateId);
		final VirtualLink virtLink = (VirtualLink) getLinkById(virtualId);
		
		// No constraints to check!
		virtLink.getHosts().add(subServ);
		return subServ.getGuestLinks().add(virtLink);
	}
	
	/**
	 * Adds an embedding of one virtual link to one substrate link.
	 * 
	 * @param substrateId Substrate Id.
	 * @param virtualId Virtual Id.
	 * @return True if embedding was successful.
	 */
	public boolean embedLinkToLink(final String substrateId, final String virtualId) {
		final SubstrateLink subLink = (SubstrateLink) getLinkById(substrateId);
		final VirtualLink virtLink = (VirtualLink) getLinkById(virtualId);
		boolean success = true;
		
		if (subLink.getResidualBandwidth() >= virtLink.getBandwidth()) {
			success &= subLink.getGuestLinks().add(virtLink);
			virtLink.getHosts().add(subLink);
			
			// Update residual values of the host
			final int oldResBw = subLink.getResidualBandwidth();
			subLink.setResidualBandwidth(oldResBw - virtLink.getBandwidth());
		} else {
			throw new UnsupportedOperationException("Embeding of link not possible due resource "
					+ "constraint violation.");
		}
		
		return success;
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
		
		if (subPath.getResidualBandwidth() >= virtLink.getBandwidth()) {
			success &= subPath.getGuestLinks().add(virtLink);
			virtLink.getHosts().add(subPath);
			
			// Update residual values of the host path
			final int oldResBw = subPath.getResidualBandwidth();
			subPath.setResidualBandwidth(oldResBw - virtLink.getBandwidth());
		} else {
			throw new UnsupportedOperationException("Embeding of link not possible due resource "
					+ "constraint violation.");
		}
		
		// Update all residual bandwidths of all links of the path
		for (Link actLink : subPath.getLinks()) {
			SubstrateLink actSubLink = (SubstrateLink) actLink;
			final int resBw = actSubLink.getResidualBandwidth();
			actSubLink.setResidualBandwidth(resBw - virtLink.getBandwidth());
		}
		
		return success;
	}
	
}
