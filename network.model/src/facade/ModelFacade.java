package facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.moflon.core.utilities.eMoflonEMFUtil;

import model.Link;
import model.ModelFactory;
import model.Network;
import model.Node;
import model.Root;
import model.Server;
import model.Switch;
import model.VirtualNetwork;

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
		
		return net.getLinks().add(link);
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
	
	public void embedNetworkToNetwork() {
		//TODO
	}
	
	public void embedServerToServer() {
		//TODO
	}
	
	public void embedSwitchToNode() {
		//TODO
	}
	
	public void embedLinkToServer() {
		//TODO
	}
	
	public void embedLinkToLink() {
		//TODO
	}
	
	public void embedLinkToPath() {
		//TODO
	}
	
}
