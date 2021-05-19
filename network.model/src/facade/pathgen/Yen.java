package facade.pathgen;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import facade.ModelFacade;
import model.Link;
import model.Node;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;

/**
 * Yen path finding algorithm that is used to generate the shortest K paths for all models. Uses the
 * base Dijkstra implementation. Heavily based on this Wikipedia article:
 * https://en.wikipedia.org/wiki/Yen%27s_algorithm
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class Yen implements IPathGen {

  /**
   * Implementation of the path finding algorithm by Yen (see reference above). Finds the K shortest
   * paths from a source to a given target node in a given substrate network.
   * 
   * @param net Substrate network to work with.
   * @param source Source node for all paths to find.
   * @param target Target node for all paths to find.
   * @param K Parameter for the fastest K paths.
   * @return List of lists of substrate nodes which form the paths.
   */
  private List<List<SubstrateNode>> yen(final SubstrateNetwork net, final SubstrateNode source,
      final SubstrateNode target, final int K) {

    // K shortest paths
    final List<List<SubstrateNode>> A = new LinkedList<List<SubstrateNode>>();

    // Determine the shortest path from the source to the sink.
    // No need to ignore any nodes or links here.
    {
      final ExtendedDijkstra extendedDijkstra = new ExtendedDijkstra();
      extendedDijkstra.dijkstra(net, source, new HashSet<SubstrateNode>(),
          new HashSet<SubstrateLink>());
      A.add(0, extendedDijkstra.shortestPathNodes(target));
    }

    // Initialize the List to store the potential kth shortest paths.
    final List<List<SubstrateNode>> B = new LinkedList<List<SubstrateNode>>();

    for (int k = 1; k < K; k++) {
      // The spur node ranges from the first node to the next to last node in the previous
      // k-shortest path.
      for (int i = 0; i < A.get(k - 1).size() - 2; i++) {
        // Setup for the nodes and links to ignore
        final Set<SubstrateNode> ignoredNodes = new HashSet<SubstrateNode>();
        final Set<SubstrateLink> ignoredLinks = new HashSet<SubstrateLink>();

        // Spur node is retrieved from the previous k-shortest path, k − 1.
        final SubstrateNode spurNode = A.get(k - 1).get(i);
        // The sequence of nodes from the source to the spur node of the previous k-shortest path.
        final List<SubstrateNode> rootPath = A.get(k - 1).subList(0, i + 1);

        for (final List<SubstrateNode> p : A) {
          if (rootPath.equals(p.subList(0, i + 1))) {
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
        final ExtendedDijkstra extendedDijkstra = new ExtendedDijkstra();
        extendedDijkstra.dijkstra(net, spurNode, ignoredNodes, ignoredLinks);
        final List<SubstrateNode> spurPath = extendedDijkstra.shortestPathNodes(target);

        if (spurPath.isEmpty()) {
          // throw new UnsupportedOperationException("Spur path was empty!");
          // break;
          continue;
        }

        // Remove first node in spurPath, because it is the same as the spurNode and it is also
        // contained in rootPath.
        spurPath.remove(0);

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
  private List<SubstrateNode> popBestCandidate(List<List<SubstrateNode>> candidates) {
    // Sort candidates by length and then by IDs to ensure same order for every permutation of
    // the input list.
    sortCandidates(candidates);

    // After sorting as described above, the search can be replaced by popping the first one.
    return candidates.remove(0);
  }

  /**
   * Sorts a list of given candidates by their size and by their node names.
   * 
   * @param candidates List of lists of substrate nodes to sort.
   */
  private void sortCandidates(List<List<SubstrateNode>> candidates) {
    Collections.sort(candidates, new Comparator<List<SubstrateNode>>() {
      /**
       * Sorts to given lists of substrate nodes according to: (1) Smaller list size first. (2)
       * Alphabetical order of node names.
       */
      @Override
      public int compare(final List<SubstrateNode> o1, final List<SubstrateNode> o2) {
        if (o1.size() == o2.size()) {
          for (int i = 1; i < o1.size(); i++) {
            if (!o1.get(i).equals(o2.get(i))) {
              return o1.get(i).getName().compareTo(o2.get(i).getName());
            }
          }
          return 0;
        } else {
          return o2.size() - o1.size();
        }
      }
    });
  }

  /**
   * Calculates and returns the first K fastest paths from a given start node in a given network.
   * (More specific: It calculates the first K paths with the smallest amount of hops from the start
   * node to all other nodes.) This method returns a map of all substrate nodes mapped to a list of
   * of lists of substrate links from start node to the key of the map.
   * 
   * @param net Network to search all paths for.
   * @param start SubstrateNode as start/source node of all paths.
   * @param K Create first K paths.
   * @return Map of SubstrateNodes to lists of lists of SubstrateLinks that form the corresponding
   *         paths.
   */
  @Override
  public Map<SubstrateNode, List<List<SubstrateLink>>> getAllKFastestPaths(
      final SubstrateNetwork net, final SubstrateNode start, final int K) {
    final Map<SubstrateNode, List<List<SubstrateLink>>> paths =
        new HashMap<SubstrateNode, List<List<SubstrateLink>>>();

    // Iterate over all nodes and execute path generation if the current node is not the start node
    for (final Node n : net.getNodes()) {
      SubstrateNode sn = (SubstrateNode) n;
      if (!sn.equals(start)) {
        final List<List<SubstrateNode>> candidates = yen(net, start, sn, K);
        paths.put(sn, translateAll(candidates));
      }
    }

    return paths;
  }

  @Override
  public Map<SubstrateNode, List<SubstrateLink>> getAllFastestPaths(final SubstrateNetwork net,
      final SubstrateNode start) {
    final Map<SubstrateNode, List<SubstrateLink>> output =
        new HashMap<SubstrateNode, List<SubstrateLink>>();

    final Map<SubstrateNode, List<List<SubstrateLink>>> kOutput =
        getAllKFastestPaths(net, start, 1);

    for (final SubstrateNode sn : kOutput.keySet()) {
      output.put(sn, kOutput.get(sn).get(0));
    }

    return output;
  }

  /**
   * Translates a list of lists of substrate nodes to a list of lists of substrate links connecting
   * the nodes together. This one is a wrapper that calls {@link #translate(List)} for every item in
   * the outer list given.
   * 
   * @param input List of lists of substrate nodes to convert.
   * @return List of list of substrate links that form the path of all given substrate nodes.
   */
  private List<List<SubstrateLink>> translateAll(final List<List<SubstrateNode>> input) {
    final List<List<SubstrateLink>> output = new LinkedList<List<SubstrateLink>>();

    for (final List<SubstrateNode> act : input) {
      output.add(translate(act));
    }

    return output;
  }

  /**
   * Translates a list of substrate nodes to a list of substrate links connecting the nodes
   * together.
   * 
   * @param nodes List of substrate nodes to convert.
   * @return List of substrate links that form the path of all given substrate nodes.
   */
  private List<SubstrateLink> translate(final List<SubstrateNode> nodes) {
    final List<SubstrateLink> links = new LinkedList<SubstrateLink>();

    for (int i = 0; i < nodes.size() - 1; i++) {
      final Link l =
          ModelFacade.getInstance().getLinkFromSourceToTarget(nodes.get(i), nodes.get(i + 1));
      links.add((SubstrateLink) l);
    }

    return links;
  }

}
