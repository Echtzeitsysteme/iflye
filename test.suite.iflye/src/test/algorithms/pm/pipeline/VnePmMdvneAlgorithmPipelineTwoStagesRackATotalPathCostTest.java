package test.algorithms.pm.pipeline;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import model.Link;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.pm.VnePmMdvneAlgorithmTotalPathCostTest;

/**
 * Test class for the VNE pattern matching algorithm implementation for
 * minimizing the total path cost metric including the pipeline functionality.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackATotalPathCostTest extends VnePmMdvneAlgorithmTotalPathCostTest {

	@Override
	public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
		algo = new VnePmMdvneAlgorithmPipelineTwoStagesRackA();
		algo.prepare(sNet, vNets);
	}

	@Override
	@Disabled
	public void testAllOnOneServer() {
		// This test is disabled, because the pipeline stage rack A can not embed a
		// virtual network onto
		// a substrate server only (desired behavior).
	}

	@Test
	@Override
	public void testNoEmbeddingIfFullOneByOne() {
		oneTierSetupTwoServers("virt", 2);
		twoTierSetupFourServers("sub", 4);

		// Patch the substrate links that connects servers with rack switches to hold a
		// higher bandwidth
		// value
		final List<Link> slinks = facade.getAllLinksOfNetwork("sub");
		slinks.stream().forEach(l -> {
			if (l instanceof SubstrateLink && l.getBandwidth() == 1) {
				final SubstrateLink sl = (SubstrateLink) l;
				sl.setBandwidth(5);
				sl.setResidualBandwidth(5);
			}
		});

		// Normal test procedure continues here
		facade.createAllPathsForNetwork("sub");

		final SubstrateNetwork sNet = (SubstrateNetwork) facade.getNetworkById("sub");

		// First three must succeed
		initAlgo(sNet, Set.of((VirtualNetwork) facade.getNetworkById("virt")));
		assertTrue(algo.execute());

		facade.addNetworkToRoot("virt2", true);
		oneTierSetupTwoServers("virt2", 2);

		initAlgo(sNet, Set.of((VirtualNetwork) facade.getNetworkById("virt2")));
		assertTrue(algo.execute());

		facade.addNetworkToRoot("virt3", true);
		oneTierSetupTwoServers("virt3", 2);

		initAlgo(sNet, Set.of((VirtualNetwork) facade.getNetworkById("virt3")));
		assertTrue(algo.execute());

		facade.addNetworkToRoot("virt4", true);
		oneTierSetupThreeServers("virt4", 2);

		// Last one must not succeed
		initAlgo(sNet, Set.of((VirtualNetwork) facade.getNetworkById("virt4")));
		assertFalse(algo.execute());

		checkAllElementsEmbeddedOnSubstrateNetwork(sNet, Set.of((VirtualNetwork) facade.getNetworkById("virt"),
				(VirtualNetwork) facade.getNetworkById("virt2"), (VirtualNetwork) facade.getNetworkById("virt3")));
		final VirtualNetwork vNet4 = (VirtualNetwork) facade.getNetworkById("virt4");
		assertNull(vNet4.getHost());
	}

}
