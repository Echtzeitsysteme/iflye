package algorithms.pm.stochastic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.gt.api.GraphTransformationMatch;

import algorithms.AbstractAlgorithm;
import facade.config.ModelFacadeConfig;
import gt.IncrementalPatternMatcher;
import gt.PatternMatchingDelta.Match;
import ilp.wrapper.IlpDelta;
import ilp.wrapper.IncrementalIlpSolver;
import ilp.wrapper.config.IlpSolverConfig;
import ilp.wrapper.impl.IncrementalGurobiSolver;
import model.Link;
import model.Node;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import sgt.emoflon.EmoflonSgt;
import sgt.emoflon.EmoflonSgtFactory;

public class VneSgtMdvneAlgorithm extends AbstractAlgorithm {

	/**
	 * Algorithm instance (singleton).
	 */
	protected static VneSgtMdvneAlgorithm instance;

	/**
	 * Incremental pattern matcher to use.
	 */
	protected EmoflonSgt patternMatcher;

	/**
	 * Incremental ILP solver to use.
	 */
	protected IncrementalIlpSolver ilpSolver;

	/**
	 * Mapping of string (name) to matches.
	 */
	protected final Map<String, GraphTransformationMatch<?,?>> variablesToMatch = new HashMap<>();

	protected VneSgtMdvneAlgorithm(SubstrateNetwork sNet, Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
	}

	public static synchronized VneSgtMdvneAlgorithm instance(final SubstrateNetwork sNet,
			final Set<VirtualNetwork> vNets) {
		if (sNet == null || vNets == null) {
			throw new IllegalArgumentException("One of the provided network objects was null.");
		}

		if (vNets.size() == 0) {
			throw new IllegalArgumentException("Provided set of virtual networks was empty.");
		}

		if (instance == null) {
			instance = new VneSgtMdvneAlgorithm(sNet, vNets);
		}
		instance.sNet = sNet;
		instance.vNets = new HashSet<VirtualNetwork>();
		instance.vNets.addAll(vNets);

		instance.checkPreConditions();
		return instance;
	}

	/**
	 * Checks every condition necessary to run this algorithm. If a condition is not
	 * met, it throws an UnsupportedOperationException.
	 */
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
	 * Resets the ILP solver and the pattern matcher.
	 */
	public void dispose() {
		if (instance == null) {
			return;
		}
		if (this.ilpSolver != null) {
			this.ilpSolver.dispose();
		}
		if (this.patternMatcher != null) {
			this.patternMatcher.dispose();
		}
		instance = null;
	}

	/**
	 * Initializes the algorithm by creating a new incremental solver object and a
	 * new pattern matcher object.
	 */
	public void init() {
		// Create new ILP solver object on every method call.
		ilpSolver = new IncrementalGurobiSolver(IlpSolverConfig.TIME_OUT, IlpSolverConfig.RANDOM_SEED);

		if (patternMatcher == null) {
			patternMatcher = (EmoflonSgt) new EmoflonSgtFactory().create();
		}
	}

	

	@Override
	public boolean execute() {
		init();
		
		// Repair model consistency: Substrate network
		repairSubstrateNetwork();
		
		// Repair model consistency: Virtual network(s)
		final Set<VirtualNetwork> repairedVnets = repairVirtualNetworks();
		vNets.addAll(repairedVnets);
		
		patternMatcher.run();
		
		// Create ILP problem and run...
		IlpDelta delta = delta2Ilp();
		delta.apply(ilpSolver);
		
		return false;
	}

	@Override
	public double getCost(Object... args) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRejectionCost(Object... args) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Repairs the consistency of the substrate network. This is necessary, if a
	 * virtual network was removed "dirty" from the model and the residual values or
	 * guest references are not updated properly.
	 */
	protected void repairSubstrateNetwork() {
		// Find all networks that were removed in the meantime
		final Set<VirtualNetwork> removedGuests = sNet.getGuests().stream()
				.filter(g -> !facade.networkExists(g.getName())).collect(Collectors.toSet());

		// Remove embedding of all elements of the virtual network
		removedGuests.forEach(g -> facade.unembedVirtualNetwork(g));
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
	protected Set<VirtualNetwork> repairVirtualNetworks() {
		// Find all virtual networks that are floating
		final Set<VirtualNetwork> floatingGuests = sNet.getGuests().stream().filter(g -> facade.checkIfFloating(g))
				.collect(Collectors.toSet());

		// Remove embedding of all elements of the virtual network so they can be
		// embedded again
		floatingGuests.forEach(g -> facade.unembedVirtualNetwork(g));
		return floatingGuests;
	}
	
	/**
	 * Translates the given pattern matching delta to an ILP formulation.
	 * 
	 * @param delta Pattern matching delta to translate into an ILP formulation.
	 */
	protected IlpDelta delta2Ilp() {
		final IlpDeltaGenerator gen = new IlpDeltaGenerator(ilpSolver, facade, variablesToMatch, 
				this::getCost, this::getRejectionCost);
		
		patternMatcher.getMappingRules().forEach((name, rule) -> {
			rule.findMatches().forEach(match -> {
				gen.addMatch(match);
			});
		});
		
		// return delta for ILP generator
		return gen.getDelta();
	}

}
