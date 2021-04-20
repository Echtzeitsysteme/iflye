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
import model.Status;
import model.Switch;
import model.VirtualNetwork;

public class ModelFacade {
	
	private static ModelFacade instance;
	private static AtomicInteger counter = new AtomicInteger();
	
	private ModelFacade() {}
	
	public static synchronized ModelFacade getInstance() {
		if (ModelFacade.instance == null) {
			ModelFacade.instance = new ModelFacade();
		}
		return ModelFacade.instance;
	}

	private final Root root = ModelFactory.eINSTANCE.createRoot();
	
	public void dummy() {
		System.out.println("=> Dummy method called.");
	}
	
	public Collection<Network> getAllNetworks() {
		return root.getNetworks();
	}
	
	public Collection<Node> getAllServersOfNetwork(final String networkId) {
		checkStringValid(networkId);
		
		for (Network actNet : getAllNetworks()) {
			if (actNet.getName().equals(networkId)) {
				return actNet.getNodes().stream()
						.filter(n -> n instanceof Server)
						.collect(Collectors.toList());
			}
		}
		
		return null;
	}
	
	public Network getNetworkById(final String id) {
		checkStringValid(id);
		
		return (Network) root.getNetworks().stream()
				.filter(n -> n.getName() == id)
				.collect(Collectors.toList()).get(0);
	}
	
	public boolean networkExists(final String id) {
		checkStringValid(id);
		
		return getAllNetworks().stream()
		.filter(n -> n.getName().equals(id))
		.collect(Collectors.toList()).size() != 0;
	}
	
	public Server getServerById(final String id) {
		checkStringValid(id);
		return (Server) getNodeById(id);
	}
	
	public Node getNodeById(final String id) {
		checkStringValid(id);
		
		List<Network> nets = root.getNetworks();
		List<Node> nodes = new ArrayList<Node>();
		nets.stream()
		.forEach(net -> {
			net.getNodes().stream()
			.filter(n -> n instanceof Node)
			.forEach(n -> nodes.add(n));
		});
		return nodes.stream()
				.filter(n -> n.getName().equals(id))
				.collect(Collectors.toList()).get(0);
	}
	
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
		server.setStatus(Status.ACTIVE);
		return net.getNodes().add(server);
	}
	
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
		sw.setStatus(Status.ACTIVE);
		
		return net.getNodes().add(sw);
	}
	
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
	
	public boolean doesNodeIdExist(final String id, final String networkId) {
		checkStringValid(new String[] {id, networkId});
		
		return !getNetworkById(networkId).getNodes().stream()
				.filter(n -> n.getName().equals(id))
				.collect(Collectors.toList()).isEmpty();
	}
	
	public boolean doesLinkIdExist(final String id, final String networkId) {
		checkStringValid(id);
		checkStringValid(networkId);
		
		return !getNetworkById(networkId).getLinks().stream()
				.filter(l -> l.getName().equals(id))
				.collect(Collectors.toList()).isEmpty();
	}
	
	public void resetAll() {
		root.getNetworks().clear();
	}
	
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
	
	public void checkIntValid(final int... vals) {
		if (vals == null) {
			throw new IllegalArgumentException("Provided int(-array) was null!");
		}
		
		for (int val : vals) {
			if (val < 0) {
				throw new IllegalArgumentException("Provided int was smaller than zero!");
			}
		}
	}
	
	public int getNextId() {
		return counter.getAndIncrement();
	}
	
	public void persistModel() {
		eMoflonEMFUtil.saveModel(root, "./export.xmi");
	}
	
}
