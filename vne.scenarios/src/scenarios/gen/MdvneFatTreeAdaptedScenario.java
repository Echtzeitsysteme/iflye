package scenarios.gen;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.heuristics.TafAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.FatTreeNetworkGenerator;
import generators.config.FatTreeConfig;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Adapted evaluation scenario of the dissertation [1]. In comparison to the
 * evaluation section of the dissertation [1], this scenario uses a Fat Tree
 * based substrate network. All other parameters are left as in
 * {@link MdvneAdaptedScenario}.
 *
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in
 * Rechenzentren, http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI
 * 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class MdvneFatTreeAdaptedScenario extends AMdvneAdaptedScenario implements IScenario {

	/**
	 * Fat Tree scaling parameter.
	 */
	private static int k = 8;

	/**
	 * Number of virtual network requests (VNRs).
	 */
	private static int numberOfVnrs = 40;

	/**
	 * Amount of bandwidth for all links between rack and aggregation switches.
	 */
	public final int substrateBwAggr = 10_000;

	/**
	 * Entry point method for this scenario.
	 *
	 * @param args Runtime arguments that will be ignored.
	 */
	public static void main(final String[] args) {
		// TODO: Remove bandwidth ignoring after changing the VNE algorithm instance
		// below.
		ModelFacadeConfig.IGNORE_BW = true;
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 6;

		final MdvneFatTreeAdaptedScenario scen = new MdvneFatTreeAdaptedScenario();

		scen.substrateSetup("sub");
		final SubstrateNetwork sub = (SubstrateNetwork) facade.getNetworkById("sub");

		for (int i = 0; i < numberOfVnrs; i++) {
			final String virtualNetworkId = "v" + i;
			scen.virtualSetup(virtualNetworkId);
			final VirtualNetwork virt = (VirtualNetwork) facade.getNetworkById(virtualNetworkId);
			// TODO: Change the algorithm instance later on.
			final AbstractAlgorithm algo = new TafAlgorithm();
			algo.prepare(sub, Set.of(virt));
			final boolean success = algo.execute();

			if (success) {
				logger.info("Embedded VNR: " + virtualNetworkId);
			} else {
				logger.warning("Rejected VNR: " + virtualNetworkId);
			}
		}

		// Save model to file
		ModelFacade.getInstance().persistModel();
		logger.info("=> Scenario finished.");

		System.exit(0);
	}

	/**
	 * Sets the substrate network up (according to the configuration).
	 *
	 * @param substrateNetworkId The ID for the substrate network to build.
	 */
	private void substrateSetup(final String substrateNetworkId) {
		final OneTierConfig substrateRackConfig = new OneTierConfig(serversPerRack, 1, false, substrateCpu,
				substrateMem, substrateSto, substrateBwSrv);
		final FatTreeConfig substrateConfig = new FatTreeConfig(k);
		substrateConfig.setRack(substrateRackConfig);
		substrateConfig.setBwCoreToAggr(substrateBwCore);
		substrateConfig.setBwAggrToEdge(substrateBwAggr);
		final FatTreeNetworkGenerator subGen = new FatTreeNetworkGenerator(substrateConfig);
		subGen.createNetwork(substrateNetworkId, false);
	}

}
