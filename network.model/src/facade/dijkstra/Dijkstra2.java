package facade.dijkstra;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Link;
import model.Node;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;

/**
 * Dijkstra path finding algorithm that is used to generate the paths for all models. Heavily based
 * on this Wikipedia article: https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class Dijkstra2 {

  /**
   * Mapping: Node -> Distance.
   */
  final static Map<SubstrateNode, Integer> dists = new HashMap<SubstrateNode, Integer>();

  /**
   * Mapping: Node -> Previous node.
   */
  final static Map<SubstrateNode, SubstrateNode> prevs =
      new HashMap<SubstrateNode, SubstrateNode>();

  /**
   * List of all nodes.
   */
  final static Set<SubstrateNode> nodes = new HashSet<SubstrateNode>();

  /**
   * Private constructor to avoid instantiation.
   */
  private Dijkstra2() {}

  /**
   * Starts the whole algorithm for a given substrate network and one given substrate node as start.
   * 
   * @param net SubstrateNetwork to generate all paths for.
   * @param start SubstrateNode to start with.
   * @return List of nodes with all previous visited nodes.
   */
  protected static List<Node> dijkstra(final SubstrateNetwork net, final SubstrateNode start) {
    final List<Node> prev = new LinkedList<Node>();

    init(net, start);

    while (!nodes.isEmpty()) {
      final SubstrateNode u = getSmallestDistNode();
      nodes.remove(u);

      for (final Link out : u.getOutgoingLinks()) {
        SubstrateNode next = (SubstrateNode) out.getTarget();
        if (nodes.contains(next)) {
          distanceUpdate(u, next);
        }
      }
    }

    return prev;
  }

  /**
   * Initializes this Dijkstra algorithm class.
   * 
   * @param net SubstrateNetwork to use.
   * @param start SubstrateNode to use as a start.
   */
  private static void init(final SubstrateNetwork net, final SubstrateNode start) {
    for (final Node n : net.getNodes()) {
      final SubstrateNode sn = (SubstrateNode) n;
      dists.put(sn, Integer.MAX_VALUE);
      prevs.put(sn, null);
      nodes.add(sn);
    }

    dists.replace(start, 0);
  }

  /**
   * Returns the substrate node with the smallest distance from static collection nodes.
   * 
   * @return SubstrateNode with smallest distance.
   */
  private static SubstrateNode getSmallestDistNode() {
    int dist = Integer.MAX_VALUE;
    SubstrateNode nearest = null;

    for (final SubstrateNode n : nodes) {
      final int nDist = dists.get(n);
      if (nDist < dist) {
        nearest = n;
        dist = nDist;
      }
    }

    return nearest;
  }

  /**
   * Performs an update of the distance between to given substrate nodes. The value will be
   * incremented by 1.
   * 
   * @param u SubstrateNode u.
   * @param v SubstrateNode v.
   */
  private static void distanceUpdate(final SubstrateNode u, final SubstrateNode v) {
    final int alt = dists.get(u) + 1;

    if (alt < dists.get(v)) {
      dists.replace(v, alt);
      prevs.replace(v, u);
    }
  }

  /**
   * Returns a list of substrate links that form the shortest path from the global start to a given
   * target node.
   * 
   * @param target Target node to calculate path for.
   * @return List of substrate links that form the shortest path from start to target.
   */
  private static List<SubstrateLink> shortestPath(final SubstrateNode target) {
    final List<SubstrateLink> links = new LinkedList<SubstrateLink>();
    SubstrateNode u = target;
    while (prevs.get(u) != null) {
      final List<Link> uIngressLinks = u.getIncomingLinks();
      u = prevs.get(u);
      final SubstrateLink l = getLinkFrom(u, uIngressLinks);
      links.add(0, l);
    }

    return links;
  }

  /**
   * Returns a list of substrate nodes that form the shortest path from the global start to a given
   * target node.
   * 
   * @param target Target node to calculate path for.
   * @return List of substrate nodes that form the shortest path from start to target.
   */
  protected static List<SubstrateNode> shortestPathNodes(final SubstrateNode target) {
    final List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
    SubstrateNode u = target;
    nodes.add(0, u);
    while (prevs.get(u) != null) {
      u = prevs.get(u);
      nodes.add(0, u);
    }

    return nodes;
  }

  /**
   * Searches for a link in a given collection that has the given substrate node as source node.
   * 
   * @param source Source node to search for.
   * @param links Collection of links to search the one with corresponding source node in.
   * @return SubstrateLink found in the collection with given source node.
   */
  private static SubstrateLink getLinkFrom(final SubstrateNode source,
      final Collection<Link> links) {
    for (final Link l : links) {
      if (l.getSource().equals(source)) {
        return (SubstrateLink) l;
      }
    }

    return null;
  }

  /**
   * Calculates and returns all paths from a given start node in a given network. This method
   * returns a map of all substrate nodes mapped to a list of substrate link from start node to the
   * key of the map.
   * 
   * @param net Network to search all paths for.
   * @param start SubstrateNode as start/source node of all paths.
   * @return Map of SubstrateNodes to lists of SubstrateLinks that form the corresponding paths.
   */
  public static Map<SubstrateNode, List<SubstrateLink>> getAllPaths(final SubstrateNetwork net,
      final SubstrateNode start) {
    final Map<SubstrateNode, List<SubstrateLink>> paths =
        new HashMap<SubstrateNode, List<SubstrateLink>>();
    dijkstra(net, start);

    for (final Node n : net.getNodes()) {
      SubstrateNode sn = (SubstrateNode) n;
      if (!sn.equals(start)) {
        paths.put(sn, shortestPath(sn));
      }
    }

    return paths;
  }

}
