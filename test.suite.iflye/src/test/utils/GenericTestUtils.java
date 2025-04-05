package test.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.ilp.VneFakeIlpAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Generic utilities for all tests.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class GenericTestUtils {

	/**
	 * Embeds a given set of virtual networks to a substrate network using the fake
	 * ILP algorithm.
	 *
	 * @param sNet  Substrate network to embed virtual networks onto.
	 * @param vNets Set of virtual networks to embed onto the substrate network.
	 */
	public static void vneFakeIlpEmbedding(SubstrateNetwork sNet, Set<VirtualNetwork> vNets) {
		final Embedding oldEmbeddingMechanism = AlgorithmConfig.emb;
		AlgorithmConfig.emb = Embedding.MANUAL;
		final AbstractAlgorithm algo = new VneFakeIlpAlgorithm();
		algo.prepare(sNet, vNets);
		assertTrue(algo.execute());
		AlgorithmConfig.emb = oldEmbeddingMechanism;
	}

}
