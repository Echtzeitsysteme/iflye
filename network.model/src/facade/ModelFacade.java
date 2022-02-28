package facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.moflon.core.utilities.eMoflonEMFUtil;

import facade.config.ModelFacadeConfig;
import model.Link;
import model.ModelFactory;
import model.Network;
import model.Node;
import model.Root;
import model.Server;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.Switch;
import model.VirtualElement;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Facade to access and manipulate the underlying model.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
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
	private final Set<Node> visitedNodes = new HashSet<>();
	private final Set<Link> linksUntilNode = new HashSet<>();

	/**
	 * Private constructor to disable direct object instantiation.
	 */
	private ModelFacade() {
	}

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

	/*
	 * Look-up data structures.
	 */
	private Map<String, Link> links = new HashMap<>();

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
	 * Returns a network object by its ID.
	 *
	 * @param id ID to return network object for.
	 * @return Network object for given ID.
	 */
	public Network getNetworkById(final String id) {
		checkStringValid(id);

		return root.getNetworks().stream().filter(n -> n.getName().equals(id)).collect(Collectors.toList()).get(0);
	}

	/**
	 * Returns true if a network for a given ID exists.
	 *
	 * @param id ID to check network existence for.
	 * @return True if network does exist in model.
	 */
	public boolean networkExists(final String id) {
		checkStringValid(id);

		return getAllNetworks().stream().filter(n -> n.getName().equals(id)).collect(Collectors.toList()).size() != 0;
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
		List<Node> nodes = new ArrayList<>();
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
		// net.getLinks().stream().filter(l -> l.getName().equals(id)).forEach(l ->
		// links.add(l));
		// });
		// return links.get(0);
		return links.get(id);
	}

	/**
	 * Creates and adds a new (substrate or virtual) network object with given ID to
	 * the root node of the model.
	 *
	 * @param id        ID of the new network to create.
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
	 * @param id        ID of the new server to create.
	 * @param networkId Network ID to add the new server to.
	 * @param cpu       CPU amount.
	 * @param memory    Memory amount.
	 * @param storage   Storage amount.
	 * @param depth     Depth inside the network.
	 * @return True if creation was successful.
	 */
	public boolean addServerToNetwork(final String id, final String networkId, final int cpu, final int memory,
			final int storage, final int depth) {
		checkStringValid(new String[] { id, networkId });
		checkIntValid(new int[] { cpu, memory, storage, depth });

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
	 * @param id        ID of the new switch to create.
	 * @param networkId Network ID to add the new server to.
	 * @param depth     Depth inside the network.
	 * @return True if creation was successful.
	 */
	public boolean addSwitchToNetwork(final String id, final String networkId, final int depth) {
		checkStringValid(new String[] { id, networkId });
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
	 * @param id        ID of the new link to create.
	 * @param networkId Network ID to add link to.
	 * @param bandwidth Bandwidth amount.
	 * @param sourceId  ID of the source node.
	 * @param targetId  ID of the target node.
	 * @return True if link creation was successful.
	 */
	public boolean addLinkToNetwork(final String id, final String networkId, final int bandwidth, final String sourceId,
			final String targetId) {
		checkStringValid(new String[] { id, networkId, sourceId, targetId });
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

		links.put(id, link);
		return net.getLinks().add(link);
	}

	/**
	 * Determines the maximum path length needed to connect one server within the
	 * network with a core switch. Throws an {@link UnsupportedOperationException}
	 * if servers have different depths.
	 *
	 * @param networkId Network ID to calculate maximum needed path length for.
	 * @return Maximum path length.
	 */
	private int determineMaxPathLengthForTree(final String networkId) {
		int maxServerDepth = Integer.MAX_VALUE;

		final List<Node> servers = getAllServersOfNetwork(networkId);
		for (final Node n : servers) {
			final SubstrateServer srv = (SubstrateServer) n;

			if (srv.getDepth() != maxServerDepth) {
				if (maxServerDepth != Integer.MAX_VALUE) {
					throw new UnsupportedOperationException(
							"In network " + networkId + " are servers with different depths, which is not supported.");
				}

				if (srv.getDepth() < maxServerDepth) {
					maxServerDepth = srv.getDepth();
				}
			}
		}

		int minSwitchDepth = Integer.MAX_VALUE;
		final List<Node> switches = getAllSwitchesOfNetwork(networkId);
		for (final Node n : switches) {
			final SubstrateSwitch sw = (SubstrateSwitch) n;
			if (sw.getDepth() < minSwitchDepth) {
				minSwitchDepth = sw.getDepth();
			}
		}

		return maxServerDepth - minSwitchDepth;
	}

	/**
	 * Creates a string with all names of given node list.
	 *
	 * @param nodes Input list of nodes.
	 * @return String with all names of given node list.
	 */
	private String concatNodeNames(final List<SubstrateNode> nodes) {
		String name = "path";

		for (final Node n : nodes) {
			name += "-";
			name += n.getName();
		}

		return name;
	}

	/**
	 * Calculates the minimum bandwidth found in a collection of links. This method
	 * is used to calculate the actual bandwidth of a path.
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
	 * Takes a given link and searches for the opposite one. The opposite link has
	 * the original target as source and vice versa.
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
	 * Returns a list of all opposite links for a given set of links. Basically, it
	 * calls the method {@link #getOppositeLink(Link)} for every link in the
	 * incoming collection.
	 *
	 * @param links Collection of links to get opposites for.
	 * @return List of opposite links.
	 */
	private List<SubstrateLink> getOppositeLinks(final Collection<SubstrateLink> links) {
		final List<SubstrateLink> opposites = new LinkedList<>();

		for (Link l : links) {
			opposites.add((SubstrateLink) getOppositeLink(l));
		}

		return opposites;
	}

	/**
	 * Returns true, if a given node ID exists in a given network model.
	 *
	 * @param id        Node ID to check.
	 * @param networkId Network ID to check node ID in.
	 * @return True, if the given node ID exists.
	 */
	public boolean doesNodeIdExist(final String id, final String networkId) {
		checkStringValid(new String[] { id, networkId });

		return !getNetworkById(networkId).getNodes().stream().filter(n -> n.getName().equals(id))
				.collect(Collectors.toList()).isEmpty();
	}

	/**
	 * Returns true, if a given link ID exists in a given network model.
	 *
	 * @param id        Link ID to check.
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
	 * Completely resets the network model. This method clears the collection of
	 * networks of the root node.
	 */
	public void resetAll() {
		root.getNetworks().clear();
		visitedNodes.clear();
		linksUntilNode.clear();
		counter.set(0);
		links.clear();
		root = ModelFactory.eINSTANCE.createRoot();
	}

	/**
	 * Returns a link from source node to target node if such a link exists. Else it
	 * returns null.
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
	 * Checks integer validity (must be greater or equal to 0).
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
	 * Saves the model to given file path.
	 *
	 * @param path File path as string.
	 */
	public void persistModel(final String path) {
		eMoflonEMFUtil.saveModel(root, path);
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
	 * @param virtualId   Virtual network id.
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
	 * @param virtualId   Virtual server id.
	 * @return True if embedding was successful.
	 */
	public boolean embedServerToServer(final String substrateId, final String virtualId) {
		final SubstrateServer subServ = (SubstrateServer) getServerById(substrateId);
		final VirtualServer virtServ = (VirtualServer) getServerById(virtualId);
		boolean success = true;

		if (subServ.getResidualCpu() >= virtServ.getCpu() && subServ.getResidualMemory() >= virtServ.getMemory()
				&& subServ.getResidualStorage() >= virtServ.getStorage()) {
			success &= subServ.getGuestServers().add(virtServ);
			virtServ.setHost(subServ);

			// Update residual values of the host
			final long oldResCpu = subServ.getResidualCpu();
			final long oldResMem = subServ.getResidualMemory();
			final long oldResStor = subServ.getResidualStorage();
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
	 * Adds an embedding of one virtual switch to one substrate switch.
	 *
	 * @param substrateId Substrate Id.
	 * @param virtualId   Virtual Id.
	 * @return True if embedding was successful.
	 */
	public boolean embedSwitchToNode(final String substrateId, final String virtualId) {
		final SubstrateSwitch subSwitch = (SubstrateSwitch) getNodeById(substrateId);
		final VirtualSwitch virtSwitch = (VirtualSwitch) getSwitchById(virtualId);
		virtSwitch.setHost(subSwitch);
		return subSwitch.getGuestSwitches().add(virtSwitch);
	}

	/**
	 * Adds an embedding of one virtual link to one substrate link.
	 *
	 * @param substrateId Substrate Id.
	 * @param virtualId   Virtual Id.
	 * @return True if embedding was successful.
	 */
	public boolean embedLinkToLink(final String substrateId, final String virtualId) {
		final SubstrateLink subLink = (SubstrateLink) getLinkById(substrateId);
		final VirtualLink virtLink = (VirtualLink) getLinkById(virtualId);
		boolean success = true;

		if (!ModelFacadeConfig.IGNORE_BW) {
			if (subLink.getResidualBandwidth() < virtLink.getBandwidth()) {
				throw new UnsupportedOperationException(
						"Embeding of link not possible due resource constraint violation.");
			}
		}

		success &= subLink.getGuestLinks().add(virtLink);
		virtLink.setHost(subLink);
		return success;
	}

	/**
	 * Removes a network embedding with the given ID from the substrate network.
	 *
	 * @param id Virtual network ID to remove embedding for.
	 */
	public void removeNetworkEmbedding(final String id) {
		checkStringValid(id);
		if (!networkExists(id)) {
			throw new IllegalArgumentException("A network with id " + id + " does not exists!");
		}

		final Network net = getNetworkById(id);

		if (net instanceof VirtualNetwork) {
			// Virtual network
			final VirtualNetwork vNet = (VirtualNetwork) net;
			unembedVirtualNetwork(vNet);
		}
	}

	/**
	 * Removes a network with the given ID from the model and re-creates the
	 * consistency of the model afterwards.
	 *
	 * @param id Network ID to remove.
	 */
	public void removeNetworkFromRoot(final String id) {
		removeNetworkFromRoot(id, true);
	}

	/**
	 * Removes a network with the given ID from the model and does not re-create the
	 * consistency of the model afterwards.
	 *
	 * @param id Network ID to remove.
	 */
	public void removeNetworkFromRootSimple(final String id) {
		removeNetworkFromRoot(id, false);
	}

	/**
	 * Removes a network with the given ID from the root.
	 *
	 * @param id                  Network ID to remove network for.
	 * @param recreateConsistency True if model must be consistent after the remove.
	 *                            Otherwise, the network will only be removed from
	 *                            the model.
	 */
	private void removeNetworkFromRoot(final String id, final boolean recreateConsistency) {
		checkStringValid(id);
		if (!networkExists(id)) {
			throw new IllegalArgumentException("A network with id " + id + " does not exists!");
		}

		final Network net = getNetworkById(id);

		if (recreateConsistency) {
			if (net instanceof SubstrateNetwork) {
				// Substrate network
				final SubstrateNetwork sNet = (SubstrateNetwork) net;

				final Set<VirtualNetwork> guestHostToNulls = new HashSet<>();
				for (final VirtualNetwork guest : sNet.getGuests()) {
					guestHostToNulls.add(guest);
					for (final Node n : getAllServersOfNetwork(guest.getName())) {
						final VirtualServer vsrv = (VirtualServer) n;
						vsrv.setHost(null);
					}

					for (final Node n : getAllSwitchesOfNetwork(guest.getName())) {
						final VirtualSwitch vsw = (VirtualSwitch) n;
						vsw.setHost(null);
					}

					for (final Link l : guest.getLinks()) {
						final VirtualLink vl = (VirtualLink) l;
						vl.setHost(null);
					}
				}

				guestHostToNulls.forEach(g -> {
					g.setHost(null);
				});
			} else {
				unembedVirtualNetwork((VirtualNetwork) net);
			}
		}

		root.getNetworks().remove(net);
	}

	/**
	 * Removes the embedding of a virtual network.
	 *
	 * @param vNet Virtual network to remove embedding for.
	 */
	public void unembedVirtualNetwork(final VirtualNetwork vNet) {
		// Check if there is a host for this virtual network.
		if (vNet.getHost() != null) {
			vNet.getHost().getGuests().remove(vNet);

			for (final Node n : vNet.getNodes()) {
				if (n instanceof VirtualServer) {
					final VirtualServer vsrv = (VirtualServer) n;
					final SubstrateServer host = vsrv.getHost();
					if (host == null) {
						continue;
					}
					host.getGuestServers().remove(vsrv);
					host.setResidualCpu(host.getResidualCpu() + vsrv.getCpu());
					host.setResidualMemory(host.getResidualMemory() + vsrv.getMemory());
					host.setResidualStorage(host.getResidualStorage() + vsrv.getStorage());
				} else if (n instanceof VirtualSwitch) {
					final VirtualSwitch vsw = (VirtualSwitch) n;
					if (vsw.getHost() == null) {
						continue;
					}
					vsw.getHost().getGuestSwitches().remove(vsw);
				}
			}

			for (final Link l : vNet.getLinks()) {
				final VirtualLink vl = (VirtualLink) l;
				final SubstrateLink host = vl.getHost();
				if (host == null) {
					continue;
				}
				host.getGuestLinks().remove(vl);
			}
		}
	}

	/**
	 * Removes a substrate server with the given ID from the network and re-creates
	 * the consistency of the model afterwards.
	 *
	 * @param id Substrate server ID to remove.
	 */
	public void removeSubstrateServerFromNetwork(final String id) {
		removeSubstrateServerFromNetwork(id, true);
	}

	/**
	 * Removes a substrate server with the given ID from the network and does not
	 * re-create the consistency of the model afterwards.
	 *
	 * @param id Substrate server ID to remove.
	 */
	public void removeSubstrateServerFromNetworkSimple(final String id) {
		removeSubstrateServerFromNetwork(id, false);
	}

	/**
	 * Removes a substrate server with the given ID from the network.
	 *
	 * @param id               Substrate server ID to remove.
	 * @param removeEmbeddings True if embeddings must be removed. Otherwise, the
	 *                         substrate server will only be removed from the
	 *                         network together will all links and paths containing
	 *                         it.
	 */
	private void removeSubstrateServerFromNetwork(final String id, final boolean removeEmbeddings) {
		final Server srv = getServerById(id);
		if (srv instanceof VirtualServer) {
			throw new IllegalArgumentException("Given ID is from a virtual server.");
		}

		final SubstrateServer ssrv = (SubstrateServer) srv;

		if (removeEmbeddings) {
			// Remove embedding of all guests
			final Set<VirtualElement> guestsToRemove = new HashSet<>();
			for (final VirtualServer guestSrv : ssrv.getGuestServers()) {
				guestsToRemove.add(guestSrv);
			}
			guestsToRemove.forEach(e -> {
				if (e instanceof VirtualServer) {
					((VirtualServer) e).setHost(null);
				} else if (e instanceof VirtualSwitch) {
					((VirtualSwitch) e).setHost(null);
				} else if (e instanceof VirtualLink) {
					((VirtualLink) e).setHost(null);
				} else {
					throw new UnsupportedOperationException("Removal of guest " + e + " not yet implemented.");
				}
			});
		}

		// Remove all links
		final Set<Link> linksToRemove = new HashSet<>();
		linksToRemove.addAll(ssrv.getIncomingLinks());
		linksToRemove.addAll(ssrv.getOutgoingLinks());
		linksToRemove.forEach(sl -> {
			removeSubstrateLink(sl, removeEmbeddings);
		});

		// Remove server itself
		getNetworkById(ssrv.getNetwork().getName()).getNodes().remove(ssrv);
		EcoreUtil.delete(ssrv);
	}

	/**
	 * Removes the given substrate link from the network. Does not check any guests.
	 *
	 * @param link             Substrate link to remove from the network.
	 * @param removeEmbeddings True if embeddings must be removed. Otherwise, the
	 *                         substrate link will only be removed from the network.
	 */
	private void removeSubstrateLink(final Link link, final boolean removeEmbeddings) {
		if (!(link instanceof SubstrateLink)) {
			throw new IllegalArgumentException("Given link is not a substrate link.");
		}
		final SubstrateLink sl = (SubstrateLink) link;

		if (removeEmbeddings) {
			sl.getGuestLinks().forEach(gl -> {
				gl.setHost(null);
			});
		}

		sl.getSource().getOutgoingLinks().remove(sl);
		sl.getTarget().getIncomingLinks().remove(sl);

		getNetworkById(link.getNetwork().getName()).getLinks().remove(link);
		links.remove(sl.getName());
		EcoreUtil.delete(sl);
	}

	/**
	 * Validates the current state of the model, i.e. checks if: (1) Every virtual
	 * element of an embedded virtual network is embedded. (2) Every residual value
	 * of the substrate elements is equal to the total value minus the embedded
	 * values.
	 */
	public void validateModel() {
		for (final Network net : root.getNetworks()) {
			if (net instanceof SubstrateNetwork) {
				validateSubstrateNetwork((SubstrateNetwork) net);
			} else if (net instanceof VirtualNetwork) {
				validateVirtualNetwork((VirtualNetwork) net);
			}
		}
	}

	/**
	 * Validates a given substrate network.
	 *
	 * @param sNet Substrate network to validate.
	 */
	private void validateSubstrateNetwork(final SubstrateNetwork sNet) {
		// Check embedded virtual networks
		sNet.getGuests().forEach(g -> {
			if (!networkExists(g.getName())) {
				throw new InternalError("Substrate network " + sNet.getName()
						+ " has embeddings from a virtual network that is not part of this model.");
			}
		});

		for (final Node n : sNet.getNodes()) {
			if (n instanceof SubstrateServer) {
				final SubstrateServer srv = (SubstrateServer) n;

				if (srv.getCpu() < 0 || srv.getMemory() < 0 || srv.getStorage() < 0) {
					throw new InternalError(
							"At least one of the normal resources of server " + srv.getName() + " was less than zero.");
				}

				if (srv.getResidualCpu() < 0 || srv.getResidualMemory() < 0 || srv.getResidualStorage() < 0) {
					throw new InternalError("At least one of the residual resources of server " + srv.getName()
							+ " was less than zero.");
				}

				int sumGuestCpu = 0;
				int sumGuestMem = 0;
				int sumGuestSto = 0;

				for (final VirtualServer gs : srv.getGuestServers()) {
					sumGuestCpu += gs.getCpu();
					sumGuestMem += gs.getMemory();
					sumGuestSto += gs.getStorage();
				}

				if (srv.getResidualCpu() != srv.getCpu() - sumGuestCpu) {
					throw new InternalError("Residual CPU value of server " + srv.getName() + " was incorrect.");
				}

				if (srv.getResidualMemory() != srv.getMemory() - sumGuestMem) {
					throw new InternalError("Residual memory value of server " + srv.getName() + " was incorrect.");
				}

				if (srv.getResidualStorage() != srv.getStorage() - sumGuestSto) {
					throw new InternalError("Residual storage value of server " + srv.getName() + " was incorrect.");
				}
			} else if (n instanceof SubstrateSwitch) {
				// Do nothing?
			}

			// Check links
			if (!sNet.getLinks().containsAll(n.getIncomingLinks())) {
				throw new InternalError("Incoming links of node " + n.getName() + " are missing in network.");
			}

			if (!sNet.getLinks().containsAll(n.getOutgoingLinks())) {
				throw new InternalError("Outgoing links of node " + n.getName() + " are missing in network.");
			}
		}

		// If ignoring of bandwidth is activated, no link or path has to be checked.
		if (ModelFacadeConfig.IGNORE_BW) {
			return;
		}

		// Check that no link bandwidth value is below zero
		for (final Link l : sNet.getLinks()) {
			final SubstrateLink sl = (SubstrateLink) l;

			if (sl.getBandwidth() < 0) {
				throw new InternalError("Normal bandwidth of link " + sl.getName() + " was smaller than zero.");
			}

			if (sl.getResidualBandwidth() < 0) {
				throw new InternalError("Residual bandwidth of link " + sl.getName() + " was smaller than zero.");
			}
		}

		// Check if virtual links are also embedded to substrate ones (additional to
		// substrate paths).
		if (ModelFacadeConfig.LINK_HOST_EMBED_PATH) {
			for (final Link l : sNet.getLinks()) {
				final SubstrateLink sl = (SubstrateLink) l;
				int sumGuestBw = 0;

				for (final VirtualLink gl : sl.getGuestLinks()) {
					sumGuestBw += gl.getBandwidth();
				}

				if (sl.getResidualBandwidth() != sl.getBandwidth() - sumGuestBw) {
					throw new InternalError("Residual bandwidth value of link " + sl.getName() + " was incorrect.");
				}
			}
		}
	}

	/**
	 * Validates a given virtual network.
	 *
	 * @param vNet Virtual network to validate.
	 */
	private void validateVirtualNetwork(final VirtualNetwork vNet) {
		// If virtual network is embedded, all of its elements have to be embedded.
		SubstrateNetwork host = null;
		if (vNet.getHost() != null) {
			host = vNet.getHost();
		} else {
			// If virtual network is not embedded, all of its elements must not be embedded.
			host = null;
		}

		for (final Node n : vNet.getNodes()) {
			if (n instanceof VirtualServer) {
				final VirtualServer vsrv = (VirtualServer) n;

				if (vsrv.getCpu() < 0 || vsrv.getMemory() < 0 || vsrv.getStorage() < 0) {
					throw new InternalError("At least one of the resources of virtual server " + vsrv.getName()
							+ " was less than zero.");
				}

				if (host == null && vsrv.getHost() == null) {
					continue;
				} else if (host == null || vsrv.getHost() == null) {
					// Do nothing to trigger exception
				} else if (host.equals(vsrv.getHost().getNetwork())) {
					continue;
				}
				throw new InternalError("Validation of virtual server " + vsrv.getName() + " was incorrect.");
			} else if (n instanceof VirtualSwitch) {
				final VirtualSwitch vsw = (VirtualSwitch) n;
				if (host == null) {
					if (vsw.getHost() == null) {
						continue;
					}
				} else {
					if (vsw.getHost() != null && host.equals(vsw.getHost().getNetwork())) {
						continue;
					}
				}
				throw new InternalError("Validation of virtual switch " + vsw.getName() + " was incorrect.");
			}
		}

		for (final Link l : vNet.getLinks()) {
			final VirtualLink vl = (VirtualLink) l;

			if (vl.getBandwidth() < 0) {
				throw new InternalError("Normal bandwidth of link " + vl.getName() + " was smaller than zero.");
			}

			if (host == null && vl.getHost() == null) {
				continue;
			} else {
				if (vl.getHost() instanceof SubstrateLink) {
					final SubstrateLink lHost = vl.getHost();
					if (host.equals(lHost.getNetwork())) {
						continue;
					}
				}
			}
			throw new InternalError("Validation of virtual link " + vl.getName() + " was incorrect.");
		}
	}

	/**
	 * Checks if a given virtual network (ID) is currently in a floating state. A
	 * floating state is given if a substrate server hosting at least one component
	 * of the virtual network was removed in a "dirty" way. (This method checks
	 * every virtual element for valid substrate hosting, BTW.)
	 *
	 * @param vNet The virtual network to check floating state for.
	 * @return True if virtual network is in a floating state.
	 */
	public boolean checkIfFloating(final VirtualNetwork vNet) {
		for (final Node n : vNet.getNodes()) {
			if (n instanceof VirtualServer) {
				final VirtualServer vsrv = (VirtualServer) n;
				if (vsrv.getHost() == null || vsrv.getHost().getNetwork() == null
						|| !vsrv.getHost().getNetwork().getNodes().contains(vsrv.getHost())) {
					return true;
				}
			} else if (n instanceof VirtualSwitch) {
				final VirtualSwitch vsw = (VirtualSwitch) n;
				if ((vsw.getHost().getNetwork() == null)
						|| !vsw.getHost().getNetwork().getNodes().contains(vsw.getHost())) {
					return true;
				}
			}
		}

		for (final Link l : vNet.getLinks()) {
			final VirtualLink vl = (VirtualLink) l;
			if (vl.getHost() instanceof SubstrateLink) {
				final SubstrateLink host = vl.getHost();
				if ((host.getNetwork() == null) || !host.getNetwork().getLinks().contains(host)) {
					return true;
				}
			}
		}

		return false;
	}

}
