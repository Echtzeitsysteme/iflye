package algorithms.random;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import algorithms.AbstractAlgorithm;
import facade.config.ModelFacadeConfig;
import model.Link;
import model.Node;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstratePath;
import model.SubstrateServer;
import model.VirtualNetwork;
import model.VirtualNode;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Super simple Virtual Network Embedding algorithm. It chooses substrate nodes
 * randomly and embeds the virtual links accordingly.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class RandomVneAlgorithm extends AbstractAlgorithm {

	/**
	 * Pseudo random number generator with a seed.
	 */
	final private Random randGen = new Random(0);

	private int retries = 10;

	/**
	 * Initializes a new object of this random VNE algorithm.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public RandomVneAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);

		if (vNets.size() != 1) {
			throw new IllegalArgumentException(
					"The random VNE algorithm is only suited for one virtual network at a time.");
		}

		// Check pre-conditions
		checkPreConditions();

		retries = sNet.getNodes().size() * 2;
	}

	/**
	 * TODO
	 * 
	 * @param sNet
	 * @param vNets
	 * @param randomSeed
	 * @param retries
	 */
	public RandomVneAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets, final int randomSeed,
			final int retries) {
		this(sNet, vNets);
		randGen.setSeed(randomSeed);
		this.retries = retries;
	}

	@Override
	public boolean execute() {
		final List<Node> subServers = facade.getAllServersOfNetwork(sNet.getName());
		final List<Node> subSwitches = facade.getAllSwitchesOfNetwork(sNet.getName());
		final List<Node> allNodes = new LinkedList<Node>();
		allNodes.addAll(subSwitches);
		allNodes.addAll(subServers);

		final Set<String> embeddedIds = new HashSet<String>();

		/*
		 * Place embedding on model
		 */

		boolean success = true;
		final VirtualNetwork vNet = vNets.iterator().next();

		// Embed virtual network
		success &= facade.embedNetworkToNetwork(sNet.getName(), vNet.getName());
		if (success) {
			embeddedIds.add(vNet.getName());
		}

		for (final Node vnode : vNet.getNodes()) {
			if (vnode instanceof VirtualServer vserver) {
				// Get random substrate server
				SubstrateServer sserver = (SubstrateServer) subServers.get(rand(subServers.size()));

				boolean serverSuccess = true;
				for (int i = 0; i < retries; i++) {
					try {
						facade.embedServerToServer(sserver.getName(), vserver.getName());
						embeddedIds.add(vserver.getName());
						serverSuccess = true;
						break;
					} catch (final UnsupportedOperationException ex) {
						serverSuccess = false;
						sserver = (SubstrateServer) subServers.get(rand(subServers.size()));
					}
				}

				success &= serverSuccess;
			} else if (vnode instanceof VirtualSwitch vswitch) {
				final SubstrateNode snode = (SubstrateNode) allNodes.get(rand(allNodes.size()));
				success &= facade.embedSwitchToNode(snode.getName(), vswitch.getName());

				if (success) {
					embeddedIds.add(vswitch.getName());
				}
			}

			// Termination condition
			if (!success) {
				break;
			}
		}

		if (success) {
			for (final Link vLink : vNet.getLinks()) {
				final VirtualNode vsource = (VirtualNode) vLink.getSource();
				final VirtualNode vtarget = (VirtualNode) vLink.getTarget();

				final SubstrateNode vsourceHost = findHost(vsource);
				final SubstrateNode vtargetHost = findHost(vtarget);

				// If both, the source and the target of a virtual link are embedded to the same
				// substrate node, also use this node for the embedding of the virtual link.
				if (vsourceHost.equals(vtargetHost)) {
					success &= facade.embedGeneric(vsourceHost.getName(), vLink.getName());

					if (success) {
						embeddedIds.add(vLink.getName());
					}
				} else {
					// If source and target host are not the same node, find the corresponding path
					// between the two nodes.
					final SubstratePath sPath = facade.getPathFromSourceToTarget(vsourceHost.getName(),
							vtargetHost.getName());
					try {
						facade.embedGeneric(sPath.getName(), vLink.getName());
						embeddedIds.add(vLink.getName());
					} catch (final UnsupportedOperationException ex) {
						success = false;
					}
				}
			}
		}

		// If at least one element could not be embedded, all other embeddings must be
		// removed.
		if (!success) {
//			embeddedIds.forEach(id -> {
//				// TODO: Generic un-embedding
//			});
			facade.unembedVirtualNetwork((VirtualNetwork) facade.getNetworkById(vNet.getName()));
		}

		return success;
	}

	/**
	 * TODO
	 * 
	 * @param start Inclusive
	 * @param end   Exclusive
	 * @return
	 */
	private int rand(final int start, final int end) {
		return randGen.nextInt(start, end);
	}

	/**
	 * TODO
	 * 
	 * @param end
	 * @return
	 */
	private int rand(final int end) {
		return rand(0, end);
	}

	/**
	 * TODO
	 * 
	 * @param vNode
	 * @return
	 */
	private SubstrateNode findHost(final VirtualNode vNode) {
		if (vNode instanceof VirtualServer vsrv) {
			return vsrv.getHost();
		} else if (vNode instanceof VirtualSwitch vsw) {
			return vsw.getHost();
		}

		throw new UnsupportedOperationException();
	}

	/**
	 * Checks every condition necessary to run this algorithm. If a condition is not
	 * met, it throws an UnsupportedOperationException.
	 */
	private void checkPreConditions() {
		// Path creation has to be enabled for paths with length = 1
		if (ModelFacadeConfig.MIN_PATH_LENGTH != 1) {
			throw new UnsupportedOperationException("Minimum path length must be 1.");
		}

		// Bandwidth ignore must be true
		if (!ModelFacadeConfig.IGNORE_BW) {
			throw new UnsupportedOperationException("Bandwidth ignore flag must be set.");
		}

		// There must be generated substrate paths
		if (sNet.getPaths().isEmpty()) {
			throw new UnsupportedOperationException("Generated paths are missing in substrate network.");
		}
	}

}
