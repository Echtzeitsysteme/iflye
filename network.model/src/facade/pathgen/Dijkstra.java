package facade.pathgen;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import model.Link;
import model.Node;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;

/**
 * Dijkstra path finding algorithm that is used to generate the paths for all
 * models. Heavily based on this Wikipedia article:
 * https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class Dijkstra implements IPathGen {

	/**
	 * Mapping: Node -> Distance.
	 */
	protected final Map<SubstrateNode, Integer> dists = new HashMap<>();

	/**
	 * Mapping: Node -> Previous node.
	 */
	protected final Map<SubstrateNode, SubstrateNode> prevs = new HashMap<>();

	/**
	 * Set of all nodes.
	 */
	protected final Set<SubstrateNode> nodes = new HashSet<>();

	/**
	 * Priority queue of all nodes.
	 */
	protected final PriorityQueue<SubstrateNode> prioNodes = new PriorityQueue<>(new Comparator<SubstrateNode>() {
		@Override
		public int compare(final SubstrateNode o1, final SubstrateNode o2) {
			final int distFirst = dists.get(o1);
			final int distSecond = dists.get(o2);

			if (distFirst == distSecond) {
				return o1.getName().compareTo(o2.getName());
			} else {
				return distFirst - distSecond;
			}
		}
	});

	/**
	 * Starts the whole algorithm for a given substrate network and one given
	 * substrate node as start.
	 *
	 * @param net   SubstrateNetwork to generate all paths for.
	 * @param start SubstrateNode to start with.
	 */
	private void dijkstra(final SubstrateNetwork net, final SubstrateNode start) {
		init(net, start);

		while (!nodes.isEmpty()) {
			final SubstrateNode u = popSmallestDistNode();

			for (final Link out : u.getOutgoingLinks()) {
				SubstrateNode next = (SubstrateNode) out.getTarget();
				if (nodes.contains(next)) {
					distanceUpdate(u, next);
				}
			}
		}
	}

	/**
	 * Initializes this Dijkstra algorithm class.
	 *
	 * @param net   SubstrateNetwork to use.
	 * @param start SubstrateNode to use as a start.
	 */
	private void init(final SubstrateNetwork net, final SubstrateNode start) {
		for (final Node n : net.getNodes()) {
			final SubstrateNode sn = (SubstrateNode) n;
			dists.put(sn, Integer.MAX_VALUE);
			prevs.put(sn, null);
			nodes.add(sn);
			prioNodes.add(sn);
		}

		dists.replace(start, 0);
		// After updating a distance the corresponding node has to be re-added to the
		// priority queue to
		// update its priority
		prioNodes.remove(start);
		prioNodes.add(start);
	}

	/**
	 * Pops the substrate node with the smallest distance from priority queue of
	 * nodes. If there is no such node, the method returns null instead. (This is
	 * necessary for graphs that are not fully connected which is the case for the
	 * extended Dijkstra implementation.)
	 *
	 * @return SubstrateNode with smallest distance.
	 */
	protected SubstrateNode popSmallestDistNode() {
		final SubstrateNode candidate = prioNodes.peek();
		if (dists.get(candidate) != Integer.MAX_VALUE) {
			prioNodes.poll();
			nodes.remove(candidate);
			return candidate;
		}
		return null;
	}

	/**
	 * Performs an update of the distance between to given substrate nodes. The
	 * value will be incremented by 1.
	 *
	 * @param u SubstrateNode u.
	 * @param v SubstrateNode v.
	 */
	protected void distanceUpdate(final SubstrateNode u, final SubstrateNode v) {
		final int alt = dists.get(u) + 1;

		if (alt < dists.get(v)) {
			dists.replace(v, alt);
			prevs.replace(v, u);
			// After updating a distance the corresponding node has to be re-added to the
			// priority queue
			// to update its priority
			prioNodes.remove(v);
			prioNodes.add(v);
		}
	}

	/**
	 * Returns a list of substrate links that form the shortest path from the global
	 * start to a given target node.
	 *
	 * @param target Target node to calculate path for.
	 * @return List of substrate links that form the shortest path from start to
	 *         target.
	 */
	private List<SubstrateLink> shortestPath(final SubstrateNode target) {
		final List<SubstrateLink> links = new LinkedList<>();
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
	 * Searches for a link in a given list that has the given substrate node as
	 * source node.
	 *
	 * @param source Source node to search for.
	 * @param links  List of links to search the one with corresponding source node
	 *               in.
	 * @return SubstrateLink found in the list with given source node.
	 */
	private SubstrateLink getLinkFrom(final SubstrateNode source, final List<Link> links) {
		for (final Link l : links) {
			if (l.getSource().equals(source)) {
				return (SubstrateLink) l;
			}
		}

		return null;
	}

	/**
	 * Calculates and returns all paths from a given start node in a given network.
	 * This method returns a map of all substrate nodes mapped to a list of
	 * substrate link from start node to the key of the map.
	 *
	 * @param net   Network to search all paths for.
	 * @param start SubstrateNode as start/source node of all paths.
	 * @return Map of SubstrateNodes to lists of SubstrateLinks that form the
	 *         corresponding paths.
	 */
	@Override
	public Map<SubstrateNode, List<SubstrateLink>> getAllFastestPaths(final SubstrateNetwork net,
			final SubstrateNode start) {
		final Map<SubstrateNode, List<SubstrateLink>> paths = Collections
				.synchronizedMap(new HashMap<SubstrateNode, List<SubstrateLink>>());
		dijkstra(net, start);

		net.getNodes().stream().forEach((n) -> {
			final SubstrateNode sn = (SubstrateNode) n;
			if (!sn.equals(start)) {
				paths.put(sn, shortestPath(sn));
			}
		});

		return paths;
	}

	@Override
	public Map<SubstrateNode, List<List<SubstrateLink>>> getAllKFastestPaths(final SubstrateNetwork net,
			final SubstrateNode start, final int K) {
		if (K != 1) {
			throw new UnsupportedOperationException(
					"Due to its nature, the Dijkstra algorithm is only able to calculate the K=1 fastest "
							+ "paths for all nodes.");
		}

		final Map<SubstrateNode, List<List<SubstrateLink>>> paths = Collections
				.synchronizedMap(new HashMap<SubstrateNode, List<List<SubstrateLink>>>());

		dijkstra(net, start);

		net.getNodes().stream().forEach((n) -> {
			final SubstrateNode sn = (SubstrateNode) n;
			if (!sn.equals(start)) {
				final List<List<SubstrateLink>> act = new LinkedList<>();
				act.add(shortestPath(sn));
				paths.put(sn, act);
			}
		});

		return paths;
	}

}
