package visualization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

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
public class Ui {
	/*
	 * Configuration parameters.
	 */

	/**
	 * X scaling of the placement.
	 */
	private static final double SCALE_X = 2;

	/**
	 * Y scaling of the placement.
	 */
	private static final double SCALE_Y = 5;

	/**
	 * True if links should be displayed bidirectional.
	 */
	private static final boolean LINK_BIDIRECTIONAL = true;

	/**
	 * Servers (nodes) loaded from model.
	 */
	private final static List<model.Node> servers = new LinkedList<>();

	/**
	 * Switches (nodes) loaded from model.
	 */
	private final static List<model.Node> switches = new LinkedList<>();

	/**
	 * Links (edges) loaded from model.
	 */
	private final static Set<model.Link> links = new HashSet<>();

	/**
	 * Network ID.
	 */
	private static String subNetworkId;

	private static Map<Node, List<String>> serverToServerMappings = new HashMap<>();
	private static Map<Node, List<String>> switchToServerMappings = new HashMap<>();
	private static Map<Node, List<String>> switchMappings = new HashMap<>();
	private static Set<String> virtualNetworks = new HashSet<>();

	/**
	 * Main method that starts the visualization process.
	 *
	 * @param args First string is the path of the model to load, second string is
	 *             the name/ID of the network to visualize, and third parameter
	 *             (0/1) enables automatic shaping.
	 */
	public static void main(final String[] args) {
		System.setProperty("org.graphstream.ui", "swing");
		readModel(args[0], args[1]);
		// readModel("../examples/model.xmi", "sub");

		// Create the graph
		final Graph graph = new SingleGraph("network model visualizer");
		graph.setAttribute("ui.quality");
		graph.setAttribute("ui.antialias");

		// Add all server nodes to graph
		double srvCurrX = (-servers.size() + 1) * SCALE_X / 2;
		for (final model.Node srv : servers) {
			final Node srvNode = graph.addNode(srv.getName());
			srvNode.setAttribute("ui.label", removeNetworkId(srv.getName()));
			srvNode.setAttribute("ui.style",
					"fill-color: rgb(000,155,000);" + "stroke-color: rgb(0,0,0);" + "stroke-width: 1px;"
							+ "stroke-mode: plain;" + "text-size: 10;" + "size: 50px;" + "text-style: bold;");

			// Placement of the server
			srvNode.setAttribute("xyz", srvCurrX, -srv.getDepth() * SCALE_Y, 0);

			// Guest servers
			final SubstrateServer ssrv = (SubstrateServer) srv;
			for (final VirtualServer gs : ssrv.getGuestServers()) {
				if (!serverToServerMappings.containsKey(srvNode)) {
					serverToServerMappings.put(srvNode, new LinkedList<String>());
				}
				serverToServerMappings.get(srvNode).add(gs.getName());
				virtualNetworks.add(gs.getNetwork().getName());
			}
			// Guest switches
			for (final VirtualSwitch gs : ssrv.getGuestSwitches()) {
				if (!switchToServerMappings.containsKey(srvNode)) {
					switchToServerMappings.put(srvNode, new LinkedList<String>());
				}
				switchToServerMappings.get(srvNode).add(gs.getName());
				virtualNetworks.add(gs.getNetwork().getName());
			}

			srvCurrX += SCALE_X;
		}

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
			final Node swNode = graph.addNode(sw.getName());
			swNode.setAttribute("ui.label", removeNetworkId(sw.getName()));
			swNode.setAttribute("ui.style",
					"fill-color: rgb(255,255,255); " + "shape: rounded-box; " + "stroke-color: rgb(000,155,000); "
							+ "stroke-width: 4px; " + "stroke-mode: plain; " + "text-size: 10; " + "size: 50px; "
							+ "text-style: bold;");

			// Placement of the switch
			final double currX = xMap.get(sw.getDepth());
			swNode.setAttribute("xyz", currX, -sw.getDepth() * SCALE_Y, 0);
			xMap.replace(sw.getDepth(), currX + SCALE_X);

			// Guest switches
			final SubstrateSwitch ssw = (SubstrateSwitch) sw;
			for (final VirtualSwitch gs : ssw.getGuestSwitches()) {
				if (!switchMappings.containsKey(swNode)) {
					switchMappings.put(swNode, new LinkedList<String>());
				}
				switchMappings.get(swNode).add(gs.getName());
			}
		}

