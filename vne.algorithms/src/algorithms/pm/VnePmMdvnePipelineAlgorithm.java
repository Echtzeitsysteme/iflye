package algorithms.pm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmPipeline;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import gt.IncrementalPatternMatcher;
import gt.PatternMatchingDelta.Match;
import gt.emoflon.EmoflonGtFactory;
import ilp.wrapper.IncrementalIlpSolver;
import ilp.wrapper.config.IlpSolverConfig;
import metrics.manager.GlobalMetricsManager;
import model.Node;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.VirtualNetwork;
import model.VirtualServer;

/**
 * Implementation of the model-driven virtual network algorithm that uses
 * pattern matching as a way to reduce the search space of the ILP solver.
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
public abstract class VnePmMdvnePipelineAlgorithm extends VnePmMdvneAlgorithm implements AlgorithmPipeline {

	/**
	 * Incremental pattern matcher to use.
	 */
	protected IncrementalPatternMatcher patternMatcher;

	/**
	 * Incremental ILP solver to use.
	 */
	protected IncrementalIlpSolver ilpSolver;

	/**
	 * Mapping of string (name) to matches.
	 */
	protected final Map<String, Match> variablesToMatch = new HashMap<>();

	/**
	 * A list of all algorithms that are part of this pipeline.
	 */
	protected final List<AbstractAlgorithm> pipeline = new ArrayList<>();

	/**
	 * Set of ignored virtual networks. Ignored virtual networks are requests, that
	 * can not fit on the substrate network at all and are therefore ignored (as
	 * they are not given to the ILP solver).
	 */
	protected final Set<VirtualNetwork> ignoredVnets = new HashSet<>();

	/**
	 * Initialize the algorithm with the global model facade.
	 */
	public VnePmMdvnePipelineAlgorithm() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Initialize the algorithm with the global model facade and the given pipeline
	 * of algorithms.
	 * 
	 * @param pipeline The algorithms to be used in the pipeline.
	 */
	public VnePmMdvnePipelineAlgorithm(final Collection<AbstractAlgorithm> pipeline) {
		this(ModelFacade.getInstance(), pipeline);
	}

	/**
	 * Initialize the algorithm with the given model facade.
	 * 
	 * @param modelFacade Model facade to work with.
	 */
	public VnePmMdvnePipelineAlgorithm(final ModelFacade modelFacade) {
		this(modelFacade, List.of());
	}

	/**
	 * Initialize the algorithm with the given model facade and the given pipeline
	 * of algorithms.
	 * 
	 * @param modelFacade Model facade to work with.
	 * @param pipeline    The algorithms to be used in the pipeline.
	 */
	public VnePmMdvnePipelineAlgorithm(final ModelFacade modelFacade, final Collection<AbstractAlgorithm> pipeline) {
		super(modelFacade);

		this.pipeline.addAll(pipeline);
	}

	@Override
	public List<AbstractAlgorithm> getPipeline() {
		return this.pipeline;
	}

	/**
	 * Initializes a new instance of the VNE pattern matching algorithm.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 * @return Instance of this algorithm implementation.
	 */
	@Override
	public void prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		if (sNet == null || vNets == null) {
			throw new IllegalArgumentException("One of the provided network objects was null.");
		}

		if (vNets.size() == 0) {
			throw new IllegalArgumentException("Provided set of virtual networks was empty.");
		}

		super.prepare(sNet, vNets);

		checkPreConditions();
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

		super.dispose();
		AlgorithmPipeline.super.dispose();
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

		int stage = 0;
		for (AbstractAlgorithm algo : pipeline) {
			// Run algorithm preparation again because the substrate network or the set of
			// virtual networks may have changed because of the repairing above.
			algo.prepare(sNet, vNets);
			if (stage > 0) {
				PmAlgorithmUtils.unembedAll(sNet, vNets);
			}

			System.out.println("=> Starting pipeline stage #" + (++stage));
			if (algo.execute()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks the overall resource availability for all nodes of all virtual
	 * networks and all nodes of the substrate network. If a network can not be
	 * placed on the substrate network at all, the method adds it to the set of
	 * ignored networks.
	 */
	@Override
	protected void checkOverallResources() {
		// Calculate total residual resources for substrate servers
		// Datatype long is needed, because of the possible large values of substrate
		// server residual
		// resources (e.g. from the diss scenario).
		long subTotalResidualCpu = 0;
		long subTotalResidualMem = 0;
		long subTotalResidualSto = 0;

		for (final Node n : sNet.getNodess()) {
			if (n instanceof SubstrateServer) {
				final SubstrateServer ssrv = (SubstrateServer) n;
				subTotalResidualCpu += ssrv.getResidualCpu();
				subTotalResidualMem += ssrv.getResidualMemory();
				subTotalResidualSto += ssrv.getResidualStorage();
			}
		}

		for (final VirtualNetwork vNet : vNets) {
			// Calculate needed resources for current virtual network candidate
			long virtTotalCpu = 0;
			long virtTotalMem = 0;
			long virtTotalSto = 0;

			for (final Node n : vNet.getNodess()) {
				if (n instanceof VirtualServer) {
					final VirtualServer vsrv = (VirtualServer) n;
					virtTotalCpu += vsrv.getCpu();
					virtTotalMem += vsrv.getMemory();
					virtTotalSto += vsrv.getStorage();
				}
			}

			if (!(subTotalResidualCpu >= virtTotalCpu && subTotalResidualMem >= virtTotalMem
					&& subTotalResidualSto >= virtTotalSto)) {
				ignoredVnets.add(vNet);
			}
		}
	}

	/*
	 * Helper methods.
	 */

	/**
	 * Repairs the consistency of the substrate network. This is necessary, if a
	 * virtual network was removed "dirty" from the model and the residual values or
	 * guest references are not updated properly.
	 */
	@Override
	protected void repairSubstrateNetwork() {
		// Find all networks that were removed in the meantime
		final Set<VirtualNetwork> removedGuests = sNet.getGuests().stream()
				.filter(g -> !modelFacade.networkExists(g.getName())).collect(Collectors.toSet());

		// Remove embedding of all elements of the virtual network
		removedGuests.forEach(g -> modelFacade.unembedVirtualNetwork(g));
	}

	/**
	 * Repairs the consistency of all virtual networks. This is necessary if a
	 * substrate server was removed "dirty" from the model and the previously
	 * embedded virtual network is in a floating state. After detecting this state,
	 * the virtual network's embedding will be removed and it has to be embedded
	 * again.
	 *
	 * @return Set of virtual networks that have to be embedded again, because their
	 *         old embedding was invalid.
	 */
	@Override
	protected Set<VirtualNetwork> repairVirtualNetworks() {
		// Find all virtual networks that are floating
		final Set<VirtualNetwork> floatingGuests = sNet.getGuests().stream().filter(g -> modelFacade.checkIfFloating(g))
				.collect(Collectors.toSet());

		// Remove embedding of all elements of the virtual network so they can be
		// embedded again
		floatingGuests.forEach(g -> modelFacade.unembedVirtualNetwork(g));
		return floatingGuests;
	}

	/**
	 * Checks every condition necessary to run this algorithm. If a condition is not
	 * met, it throws an UnsupportedOperationException.
	 */
	@Override
	protected void checkPreConditions() {
		// Path creation has to be enabled for paths with length = 1
		if (ModelFacadeConfig.MIN_PATH_LENGTH != 1) {
			throw new UnsupportedOperationException("Minimum path length must be 1.");
		}

		// There must be generated substrate paths
		if (sNet.getPaths().isEmpty()) {
			throw new UnsupportedOperationException("Generated paths are missing in substrate network.");
		}
	}

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
	}

}
