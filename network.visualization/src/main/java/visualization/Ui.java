package visualization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import facade.ModelFacade;

/**
 * Visualization UI based on GraphStream, that can show network topologies based on models read from
 * XMI files.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class Ui {

  /*
   * Configuration parameters.
   */

  /**
   * Scaling of the placement.
   */
  private static final double SCALE = 2;

  /**
   * Servers (nodes) loaded from model.
   */
  final static List<model.Node> servers = new LinkedList<model.Node>();

  /**
   * Switches (nodes) loaded from model.
   */
  final static List<model.Node> switches = new LinkedList<model.Node>();

  /**
   * Links (edges) loaded from model.
   */
  final static Set<model.Link> links = new HashSet<model.Link>();

  /**
   * Main method that starts the visualization process.
   * 
   * @param args First string is the path of the model to load, second string is the name/ID of the
   *        network to visualize, and third parameter (0/1) enables automatic shaping.
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
    double srvCurrX = (-servers.size() + 1) * SCALE / 2;
    for (final model.Node srv : servers) {
      final Node srvNode = graph.addNode(srv.getName());
      srvNode.setAttribute("ui.label", srv.getName());
      srvNode.setAttribute("ui.style",
          "fill-color: rgb(000,155,000);" + "stroke-color: rgb(0,0,0);" + "stroke-width: 1px;"
              + "stroke-mode: plain;" + "text-size: 10;" + "size: 40px;" + "text-style: bold;");

      // Placement of the server
      srvNode.setAttribute("xyz", srvCurrX, -srv.getDepth() * SCALE, 0);
      srvCurrX += SCALE;
    }

    // Calculate switch positions
    final Map<Integer, Double> xMap = new HashMap<Integer, Double>();
    final Map<Integer, Integer> depthCounters = new HashMap<Integer, Integer>();
    for (final model.Node sw : switches) {
      if (!depthCounters.containsKey(sw.getDepth())) {
        depthCounters.put(sw.getDepth(), 1);
      } else {
        depthCounters.replace(sw.getDepth(), depthCounters.get(sw.getDepth()) + 1);
      }
    }

    for (final Integer i : depthCounters.keySet()) {
      xMap.put(i, (-depthCounters.get(i) + 1) * SCALE / 2);
    }

    // Add all switch nodes to graph
    for (final model.Node sw : switches) {
      final Node swNode = graph.addNode(sw.getName());
      swNode.setAttribute("ui.label", sw.getName());
      swNode.setAttribute("ui.style",
          "fill-color: rgb(255,255,255); " + "shape: rounded-box; "
              + "stroke-color: rgb(000,155,000); " + "stroke-width: 4px; " + "stroke-mode: plain; "
              + "text-size: 10; " + "size: 40px; " + "text-style: bold;");

      // Placement of the switch
      final double currX = xMap.get(sw.getDepth());
      swNode.setAttribute("xyz", currX, -sw.getDepth() * SCALE, 0);
      xMap.replace(sw.getDepth(), currX + SCALE);
    }

    // Add all link edges
    for (final model.Link l : links) {
      final Edge lnEdge =
          graph.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(), true);
      // lnEdge.setAttribute("ui.label", l.getName());
    }

    final Viewer viewer = graph.display();

    if ("0".equals(args[2])) {
      viewer.disableAutoLayout();
    }
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
