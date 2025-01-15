package visualization;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import facade.ModelFacade;
import generators.config.GlobalGeneratorConfig;
import model.Link;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Visualization UI based on GraphStream, that can show network topologies based
 * on models read from XMI files.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class NetworkGraph extends SingleGraph {
	/*
	 * Configuration parameters.
	 */

	/**
	 * X scaling of the placement.
	 */
	private final double SCALE_X = 2;

	/**
	 * Y scaling of the placement.
	 */
	private final double SCALE_Y = 5;

	/**
	 * True if links should be displayed bidirectional.
	 */
	private final boolean LINK_BIDIRECTIONAL = true;
	
	/**
	 * The iflye ModelFacade for model interaction
	 */
	private ModelFacade model;

	/**
	 * Network ID.
	 */
	private String subNetworkId;

	public NetworkGraph(String id) {
		super(id);
	}

	/**
	 * Sets the model to use for rendering.
	 *
	 * @param model     The ModelFacade to access the model.
	 */
	public void setModel(final ModelFacade model) {
		this.model = model;
	}
	
	/**
	 * Sets the networkId to render.
	 *
	 * @param networkId Network ID of the network to visualize.
	 */
	public void setNetworkId(final String networkId) {
		this.subNetworkId = networkId;
	}

	/**
	 * Main method that starts the visualization process.
	 *
	 * @param model     The ModelFacade to access the model.
	 * @param networkId Network ID of the network to visualize.
	 */
	public void render(final ModelFacade model, final String networkId) {
		setModel(model);
		setNetworkId(networkId);
		render();
	}
	
	/**
	 * Main method that starts the visualization process.
	 *
	 * @param model     The ModelFacade to access the model.
	 */
	public void render(final ModelFacade model) {
		setModel(model);
		render();
	}

	/**
	 * Main method that starts the visualization process.
	 *
	 * @param networkId Network ID of the network to visualize.
	 */
	public void render(final String networkId) {
		setNetworkId(networkId);
		render();
	}
	
	/**
	 * Main method that starts the visualization process.
	 */
	public void render() {
		// Create the graph
		this.setAttribute("ui.quality");
		this.setAttribute("ui.antialias");
		
		// Servers 
		final List<model.Node> servers = new LinkedList<>(this.model.getAllServersOfNetwork(this.subNetworkId));
		renderServers(servers);

		// Switches
		final List<model.Node> switches = new LinkedList<>(this.model.getAllSwitchesOfNetwork(this.subNetworkId));
		renderSwitches(switches);

		// Links
		final Set<model.Link> links = new HashSet<>(this.model.getAllLinksOfNetwork(this.subNetworkId));
		renderLinks(links);
		
		// Virtual Links
		final Set<String> virtualNetworks = getEmbeddedVirtualNetworks(servers);
		renderVirtualLinks(virtualNetworks);
	}
	
	/**
	 * Place the servers and their respective guest servers/switches on the graph.
	 * 
	 * @param servers
	 */
	private void renderServers(final List<model.Node> servers) {
		// Add all server nodes to graph
		double srvCurrX = (-servers.size() + 1) * SCALE_X / 2;
		for (final model.Node srv : servers) {
			final Node srvNode = this.addNode(srv.getName());
			srvNode.setAttribute("ui.label", removeNetworkId(srv.getName()));
			srvNode.setAttribute("ui.style",
					"fill-color: rgb(000,155,000);" + "shape: rounded-box; " + "stroke-color: rgb(0,0,0);"
							+ "stroke-width: 1px;" + "stroke-mode: plain;" + "text-size: 10;" + "size: 50px;"
							+ "text-style: bold;");

			// Placement of the server
			srvNode.setAttribute("xyz", srvCurrX, -srv.getDepth() * SCALE_Y, 0);

			// Render Guests
			final SubstrateServer ssrv = (SubstrateServer) srv;
			final double[] coordinates = nodeToCoordinates(srvNode);

			renderGuestServers(ssrv.getGuestServers(), coordinates);
			renderGuestSwitchesOnServer(ssrv.getGuestSwitches(), coordinates);

			srvCurrX += SCALE_X;
		}
	}
	
	/**
	 * Place the guest servers of a server on the graph.
	 * 
	 * @param guestServers
	 * @param coordinates
	 */
	private void renderGuestServers(final Collection<VirtualServer> guestServers, final double[] coordinates) {
		int counter = 0;
		for (final VirtualServer gs : guestServers) {
			final String act = gs.getName();
			final Node vsrvNode = this.addNode(act);
			vsrvNode.setAttribute("ui.label", act.substring(act.indexOf("_") + 1));
			vsrvNode.setAttribute("ui.style",
					"fill-color: rgb(155,000,000);" + "shape: rounded-box; " + "stroke-color: rgb(0,0,0);"
							+ "stroke-width: 1px;" + "stroke-mode: plain;" + "text-size: 8;" + "size: 25px;"
							+ "text-style: bold;");
			vsrvNode.setAttribute("xyz", coordinates[0] - 0.75, coordinates[1] - 0.5 * counter, 0);
			counter++;
		}
	}
	
	/**
	 * Place the guest switches of a server on the graph.
	 * 
	 * @param guestSwitches
	 * @param coordinates
	 */
	private void renderGuestSwitchesOnServer(final Collection<VirtualSwitch> guestSwitches, final double[] coordinates) {
		int counter = 0;
		for (final VirtualSwitch gs : guestSwitches) {
			final String act = gs.getName();
			final Node vsrvNode = this.addNode(act);
			vsrvNode.setAttribute("ui.label", act.substring(act.indexOf("_") + 1));
			vsrvNode.setAttribute("ui.style",
					"fill-color: rgb(255,255,255); " + "stroke-color: rgb(155,000,000); " + "stroke-width: 4px; "
							+ "stroke-mode: plain; " + "text-size: 8; " + "size: 25px; " + "text-style: bold;");
			vsrvNode.setAttribute("xyz", coordinates[0] - 0.75, coordinates[1] - 0.5 * counter, 0);
			counter++;
		}
	}

	/**
	 * Place the switches and their respective guest switches on the graph.
	 * 
	 * @param switches
	 */
	private void renderSwitches(final List <model.Node> switches) {
		// Calculate switch positions
		final Map<Integer, Double> xMap = new HashMap<>();
		final Map<Integer, Integer> depthCounters = new HashMap<>();
		for (final model.Node sw : switches) {
			if (!depthCounters.containsKey(sw.getDepth())) {
				depthCounters.put(sw.getDepth(), 1);
			} else {
				depthCounters.replace(sw.getDepth(), depthCounters.get(sw.getDepth()) + 1);
			}
		}

		for (final Integer i : depthCounters.keySet()) {
			xMap.put(i, (-depthCounters.get(i) + 1) * SCALE_X / 2);
		}

		// Add all switch nodes to graph
		for (final model.Node sw : switches) {
			final Node swNode = this.addNode(sw.getName());
			swNode.setAttribute("ui.label", removeNetworkId(sw.getName()));
			swNode.setAttribute("ui.style",
					"fill-color: rgb(255,255,255); " + "stroke-color: rgb(000,155,000); " + "stroke-width: 4px; "
							+ "stroke-mode: plain; " + "text-size: 10; " + "size: 50px; " + "text-style: bold;");

			// Placement of the switch
			final double currX = xMap.get(sw.getDepth());
			swNode.setAttribute("xyz", currX, -sw.getDepth() * SCALE_Y, 0);
			xMap.replace(sw.getDepth(), currX + SCALE_X);

			// Guest switches
			final SubstrateSwitch ssw = (SubstrateSwitch) sw;
			final double[] coordinates = nodeToCoordinates(swNode);
			renderGuestSwitches(ssw.getGuestSwitches(), coordinates);
		}
	}
	
	/**
	 * Place the guest switches of a native switch on the graph.
	 * 
	 * @param guestSwitches
	 * @param coordinates
	 */
	private void renderGuestSwitches(final Collection<VirtualSwitch> guestSwitches, final double[] coordinates) {
		int counter = 0;
		for (final VirtualSwitch gs : guestSwitches) {
			final String act = gs.getName();
			final Node vswNode = this.addNode(act);
			vswNode.setAttribute("ui.label", act.substring(act.indexOf("_") + 1));
			vswNode.setAttribute("ui.style",
					"fill-color: rgb(255,255,255); " + "stroke-color: rgb(155,000,000); "
							+ "stroke-width: 4px; " + "stroke-mode: plain; " + "text-size: 8; " + "size: 25px; "
							+ "text-style: bold;");

			vswNode.setAttribute("xyz", coordinates[0] - 0.75, coordinates[1] - 0.5 * counter, 0);
			counter++;
		}
	}

	/**
	 * Place the links between a network as edges on the graph.
	 * 
	 * @param links
	 */
	private void renderLinks(final Set<model.Link> links) {
		// Add all link edges
		for (final model.Link l : links) {
			if (LINK_BIDIRECTIONAL) {
				try {
					this.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(), false);
				} catch (final EdgeRejectedException ex) {
					// Graphstream rejects 'addEdge' if there already is an undirected edge from a
					// to b. Using the catch clause, the program does not have to track which edges
					// were already created.
				}
			} else {
				final Edge lnEdge = this.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(), true);
				// lnEdge.setAttribute("ui.label", l.getName());
			}
		}
	}
	
	/**
	 * Get all the networks that are embedded on components of this network.
	 * 
	 * @param servers
	 * @return
	 */
	private Set<String> getEmbeddedVirtualNetworks(final List<model.Node> servers) {
		return servers
				.stream()
				.flatMap((srv) -> {
					final SubstrateServer ssrv = (SubstrateServer) srv;
					
					return Stream
							.concat(
								ssrv.getGuestServers().stream().map((gs) -> gs.getNetwork().getName()),
								ssrv.getGuestSwitches().stream().map((gs) -> gs.getNetwork().getName())
							)
							.distinct();
				})
				.collect(Collectors.toSet());
	}
	
	/**
	 * Place the links of a virtual network on the graph.
	 * 
	 * @param virtualNetworks
	 */
	private void renderVirtualLinks(final Set<String> virtualNetworks) {
		for (final String virtualNetworkId : virtualNetworks) {
			for (final Link l : this.model.getAllLinksOfNetwork(virtualNetworkId)) {
				if (LINK_BIDIRECTIONAL) {
					try {
						final Edge lnEdge = this.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(),
								false);
						lnEdge.setAttribute("ui.style", "fill-color: rgb(155,000,000); shape: cubic-curve;");
					} catch (final EdgeRejectedException ex) {
						// Graphstream rejects 'addEdge' if there already is an undirected edge from a
						// to b. Using the catch clause, the program does not have to track which edges
						// were already created.
					}
				} else {
					final Edge lnEdge = this.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(),
							true);
					lnEdge.setAttribute("ui.style", "fill-color: rgb(155,000,000); shape: cubic-curve;");
				}
			}
		}
	}

	/**
	 * Extracts the coordinates for a given node as double[].
	 *
	 * @param node Input node to extract coordinates for.
	 * @return Array of doubles with the coordinates.
	 */
	private double[] nodeToCoordinates(final Node node) {
		final Object[] coordinatesObj = (Object[]) node.getAttribute("xyz");
		
		return Arrays.stream(coordinatesObj)
	        .mapToDouble(num -> num instanceof Integer ? ((Integer) num).doubleValue() : (double) num)
	        .toArray();
	}

	/**
	 * Removes the saved substrate network ID from a given name.
	 *
	 * @param name Input name.
	 * @return Given name without the saved substrate network ID.
	 */
	private String removeNetworkId(final String name) {
		return name.replace(this.subNetworkId + GlobalGeneratorConfig.SEPARATOR, "");
	}

}
