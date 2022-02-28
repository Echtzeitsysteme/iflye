package test.algorithms.pm.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import facade.ModelFacade;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the VNE pattern matching algorithm migration implementation
 * for testing the migration functionality itself.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
@Disabled
public class VnePmMdvneAlgorithmMigrationTest extends AAlgorithmTest {

	/**
	 * Old value of algorithm configuration number of tries.
	 */
	private int oldNumberOftries;

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
		algo = VnePmMdvneAlgorithmMigration.prepare(sNet, vNets);
	}

	@BeforeEach
	public void saveOldValue() {
		oldNumberOftries = AlgorithmConfig.pmNoMigrations;
	}

	@AfterEach
	public void resetAlgo() {
		if (algo != null) {
			((VnePmMdvneAlgorithm) algo).dispose();
		}
		AlgorithmConfig.pmNoMigrations = oldNumberOftries;
	}

	/*
	 * Positive tests.
	 */

	@Test
	public void testUpdateOnce() {
		createSmallScenario();

		/*
		 * Fourth virtual network
		 */
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 3, 3, 3, 1);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt4", true);
		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt4");
		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// All networks must be embedded
		assertEquals(3, sNet.getGuests().size());
		facade.validateModel();
	}

	/*
	 * Negative tests.
	 */

	/**
	 * In this test method, the algorithm implementation will more or less
	 * immediately return false, because the fourth virtual network can not be
	 * embedded at all- (The substrate network is already filled up to a point were
	 * the last virtual network can never be embedded.)
	 */
	@Test
	public void testUpdateNotPossible() {
		createSmallScenario();

		/*
		 * Fourth virtual network
		 */
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 5, 5, 5, 1);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt4", true);
		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt4");
		initAlgo(sNet, Set.of(vNet));
		assertFalse(algo.execute());

		// All networks but the last one must be embedded
		assertEquals(2, sNet.getGuests().size());
		facade.validateModel();
	}

	/*
	 * Utility methods
	 */

	private void createSmallScenario() {
		createSubstrateScenario();

		/*
		 * First virtual network
		 */
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt", true);

		SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");
		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		/*
		 * Second virtual network
		 */
		virtGen.createNetwork("virt2", true);
		sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt2");
		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		/*
		 * Third virtual network
		 */
		virtGen.createNetwork("virt3", true);
		sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt3");
		initAlgo(sNet, Set.of(vNet));
		assertTrue(algo.execute());

		// Remove second virtual network to get a scenario in which two substrate
		// servers are half
		// filled with guest networks.
		ModelFacade.getInstance().removeNetworkFromRoot("virt2");
	}

	private void createSubstrateScenario() {
		// Substrate network = one tier network
		final OneTierConfig subConfig = new OneTierConfig(3, 1, false, 4, 4, 4, 10);
		final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(subConfig);
		subGen.createNetwork("sub", false);
	}

}
