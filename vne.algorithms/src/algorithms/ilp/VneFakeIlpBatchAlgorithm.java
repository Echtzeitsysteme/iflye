package algorithms.ilp;

import java.util.HashSet;
import java.util.Set;

import gt.PatternMatchingDelta;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Implementation of the ILP fake algorithm that uses the batch mechanism.
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
public class VneFakeIlpBatchAlgorithm extends VneFakeIlpAlgorithm {

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	protected VneFakeIlpBatchAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
	}

	/**
	 * Initializes a new instance of the VNE fake ILP batch algorithm.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 * @return Instance of this algorithm implementation.
	 */
	public static VneFakeIlpAlgorithm prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		if (sNet == null || vNets == null) {
			throw new IllegalArgumentException("One of the provided network objects was null.");
		}

		if (vNets.size() == 0) {
			throw new IllegalArgumentException("Provided set of virtual networks was empty.");
		}

		if (instance == null) {
			instance = new VneFakeIlpBatchAlgorithm(sNet, vNets);
		}
		setSnet(sNet);
		final Set<VirtualNetwork> vNetsInt = new HashSet<>();
		vNetsInt.addAll(vNets);
		setVnets(vNetsInt);

		instance.checkPreConditions();
		return instance;
	}

	protected void preHook() {
		// Add all currently embedded networks to job list
		sNet.getGuests().forEach(guest -> {
			vNets.add(guest);
		});
		// Remove embedding of every virtual network, that is currently embedded on
		// substrate one
		vNets.forEach(vn -> {
			if (vn.getHost() != null) {
				System.out.println("=> Un-embed virtual network " + vn.getName());
				facade.removeNetworkEmbedding(vn.getName());
			}
		});
	}

	@Override
	public boolean execute() {
		preHook();
		GlobalMetricsManager.measureMemory();
		init();

		// Check overall embedding possibility
		checkOverallResources();

		// Repair model consistency: Substrate network
		repairSubstrateNetwork();

		// Repair model consistency: Virtual network(s)
		final Set<VirtualNetwork> repairedVnets = repairVirtualNetworks();
		vNets.addAll(repairedVnets);

		GlobalMetricsManager.startPmTime();
		final PatternMatchingDelta delta = createFakeMatches();
		GlobalMetricsManager.endPmTime();

		delta2Ilp(delta);
		GlobalMetricsManager.measureMemory();
		final Set<VirtualNetwork> rejectedNetworks = solveIlp();

		rejectedNetworks.addAll(ignoredVnets);
		embedNetworks(rejectedNetworks);
		GlobalMetricsManager.endDeployTime();
		GlobalMetricsManager.measureMemory();
		return rejectedNetworks.isEmpty();
	}

}
