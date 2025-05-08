package algorithms.random;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import algorithms.AbstractAlgorithm;
import facade.ModelFacade;
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
	 * Initialize the algorithm with the global model facade.
	 */
	public RandomVneAlgorithm() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Creates a new instance of the random VNE algorithm with the given parameters.
	 * 
	 * @param sNet       Substrate network to embed all virtual networks into.
	 * @param vNets      Set of virtual networks to embed into the substrate
	 *                   network.
	 * @param randomSeed Random seed.
	 * @param retries    Number of retries.
	 */
	public RandomVneAlgorithm(final int randomSeed, final int retries) {
		this(ModelFacade.getInstance(), randomSeed, retries);
	}

	/**
	 * Initialize the algorithm with the given model facade.
	 * 
	 * @param modelFacade Model facade to work with.
	 */
	public RandomVneAlgorithm(final ModelFacade modelFacade) {
		super(modelFacade);
	}

	/**
	 * Initialize the algorithm with the given model facade.
	 * 
	 * @param modelFacade Model facade to work with.
	 * @param randomSeed  Random seed.
	 * @param retries     Number of retries.
	 */
	public RandomVneAlgorithm(final ModelFacade modelFacade, final int randomSeed, final int retries) {
		this(modelFacade);

		randGen.setSeed(randomSeed);
		this.retries = retries;
	}

	@Override
	public void prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		if (vNets.size() != 1) {
			throw new IllegalArgumentException(
					"The random VNE algorithm is only suited for one virtual network at a time.");
		}

		super.prepare(sNet, vNets);

		// Check pre-conditions
		checkPreConditions();

		retries = sNet.getNodess().size() * 2;
	}

	@Override
	public boolean execute() {
		final List<Node> subServers = modelFacade.getAllServersOfNetwork(sNet.getName());
		final List<Node> subSwitches = modelFacade.getAllSwitchesOfNetwork(sNet.getName());
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
		success &= modelFacade.embedNetworkToNetwork(sNet.getName(), vNet.getName());
		if (success) {
			embeddedIds.add(vNet.getName());
		}

		for (final Node vnode : vNet.getNodess()) {
			if (vnode instanceof VirtualServer vserver) {
				// Get random substrate server
				SubstrateServer sserver = (SubstrateServer) subServers.get(rand(subServers.size()));

				boolean serverSuccess = true;
				for (int i = 0; i < retries; i++) {
					try {
						modelFacade.embedServerToServer(sserver.getName(), vserver.getName());
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
				success &= modelFacade.embedSwitchToNode(snode.getName(), vswitch.getName());

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
					success &= modelFacade.embedGeneric(vsourceHost.getName(), vLink.getName());

					if (success) {
						embeddedIds.add(vLink.getName());
					}
				} else {
					// If source and target host are not the same node, find the corresponding path
					// between the two nodes.
					final SubstratePath sPath = modelFacade.getPathFromSourceToTarget(vsourceHost.getName(),
							vtargetHost.getName());
					try {
						modelFacade.embedGeneric(sPath.getName(), vLink.getName());
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
			modelFacade.unembedVirtualNetwork((VirtualNetwork) modelFacade.getNetworkById(vNet.getName()));
		}

		return success;
	}

	/**
	 * Returns a random number from start to end.
	 * 
	 * @param start Inclusive lower bound.
	 * @param end   Exclusive upper bound.
	 * @return Random number from start to end.
	 */
	private int rand(final int start, final int end) {
		return randGen.nextInt(start, end);
	}

	/**
	 * Returns a random number from 0 to end (exclusive).
	 * 
	 * @param end Exclusive upper bound.
	 * @return Random number from 0 to end.
	 */
	private int rand(final int end) {
		return rand(0, end);
	}

	/**
	 * Finds a possible host for the virtual node to be embedded into.
	 * 
	 * @param vNode Virtual node to find a host for.
	 * @return Possible host for the given virtual node.
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
