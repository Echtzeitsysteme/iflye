package visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import facade.ModelFacade;

/**
 * Visualization UI based on GraphStream, that can show network topologies based
 * on models read from XMI files.
 *
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class Application extends JFrame {
	
	private static final long serialVersionUID = -2863860255459271789L;

	/**
	 * The NetworkGraph to render in the Frame
	 */
	private final NetworkGraph graph = new NetworkGraph("Network Viewer");

	/**
	 * Main method that starts the visualization process.
	 *
	 * @param args First string is the path of the model to load, second string is
	 *             the name/ID of the network to visualize, and third parameter
	 *             (0/1) disables automatic shaping (enabled by default).
	 */
	public static void main(final String[] args) {
		new Application(args[0], args[1], args.length < 3 || !"0".equals(args[2]));
	}
	
	/**
	 * Creates a new UI application with an embedded NetworkGraph for visualization.
	 * 
	 * @param path       Path to read file from.
	 * @param networkId  Network ID of the network to visualize.
	 */
	public Application(final String path, final String networkId) {
		this(path, networkId, true);
	}
	
	/**
	 * Creates a new UI application with an embedded NetworkGraph for visualization.
	 * 
	 * @param path       Path to read file from.
	 * @param networkId  Network ID of the network to visualize.
	 * @param autoLayout If the graph should be automatically laid out
	 */
	public Application(final String path, final String networkId, final boolean autoLayout) {
		System.setProperty("org.graphstream.ui", "swing");

		this.setTitle("Network Viewer: " + networkId);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(500, 600));
		this.setSize(getPreferredSize());
		this.setBackground(Color.lightGray);

		ModelFacade.getInstance().loadModel(path);
		graph.render(ModelFacade.getInstance(), networkId);
        SwingViewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        
        if (!autoLayout) {
    		viewer.disableAutoLayout();
    	} else {
    		viewer.enableAutoLayout();
    	}

		View view = viewer.addDefaultView(false);   // false indicates "no JFrame".
		this.add((DefaultView) view, BorderLayout.CENTER);
		
		this.setVisible(true);
	}

}
