package visualization;

import java.util.HashSet;
import java.util.Set;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import facade.ModelFacade;

public class Ui {
	
	final Set<Node> nodes = new HashSet<Node>();
	final Set<Edge> edges = new HashSet<Edge>();
	
	final static Set<model.Node> servers = new HashSet<model.Node>();
	final static Set<model.Node> switches = new HashSet<model.Node>();
	final static Set<model.Link> links = new HashSet<model.Link>();

	public static void main(final String[] args) {
		System.setProperty("org.graphstream.ui", "swing");
//		readModel(args[0], args[1]);
		readModel("model.xmi", "sub");
		
		final Graph graph = new SingleGraph("network.model visualizer");
		graph.setAttribute("ui.quality");
		graph.setAttribute("ui.antialias");
		
		for (final model.Node srv : servers) {
			final Node srvNode = graph.addNode(srv.getName());
			srvNode.setAttribute("ui.label", srv.getName());
			srvNode.setAttribute("ui.style", "fill-color: rgb(000,155,000);"
					+ "text-size: 12;"
					+ "size: 40px;"
					+ "text-style: bold;");
		}
		
		for (final model.Node sw : switches) {
			final Node swNode = graph.addNode(sw.getName());
			swNode.setAttribute("ui.label", sw.getName());
			swNode.setAttribute("ui.style", "fill-color: rgb(255,255,255); "
					+ "shape: rounded-box; "
					+ "stroke-color: rgb(000,155,000); "
					+ "stroke-width: 4px; "
					+ "stroke-mode: plain; "
					+ "text-size: 12; "
					+ "size: 40px; "
					+ "text-style: bold;");
		}
		
		for (final model.Link l : links) {
			final Edge lnEdge = graph.addEdge(l.getName(), l.getSource().getName(),
					l.getTarget().getName(), true);
			lnEdge.setAttribute("ui.label", l.getName());
		}
		
		final Viewer viewer = graph.display();
		//viewer.disableAutoLayout();
	}
	
	/**
	 * TODO
	 * 
	 * @param path
	 * @param networkId
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
