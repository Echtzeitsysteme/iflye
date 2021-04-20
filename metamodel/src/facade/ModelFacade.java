package facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
		return (Network) root.getNetworks().stream()
				.filter(n -> n.getName() == id)
				.collect(Collectors.toList()).get(0);
	}
	
	public boolean networkExists(final String id) {
		return getAllNetworks().stream()
		.filter(n -> n.getName().equals(id))
		.collect(Collectors.toList()).size() != 0;
	}
	
	public Server getServerById(final String id) {
		List<Network> nets = root.getNetworks();
		List<Server> servers = new ArrayList<Server>();
		nets.stream()
		.forEach(net -> {
			net.getNodes().stream()
			.filter(s -> s instanceof Server)
			.forEach(s -> servers.add((Server) s));
		});
		return servers.stream()
				.filter(s -> s.getName().equals(id))
				.collect(Collectors.toList()).get(0);
	}
	
	public Node getNodeById(final String id) {
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
		if (networkExists(id)) {
			throw new IllegalArgumentException("A network with id " + id + " already exists!");
		}
		
		final Network net = ModelFactory.eINSTANCE.createSubstrateNetwork();
		net.setName(id);
		return root.getNetworks().add(net);
	}
	
	public boolean addServerToNetwork(final String id, final String networkId, final int cpu, 
			final int memory, final int storage, final int depth) {
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
}
