package facade.pathgen;

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
 * This is a slightly adapted version compared to the normal Dijkstra implementation. It gets a set
 * of nodes and links to ignore during the path finding process. This is a needed behavior, because
 * the Yen algorithm needs to delete nodes and links but the model itself should not be changed.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class Dijkstra2 {

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
  protected List<Node> dijkstra(final SubstrateNetwork net, final SubstrateNode start,
      final Set<SubstrateNode> ignoredNodes, final Set<SubstrateLink> ignoredLinks) {
    final List<Node> prev = new LinkedList<Node>();

    init(net, start, ignoredNodes);

    while (!nodes.isEmpty()) {
      final SubstrateNode u = getSmallestDistNode(ignoredNodes);

      // If no node with the smallest distance can be found, the graph is not fully connected ->
      // Break the loop and return.
      if (u == null) {
        break;
      }

      nodes.remove(u);

      for (final Link out : u.getOutgoingLinks()) {
        // Check that link gets ignored if its contained in the ignored links set
        if (ignoredLinks.contains(out)) {
          continue;
        }

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
  private void init(final SubstrateNetwork net, final SubstrateNode start,
      final Set<SubstrateNode> ignoredNodes) {
    for (final Node n : net.getNodes()) {
      final SubstrateNode sn = (SubstrateNode) n;

      // Check if sn must be ignored
      if (ignoredNodes.contains(sn)) {
        continue;
      }

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
  private SubstrateNode getSmallestDistNode(final Set<SubstrateNode> ignoredNodes) {
    int dist = Integer.MAX_VALUE;
    SubstrateNode nearest = null;

    for (final SubstrateNode n : nodes) {
      // Check if n must be ignored
      if (ignoredNodes.contains(n)) {
        continue;
      }

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
   * Returns a list of substrate nodes that form the shortest path from the global start to a given
   * target node.
   * 
   * @param target Target node to calculate path for.
   * @return List of substrate nodes that form the shortest path from start to target.
   */
  protected List<SubstrateNode> shortestPathNodes(final SubstrateNode target) {
    final List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
    SubstrateNode u = target;
    nodes.add(0, u);
    while (prevs.get(u) != null) {
      u = prevs.get(u);
      nodes.add(0, u);
    }

    return nodes;
  }

}
