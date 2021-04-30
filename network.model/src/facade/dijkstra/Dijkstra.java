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

public class Dijkstra {

	/**
	 * Mapping: Node -> Distance.
	 */
	final static Map<SubstrateNode, Integer> dists = new HashMap<SubstrateNode, Integer>();
	
	/**
	 * Mapping: Node -> Previous node.
	 */
	final static Map<SubstrateNode, SubstrateNode> prevs = new HashMap<SubstrateNode, SubstrateNode>();
	
	/**
	 * List of all nodes.
	 */
	final static Set<SubstrateNode> nodes = new HashSet<SubstrateNode>();
	
	private Dijkstra() {}
	
	private static List<Node> dijkstra(final SubstrateNetwork net, final SubstrateNode start) {
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
	
	private static void init(final SubstrateNetwork net, final SubstrateNode start) {
		for (final Node n : net.getNodes()) {
			final SubstrateNode sn = (SubstrateNode) n;
			dists.put(sn, Integer.MAX_VALUE);
			prevs.put(sn, null);
			nodes.add(sn);
		}
		
		dists.replace(start, 0);
	}
	
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
	
	private static void distanceUpdate(final SubstrateNode u, final SubstrateNode v) {
		final int alt = dists.get(u) + 1;
		
		if (alt < dists.get(v)) {
			dists.replace(v, alt);
			prevs.replace(v, u);
		}
	}
	
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
	
	private static SubstrateLink getLinkFrom(final SubstrateNode start, final Collection<Link> links) {
		for (final Link l : links) {
			if (l.getSource().equals(start)) {
				return (SubstrateLink) l;
			}
		}
		
		return null;
	}
	
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
