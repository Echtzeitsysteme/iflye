package visualization;

import java.util.HashSet;
import java.util.Set;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import facade.ModelFacade;

/**
 * Visualization UI based on GraphStream, that can show network topologies based
 * on models read from XMI files.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class Ui {
	
	/**
	 * Servers (nodes) loaded from model.
	 */
	final static Set<model.Node> servers = new HashSet<model.Node>();
	
	/**
	 * Switches (nodes) loaded from model.
	 */
	final static Set<model.Node> switches = new HashSet<model.Node>();
	
	/**
	 * Links (edges) loaded from model.
	 */
	final static Set<model.Link> links = new HashSet<model.Link>();

	/**
	 * Main method that starts the visualization process.
	 * 
	 * @param args First string is the path of the model to load and second string is the name/ID
	 * of the network to visualize.
	 */
	public static void main(final String[] args) {
		System.setProperty("org.graphstream.ui", "swing");
		readModel(args[0], args[1]);
		//readModel("../examples/model.xmi", "sub");
		
		// Create the graph
		final Graph graph = new SingleGraph("network model visualizer");
		graph.setAttribute("ui.quality");
		graph.setAttribute("ui.antialias");
		
		// Add all server nodes
		for (final model.Node srv : servers) {
			final Node srvNode = graph.addNode(srv.getName());
			srvNode.setAttribute("ui.label", srv.getName());
			srvNode.setAttribute("ui.style", "fill-color: rgb(000,155,000);"
					+ "stroke-color: rgb(0,0,0);"
					+ "stroke-width: 1px;"
					+ "stroke-mode: plain;"
					+ "text-size: 10;"
					+ "size: 40px;"
					+ "text-style: bold;");
		}
		
		// Add all switch nodes
		for (final model.Node sw : switches) {
			final Node swNode = graph.addNode(sw.getName());
			swNode.setAttribute("ui.label", sw.getName());
			swNode.setAttribute("ui.style", "fill-color: rgb(255,255,255); "
					+ "shape: rounded-box; "
					+ "stroke-color: rgb(000,155,000); "
					+ "stroke-width: 4px; "
					+ "stroke-mode: plain; "
					+ "text-size: 10; "
					+ "size: 40px; "
					+ "text-style: bold;");
		}
		
		// Add all link edges
		for (final model.Link l : links) {
			final Edge lnEdge = graph.addEdge(l.getName(), l.getSource().getName(),
					l.getTarget().getName(), true);
			//lnEdge.setAttribute("ui.label", l.getName());
		}
		
		final Viewer viewer = graph.display();
		//viewer.disableAutoLayout();
	}
	
	/**
	 * Reads a model with given network ID from given file path.
	 * 
	 * @param path Path to read file from.
	 * @param networkId Network ID of the network to visualize.
	 */
	private static void readModel(final String path, final String networkId) {
		ModelFacade.getInstance().loadModel(path);
		
		// Servers
		servers.addAll(ModelFacade.getInstance().getAllServersOfNetwork(networkId));
		
		// Switches
		switches.addAll(ModelFacade.getInstance().getAllSwitchesOfNetwork(networkId));
		
		// Links
		links.addAll(ModelFacade.getInstance().getAllLinksOfNetwork(networkId));
	}

}