		// Add all link edges
		for (final model.Link l : links) {
			if (LINK_BIDIRECTIONAL) {
				try {
					graph.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(), false);
				} catch (final EdgeRejectedException ex) {
					// Graphstream rejects 'addEdge' if there already is an undirected edge from a
					// to b.
					// Using the catch clause, the program does not have to track which edges were
					// already
					// created.
				}
			} else {
				final Edge lnEdge = graph.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(), true);
				// lnEdge.setAttribute("ui.label", l.getName());
			}
		}

		/*
		 * Guest visualization
		 */
		// Server to server mappings
		for (final Node key : serverToServerMappings.keySet()) {
			final double[] coordinates = nodeToCoordinates(key);
			int counter = 0;
			for (final String act : serverToServerMappings.get(key)) {
				final Node srvNode = graph.addNode(act);
				srvNode.setAttribute("ui.label", act.substring(act.indexOf("_") + 1));
				srvNode.setAttribute("ui.style",
						"fill-color: rgb(155,000,000);" + "stroke-color: rgb(0,0,0);" + "stroke-width: 1px;"
								+ "stroke-mode: plain;" + "text-size: 8;" + "size: 25px;" + "text-style: bold;");
				srvNode.setAttribute("xyz", coordinates[0] - 0.75, coordinates[1] - 0.5 * counter, 0);
				counter++;
			}
		}

		// Switch to server mappings
		for (final Node key : switchToServerMappings.keySet()) {
			final double[] coordinates = nodeToCoordinates(key);
			int counter = 0;
			for (final String act : switchToServerMappings.get(key)) {
				final Node srvNode = graph.addNode(act);
				srvNode.setAttribute("ui.label", act.substring(act.indexOf("_") + 1));
				srvNode.setAttribute("ui.style",
						"fill-color: rgb(255,255,255); " + "shape: rounded-box; " + "stroke-color: rgb(155,000,000); "
								+ "stroke-width: 4px; " + "stroke-mode: plain; " + "text-size: 8; " + "size: 25px; "
								+ "text-style: bold;");
				srvNode.setAttribute("xyz", coordinates[0] - 0.75, coordinates[1] - 0.5 * counter, 0);
				counter++;
			}
		}

		// Switch to switch mappings
		for (final Node key : switchMappings.keySet()) {
			final double[] coordinates = nodeToCoordinates(key);
			int counter = 0;
			for (final String act : switchMappings.get(key)) {
				final Node swNode = graph.addNode(act);
				swNode.setAttribute("ui.label", act.substring(act.indexOf("_") + 1));
				swNode.setAttribute("ui.style",
						"fill-color: rgb(255,255,255); " + "shape: rounded-box; " + "stroke-color: rgb(155,000,000); "
								+ "stroke-width: 4px; " + "stroke-mode: plain; " + "text-size: 8; " + "size: 25px; "
								+ "text-style: bold;");

				swNode.setAttribute("xyz", coordinates[0] - 0.75, coordinates[1] - 0.5 * counter, 0);
				counter++;
			}
		}

		// Links
		for (final String actNetId : virtualNetworks) {
			for (final Link l : ModelFacade.getInstance().getAllLinksOfNetwork(actNetId)) {
				if (LINK_BIDIRECTIONAL) {
					try {
						final Edge lnEdge = graph.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(),
								false);
						lnEdge.setAttribute("ui.style", "fill-color: rgb(155,000,000); shape: cubic-curve;");
					} catch (final EdgeRejectedException ex) {
						// Graphstream rejects 'addEdge' if there already is an undirected edge from a
						// to b.
						// Using the catch clause, the program does not have to track which edges were
						// already
						// created.
					}
				} else {
					final Edge lnEdge = graph.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(),
							true);
					lnEdge.setAttribute("ui.style", "fill-color: rgb(155,000,000); shape: cubic-curve;");
				}
			}
		}

		final Viewer viewer = graph.display();

		if ("0".equals(args[2])) {
			viewer.disableAutoLayout();
		}
	}

	private static double[] nodeToCoordinates(final Node node) {
		final Object[] coordinatesObj = (Object[]) node.getAttribute("xyz");
		final double[] coordinates = new double[3];
		for (int i = 0; i < coordinatesObj.length; i++) {
			if (coordinatesObj[i] instanceof Integer) {
				coordinates[i] = ((Integer) coordinatesObj[i]).doubleValue();
			} else {
				coordinates[i] = (double) coordinatesObj[i];
			}
		}
		return coordinates;
	}

	/**
	 * Reads a model with given network ID from given file path.
	 *
	 * @param path      Path to read file from.
	 * @param networkId Network ID of the network to visualize.
	 */
	private static void readModel(final String path, final String networkId) {
		Ui.subNetworkId = networkId;
		ModelFacade.getInstance().loadModel(path);

		// Servers
		servers.addAll(ModelFacade.getInstance().getAllServersOfNetwork(networkId));

		// Switches
		switches.addAll(ModelFacade.getInstance().getAllSwitchesOfNetwork(networkId));

		// Links
		links.addAll(ModelFacade.getInstance().getAllLinksOfNetwork(networkId));
	}

	/**
	 * Removes the saved substrate network ID from a given name.
	 *
	 * @param name Input name.
	 * @return Given name without the saved substrate network ID.
	 */
	private static String removeNetworkId(final String name) {
		return name.replace(subNetworkId + GlobalGeneratorConfig.SEPARATOR, "");
	}

}
