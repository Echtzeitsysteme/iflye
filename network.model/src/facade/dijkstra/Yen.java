package facade.dijkstra;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import facade.ModelFacade;
import model.Link;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;

/**
 * Yen path finding algorithm that is used to generate the shortest n paths for all models. Uses the
 * base Dijkstra implementation. Heavily based on this Wikipedia article:
 * https://en.wikipedia.org/wiki/Yen%27s_algorithm
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class Yen {

  private static List<List<SubstrateNode>> yen(final SubstrateNetwork net,
      final SubstrateNode source, final SubstrateNode target, final int K) {

    // K shortest paths
    final List<List<SubstrateNode>> A = new LinkedList<List<SubstrateNode>>();

    // Determine the shortest path from the source to the sink.
    // No need to ignore any nodes or links here
    Dijkstra2.dijkstra(net, source, new HashSet<SubstrateNode>(), new HashSet<SubstrateLink>());
    A.add(0, Dijkstra2.shortestPathNodes(target));

    // Initialize the set to store the potential kth shortest path.
    final List<List<SubstrateNode>> B = new LinkedList<List<SubstrateNode>>();

    for (int k = 1; k <= K; k++) {
      // The spur node ranges from the first node to the next to last node in the previous
      // k-shortest path.
      for (int i = 0; i <= A.get(k - 1).size() - 2; i++) {
        // Setup for the nodes and links to ignore
        final Set<SubstrateNode> ignoredNodes = new HashSet<SubstrateNode>();
        final Set<SubstrateLink> ignoredLinks = new HashSet<SubstrateLink>();

        // Spur node is retrieved from the previous k-shortest path, k − 1.
        final SubstrateNode spurNode = A.get(k - 1).get(i);
        // The sequence of nodes from the source to the spur node of the previous k-shortest path.
        final List<SubstrateNode> rootPath = A.get(k - 1).subList(0, i);

        for (final List<SubstrateNode> p : A) {
          if (rootPath.equals(p.subList(0, i))) {
            // Remove the links that are part of the previous shortest paths which share the same
            // root path.
            final Link toIgnore =
                ModelFacade.getInstance().getLinkFromSourceToTarget(p.get(i), p.get(i + 1));
            ignoredLinks.add((SubstrateLink) toIgnore);
          }
        }

        for (final SubstrateNode rootPathNode : rootPath) {
          // Except spurNode
          if (!rootPathNode.equals(spurNode)) {
            ignoredNodes.add(rootPathNode);
          }
        }

        // Calculate the spur path from the spur node to the sink.
        // Consider also checking if any spurPath found
        Dijkstra2.dijkstra(net, spurNode, ignoredNodes, ignoredLinks);
        final List<SubstrateNode> spurPath = Dijkstra2.shortestPathNodes(target);

        if (spurPath.isEmpty()) {
          throw new UnsupportedOperationException("Spur path was empty!");
        }

        // Entire path is made up of the root path and spur path.
        final List<SubstrateNode> totalPath = new LinkedList<SubstrateNode>();
        totalPath.addAll(rootPath);
        totalPath.addAll(spurPath);
        // Add the potential k-shortest path to the heap.
        if (!B.contains(totalPath)) {
          B.add(totalPath);
        }

        // Add back the edges and nodes that were removed from the graph.
        // restore edges to Graph;
        // restore nodes in rootPath to Graph;
        // The sets for ignored nodes and ignored links will implicit be restored by the loop.
      }

      if (B.isEmpty()) {
        // This handles the case of there being no spur paths, or no spur paths left.
        // This could happen if the spur paths have already been exhausted (added to A),
        // or there are no spur paths at all - such as when both the source and sink vertices
        // lie along a "dead end".
        break;
      }

      // Sort the potential k-shortest paths by cost.
      // Add the lowest cost path becomes the k-shortest path.
      // In fact we should rather use shift since we are removing the first element
      A.add(k, popBestCandidate(B));
    }

    return A;
  }

  /**
   * Finds the best candidate in the list of lists (equals the one with the lowest amount of nodes)
   * and pops it.
   * 
   * @param candidates List of substrate node lists.
   * @return Substrate node list with the smallest amount of nodes.
   */
  private static List<SubstrateNode> popBestCandidate(final List<List<SubstrateNode>> candidates) {
    int minHops = Integer.MAX_VALUE;
    List<SubstrateNode> candidate = null;

    for (final List<SubstrateNode> actCand : candidates) {
      if (actCand.size() < minHops) {
        minHops = actCand.size();
        candidate = actCand;
      }
    }

    candidates.remove(candidate);
    return candidate;
  }

}
