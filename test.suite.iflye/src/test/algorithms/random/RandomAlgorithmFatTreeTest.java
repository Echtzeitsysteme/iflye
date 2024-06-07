package test.algorithms.random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import algorithms.random.RandomVneAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.FatTreeNetworkGenerator;
import generators.OneTierNetworkGenerator;
import generators.config.FatTreeConfig;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTest;

/**
 * Test class for the random algorithm implementation using GoogleFatTree based
 * substrate networks.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class RandomAlgorithmFatTreeTest extends AAlgorithmTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		algo = new RandomVneAlgorithm(sNet, vNets);
	}

	@BeforeEach
	public void setUp() {
		facade.resetAll();

		// Network setup
		ModelFacade.getInstance().addNetworkToRoot("sub", false);
		ModelFacade.getInstance().addNetworkToRoot("virt", true);

		// Normal model setup
		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		ModelFacadeConfig.MAX_PATH_LENGTH = 6;
		ModelFacadeConfig.IGNORE_BW = true;
	}

	/*
	 * Positive tests
	 */

	@Test
	public void testOneSmallVirtualNetwork() {
		final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
		virtGen.createNetwork("virt", true);

		final FatTreeConfig subConfig = new FatTreeConfig(4);
		final FatTreeNetworkGenerator subGen = new FatTreeNetworkGenerator(subConfig);
		subGen.createNetwork("sub", false);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
		assertTrue(randomVne.execute());
	}

	@Test
	public void testHalfLoadSmallVirtualNetworks() {
		final FatTreeConfig subConfig = new FatTreeConfig(4);
		final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
		subConfig.setRack(subRackConfig);
		final FatTreeNetworkGenerator subGen = new FatTreeNetworkGenerator(subConfig);
		subGen.createNetwork("sub", false);

		// k = 4 -> 16 substrate servers
		for (int i = 0; i < 8; i++) {
			final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
			final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
			virtGen.createNetwork("virt" + i, true);

			final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt" + i);

			final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
			assertTrue(randomVne.execute());
		}
	}

	@Test
	public void testFullLoadSmallVirtualNetworks() {
		final FatTreeConfig subConfig = new FatTreeConfig(4);
		final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
		subConfig.setRack(subRackConfig);
		final FatTreeNetworkGenerator subGen = new FatTreeNetworkGenerator(subConfig);
		subGen.createNetwork("sub", false);

		// k = 4 -> 16 substrate servers
		for (int i = 0; i < 16; i++) {
			final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
			final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
			virtGen.createNetwork("virt" + i, true);

			final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt" + i);

			final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
			assertTrue(randomVne.execute());
		}
	}

	@Test
	public void testHalfLoadLargeVirtualNetwork() {
		final FatTreeConfig subConfig = new FatTreeConfig(4);
		final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
		subConfig.setRack(subRackConfig);
		final FatTreeNetworkGenerator subGen = new FatTreeNetworkGenerator(subConfig);
		subGen.createNetwork("sub", false);

		final OneTierConfig virtConfig = new OneTierConfig(8, 1, false, 2, 2, 2, 2);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
		virtGen.createNetwork("virt", true);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
		assertTrue(randomVne.execute());
	}

	@Test
	public void testFullLoadLargeVirtualNetworkA() {
		final FatTreeConfig subConfig = new FatTreeConfig(4);
		final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
		subConfig.setRack(subRackConfig);
		final FatTreeNetworkGenerator subGen = new FatTreeNetworkGenerator(subConfig);
		subGen.createNetwork("sub", false);

		final OneTierConfig virtConfig = new OneTierConfig(16, 1, false, 2, 2, 2, 2);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
		virtGen.createNetwork("virt", true);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
		assertTrue(randomVne.execute());
	}

	@Test
	public void testFullLoadLargeVirtualNetworkB() {
		final FatTreeConfig subConfig = new FatTreeConfig(4);
		final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 10, 10, 10, 100);
		subConfig.setRack(subRackConfig);
		final FatTreeNetworkGenerator subGen = new FatTreeNetworkGenerator(subConfig);
		subGen.createNetwork("sub", false);

		final OneTierConfig virtConfig = new OneTierConfig(80, 1, false, 2, 2, 2, 2);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
		virtGen.createNetwork("virt", true);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));
		assertTrue(randomVne.execute());
	}

	/*
	 * Negative tests
	 */
	@Test
	public void testFullReject() {
		final FatTreeConfig subConfig = new FatTreeConfig(4);
		final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
		subConfig.setRack(subRackConfig);
		final FatTreeNetworkGenerator subGen = new FatTreeNetworkGenerator(subConfig);
		subGen.createNetwork("sub", false);

		// k = 4 -> 16 substrate servers, but we want to request one more embedding
		for (int i = 0; i < 17; i++) {
			final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
			final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
			virtGen.createNetwork("virt" + i, true);

			final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
			final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt" + i);

			final RandomVneAlgorithm randomVne = new RandomVneAlgorithm(sNet, Set.of(vNet));

			if (i < 16) {
				assertTrue(randomVne.execute());
			} else {
				assertFalse(randomVne.execute());
			}
		}
	}

}
