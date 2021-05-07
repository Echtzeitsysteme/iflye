package facade.pathgen;

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
public class Dijkstra implements IPathGen {

  /**
   * Mapping: Node -> Distance.
   */
  private final Map<SubstrateNode, Integer> dists = new HashMap<SubstrateNode, Integer>();

  /**
   * Mapping: Node -> Previous node.
   */
  private final Map<SubstrateNode, SubstrateNode> prevs =
      new HashMap<SubstrateNode, SubstrateNode>();

  /**
   * List of all nodes.
   */
  private final Set<SubstrateNode> nodes = new HashSet<SubstrateNode>();

  /**
   * Starts the whole algorithm for a given substrate network and one given substrate node as start.
   * 
   * @param net SubstrateNetwork to generate all paths for.
   * @param start SubstrateNode to start with.
   * @return List of nodes with all previous visited nodes.
   */
  private List<Node> dijkstra(final SubstrateNetwork net, final SubstrateNode start) {
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
  private void init(final SubstrateNetwork net, final SubstrateNode start) {
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
  private SubstrateNode getSmallestDistNode() {
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
  private void distanceUpdate(final SubstrateNode u, final SubstrateNode v) {
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
  private List<SubstrateLink> shortestPath(final SubstrateNode target) {
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
   * Searches for a link in a given collection that has the given substrate node as source node.
   * 
   * @param source Source node to search for.
   * @param links Collection of links to search the one with corresponding source node in.
   * @return SubstrateLink found in the collection with given source node.
   */
  private SubstrateLink getLinkFrom(final SubstrateNode source, final Collection<Link> links) {
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
  @Override
  public Map<SubstrateNode, List<SubstrateLink>> getAllFastestPaths(final SubstrateNetwork net,
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

  @Override
  public Map<SubstrateNode, List<List<SubstrateLink>>> getAllKFastestPaths(SubstrateNetwork net,
      SubstrateNode start, int K) {
    if (K != 1) {
      throw new UnsupportedOperationException(
          "Due to its nature, the Dijkstra algorithm is only able to calculate the K=1 fastest "
              + "paths for all nodes.");
    }

    final Map<SubstrateNode, List<List<SubstrateLink>>> paths =
        new HashMap<SubstrateNode, List<List<SubstrateLink>>>();
    dijkstra(net, start);

    for (final Node n : net.getNodes()) {
      SubstrateNode sn = (SubstrateNode) n;
      if (!sn.equals(start)) {
        final List<List<SubstrateLink>> act = new LinkedList<List<SubstrateLink>>();
        act.add(shortestPath(sn));
        paths.put(sn, act);
      }
    }

    return paths;
  }

}
