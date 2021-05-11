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
  private final static List<model.Node> servers = new LinkedList<model.Node>();

  /**
   * Switches (nodes) loaded from model.
   */
  private final static List<model.Node> switches = new LinkedList<model.Node>();

  /**
   * Links (edges) loaded from model.
   */
  private final static Set<model.Link> links = new HashSet<model.Link>();

  /**
   * Network ID.
   */
  private static String subNetworkId;

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
    double srvCurrX = (-servers.size() + 1) * SCALE_X / 2;
    for (final model.Node srv : servers) {
      final Node srvNode = graph.addNode(srv.getName());
      srvNode.setAttribute("ui.label", removeNetworkId(srv.getName()));
      srvNode.setAttribute("ui.style",
          "fill-color: rgb(000,155,000);" + "stroke-color: rgb(0,0,0);" + "stroke-width: 1px;"
              + "stroke-mode: plain;" + "text-size: 8;" + "size: 40px;" + "text-style: bold;");

      // Placement of the server
      srvNode.setAttribute("xyz", srvCurrX, -srv.getDepth() * SCALE_Y, 0);
      srvCurrX += SCALE_X;
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
      xMap.put(i, (-depthCounters.get(i) + 1) * SCALE_X / 2);
    }

    // Add all switch nodes to graph
    for (final model.Node sw : switches) {
      final Node swNode = graph.addNode(sw.getName());
      swNode.setAttribute("ui.label", removeNetworkId(sw.getName()));
      swNode.setAttribute("ui.style",
          "fill-color: rgb(255,255,255); " + "shape: rounded-box; "
              + "stroke-color: rgb(000,155,000); " + "stroke-width: 4px; " + "stroke-mode: plain; "
              + "text-size: 8; " + "size: 40px; " + "text-style: bold;");

      // Placement of the switch
      final double currX = xMap.get(sw.getDepth());
      swNode.setAttribute("xyz", currX, -sw.getDepth() * SCALE_Y, 0);
      xMap.replace(sw.getDepth(), currX + SCALE_X);
    }

    // Add all link edges
    for (final model.Link l : links) {
      if (LINK_BIDIRECTIONAL) {
        try {
          graph.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(), false);
        } catch (final EdgeRejectedException ex) {
          // Graphstream rejects 'addEdge' if there already is an undirected edge from a to b.
          // Using the catch clause, the program does not have to track which edges were already
          // created.
        }
      } else {
        final Edge lnEdge =
            graph.addEdge(l.getName(), l.getSource().getName(), l.getTarget().getName(), true);
        // lnEdge.setAttribute("ui.label", l.getName());
      }
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
