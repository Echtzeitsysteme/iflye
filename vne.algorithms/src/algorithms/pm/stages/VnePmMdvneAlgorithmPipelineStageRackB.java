package algorithms.pm.stages;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import algorithms.AlgorithmConfig;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.ModelFacade;
import gt.IncrementalPatternMatcher;
import gt.PatternMatchingDelta;
import gt.PatternMatchingDelta.Match;
import gt.emoflon.EmoflonGtFactory;
import gt.emoflon.EmoflonGtRackB;
import gt.emoflon.EmoflonGtRackBFactory;
import ilp.wrapper.config.IlpSolverConfig;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateElement;
import model.VirtualElement;
import model.VirtualNetwork;

/**
 * Implementation of the model-driven virtual network algorithm that uses
 * pattern matching as a way to reduce the search space of the ILP solver. This
 * implementation embeds virtual networks onto racks B.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineStageRackB extends VnePmMdvneAlgorithm {

	/**
	 * Incremental pattern matcher to use for the second pipeline stage.
	 */
	protected IncrementalPatternMatcher patternMatcherRack;

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public VnePmMdvneAlgorithmPipelineStageRackB() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Constructor.
	 */
	public VnePmMdvneAlgorithmPipelineStageRackB(final ModelFacade modelFacade) {
		super(modelFacade);
	}

	/**
	 * Resets the ILP solver and the pattern matcher.
	 */
	@Override
	public void dispose() {
		if (this.ilpSolver != null) {
			this.ilpSolver.dispose();
		}
		if (this.patternMatcher != null) {
			this.patternMatcher.dispose();
		}
		if (this.patternMatcherRack != null) {
			this.patternMatcherRack.dispose();
		}
		super.dispose();
	}

	@Override
	public boolean execute() {
		GlobalMetricsManager.measureMemory();
		init();

		// // Check overall embedding possibility
		// checkOverallResources();
		//
		// // Repair model consistency: Substrate network
		// repairSubstrateNetwork();
		//
		// // Repair model consistency: Virtual network(s)
		// final Set<VirtualNetwork> repairedVnets = repairVirtualNetworks();
		// if (!repairedVnets.isEmpty()) {
		// this.patternMatcher = new EmoflonGtFactory().create();
		// this.patternMatcherRack = new EmoflonGtRackFactory().create();
		// }
		// vNets.addAll(repairedVnets);

		//
		// Stage 2: Virtual network -> Rack
		//

		// // Remove embedding of all already embedded networks
		// PmAlgorithmUtils.unembedAll(sNet, vNets);
		// System.out.println("=> Starting pipeline stage #2");

		GlobalMetricsManager.startPmTime();
		final PatternMatchingDelta deltaTwo = patternMatcherRack.run();
		GlobalMetricsManager.endPmTime();

		// Uses the "normal" delta to ILP translator of the super class
		delta2Ilp(deltaTwo);
		GlobalMetricsManager.measureMemory();
		final Set<VirtualNetwork> rejectedNetworksTwo = solveIlp();

		rejectedNetworksTwo.addAll(ignoredVnets);
		embedNetworks(rejectedNetworksTwo);
		GlobalMetricsManager.endDeployTime();
		GlobalMetricsManager.measureMemory();
		return rejectedNetworksTwo.isEmpty();
	}

	/*
	 * Helper methods.
	 */

	/**
	 * Initializes the algorithm by creating a new incremental solver object and a
	 * new pattern matcher object.
	 */
	@Override
	public void init() {
		// Create new ILP solver object on every method call.
		ilpSolver = IlpSolverConfig.getIlpSolver();

		if (patternMatcher == null) {
			patternMatcher = new EmoflonGtFactory().create();
		}

		if (patternMatcherRack == null) {
			patternMatcherRack = new EmoflonGtRackBFactory().create();
		}
	}

	/**
	 * Updates and embeds the actual mappings for a given map of names (strings) and
	 * booleans.
	 *
	 * @param mappings Map of strings and booleans. The keys are mapping names and
	 *                 the values define if the mapping was chosen.
	 * @return Returns a set of all virtual networks that could not be embedded.
	 */
	@Override
	protected Set<VirtualNetwork> updateMappingsAndEmbed(final Map<String, Boolean> mappings) {
		// Embed elements
		final Set<VirtualNetwork> rejectedNetworks = new HashSet<>();
		final EmoflonGtRackB engine = (EmoflonGtRackB) patternMatcherRack;

		// for (final String s : newMappings) {
		for (final String s : mappings.keySet()) {
			if (!mappings.get(s)) {
				continue;
			}

			final Match m = variablesToMatch.get(s);

			// Network -> Network (rejected)
			if (m.getVirtual() instanceof VirtualNetwork) {
				rejectedNetworks.add((VirtualNetwork) m.getVirtual());
				continue;
			}

			// Embed element: Either use emoflon/GT or use manual mode.
			switch (AlgorithmConfig.emb) {
			case EMOFLON:
				// Create embedding via matches and graph transformation
				engine.apply((VirtualElement) m.getVirtual(), (SubstrateElement) m.getSubstrate(), true);
				break;
			case EMOFLON_WO_UPDATE:
				// Create embedding via matches and graph transformation
				engine.apply((VirtualElement) m.getVirtual(), (SubstrateElement) m.getSubstrate(), false);
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}

		// Workaround to fix the residual bandwidth of other paths possibly affected by
		// virtual link to substrate path embeddings
		modelFacade.updateAllPathsResidualBandwidth(sNet.getName());

		return rejectedNetworks;
	}

}
