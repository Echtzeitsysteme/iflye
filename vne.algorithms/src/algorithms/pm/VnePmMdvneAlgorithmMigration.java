package algorithms.pm;

import java.util.HashSet;
import java.util.Set;

import algorithms.AlgorithmConfig;
import facade.ModelFacade;
import gt.PatternMatchingDelta;
import gt.emoflon.EmoflonGtFactory;
import metrics.manager.GlobalMetricsManager;
import model.Node;
import model.VirtualNetwork;
import model.VirtualServer;

/**
 * Implementation of the model-driven virtual network algorithm that uses
 * pattern matching as a way to reduce the search space of the ILP solver. This
 * implementation also uses migration functionality in case a virtual network
 * does not fit on the current state of the substrate network.
 *
 * Parts of this implementation are heavily inspired, taken or adapted from the
 * idyve project [1].
 *
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in
 * Rechenzentren, http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI
 * 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmMigration extends VnePmMdvneAlgorithm {

	/**
	 * Set of virtual networks that are rejected despite they could not have been
	 * embedded through the migration mechanism.
	 */
	final Set<VirtualNetwork> rejectedDespiteMigration = new HashSet<>();

	/**
	 * Global pattern matching delta. This one holds all occurred matches from all
	 * algorithm runs and is needed by the embedding migration calculations.
	 */
	final PatternMatchingDelta globalDelta = new PatternMatchingDelta();

	/**
	 * Initialize the algorithm with the global model facade.
	 */
	public VnePmMdvneAlgorithmMigration() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Initialize the algorithm with the given model facade.
	 * 
	 * @param modelFacade Model facade to work with.
	 */
	public VnePmMdvneAlgorithmMigration(final ModelFacade modelFacade) {
		super(modelFacade);
	}

	@Override
	public boolean execute() {
		GlobalMetricsManager.measureMemory();
		init();

		// Check overall embedding possibility
		checkOverallResources();

		// Repair model consistency: Substrate network
		repairSubstrateNetwork();

		// Repair model consistency: Virtual network(s)
		final Set<VirtualNetwork> repairedVnets = repairVirtualNetworks();
		if (!repairedVnets.isEmpty()) {
			this.patternMatcher = new EmoflonGtFactory().create();
		}
		vNets.addAll(repairedVnets);

		GlobalMetricsManager.startPmTime();
		final PatternMatchingDelta delta = patternMatcher.run();
		GlobalMetricsManager.endPmTime();

		// Add current delta to the global one
		globalDelta.addOther(delta);

		delta2Ilp(delta);
		GlobalMetricsManager.measureMemory();
		Set<VirtualNetwork> rejectedNetworks = solveIlp();
		rejectedDespiteMigration.addAll(rejectedNetworks);

		// Check if embedding migration routing must be started
		if (!rejectedNetworks.isEmpty()) {
			System.out.println("=> Started embedding migration.");
			embedNetworks(rejectedNetworks);
			rejectedNetworks = tryMigrationEmbedding();
		}

		rejectedDespiteMigration.addAll(ignoredVnets);
		embedNetworks(rejectedDespiteMigration);
		GlobalMetricsManager.endDeployTime();
		GlobalMetricsManager.measureMemory();
		return rejectedDespiteMigration.isEmpty();
	}

	/**
	 * Removes the smallest virtual network currently embedded on the substrate one
	 * and tries the embedding job again. If it fails again, the method removes the
	 * next smallest virtual network and tries again. If no virtual network to
	 * remove is left, the method returns a set of rejected networks.
	 *
	 * @return Set of virtual networks that could not be embedded onto the substrate
	 *         one.
	 */
	private Set<VirtualNetwork> tryMigrationEmbedding() {
		Set<VirtualNetwork> rejectedNetworks = new HashSet<>();
		VirtualNetwork removalCandidate = findAndUnembedSmallestNetwork();
		int tries = 0;

		// Pattern matching delta for this run's migration tries
		final PatternMatchingDelta delta = new PatternMatchingDelta();
		delta.addOther(globalDelta);

		while (removalCandidate != null) {
			PmAlgorithmUtils.unembedAll(sNet, vNets);
			vNets.add(removalCandidate);
			PmAlgorithmUtils.unembedAll(sNet, vNets);
			init();

			GlobalMetricsManager.startPmTime();
			final PatternMatchingDelta deltaIncr = patternMatcher.run();
			GlobalMetricsManager.endPmTime();

			// add deltaIncr to delta
			delta.addOther(deltaIncr);

			delta2Ilp(delta);
			rejectedNetworks.clear();
			rejectedNetworks.addAll(solveIlp());

			rejectedDespiteMigration.addAll(rejectedNetworks);
			rejectedDespiteMigration.retainAll(rejectedNetworks);

			if (rejectedNetworks.isEmpty()) {
				break;
			}

			tries++;

			// Check number of already tried migrations; if threshold reached, stop trying.
			if (tries >= AlgorithmConfig.pmNoMigrations) {
				break;
			}

			removalCandidate = findAndUnembedSmallestNetwork();
		}

		return rejectedNetworks;
	}

	/**
	 * Finds the smallest virtual network currently embedded on the substrate
	 * network and removes its embedding. If there is no such network, the method
	 * returns null.
	 *
	 * @return Smallest virtual network object currently embedded on the substrate
	 *         network or null if
	 */
	private VirtualNetwork findAndUnembedSmallestNetwork() {
		VirtualNetwork smallest = null;
		long res = Integer.MAX_VALUE;

		for (final VirtualNetwork vn : sNet.getGuests()) {
			long aRes = 0;
			for (final Node n : vn.getNodess()) {
				if (n instanceof VirtualServer) {
					final VirtualServer vsrv = (VirtualServer) n;
					aRes += vsrv.getCpu();
					aRes += vsrv.getMemory();
					aRes += vsrv.getStorage();
				}
			}

			if (aRes < res) {
				res = aRes;
				smallest = vn;
			}
		}

		if (smallest != null) {
			ModelFacade.getInstance().unembedVirtualNetwork(smallest);
		}

		return smallest;
	}

	/**
	 * Resets the components of the super algorithm and the ones of this class.
	 */
	@Override
	public void dispose() {
		super.dispose();
		rejectedDespiteMigration.clear();
		this.globalDelta.clear();
	}

}
