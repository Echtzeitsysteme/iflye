package algorithms.pm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import facade.config.ModelFacadeConfig;
import gt.IncrementalPatternMatcher;
import gt.PatternMatchingDelta;
import gt.PatternMatchingDelta.Match;
import gt.emoflon.EmoflonGt;
import gt.emoflon.EmoflonGtFactory;
import ilp.wrapper.IlpDelta;
import ilp.wrapper.IlpSolverException;
import ilp.wrapper.IncrementalIlpSolver;
import ilp.wrapper.Statistics;
import ilp.wrapper.config.IlpSolverConfig;
import ilp.wrapper.impl.IncrementalGurobiSolver;
import metrics.CostUtility;
import metrics.manager.GlobalMetricsManager;
import model.Link;
import model.Node;
import model.SubstrateElement;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;
import model.SubstratePath;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualElement;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualNode;
import model.VirtualServer;
import model.VirtualSwitch;

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
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 * @author Sebastian Ehmes {@literal <sebastian.ehmes@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithm extends AbstractAlgorithm {

	/**
	 * Algorithm instance (singleton).
	 */
	protected static VnePmMdvneAlgorithm instance;

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
	 * Set of ignored virtual networks. Ignored virtual networks are requests, that
	 * can not fit on the substrate network at all and are therefore ignored (as
	 * they are not given to the ILP solver).
	 */
	protected final Set<VirtualNetwork> ignoredVnets = new HashSet<VirtualNetwork>();

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 * 
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	protected VnePmMdvneAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
	}

	/**
	 * Initializes a new instance of the VNE pattern matching algorithm.
	 * 
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 * @return Instance of this algorithm implementation.
	 */
	public static VnePmMdvneAlgorithm prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		if (sNet == null || vNets == null) {
			throw new IllegalArgumentException("One of the provided network objects was null.");
		}

		if (vNets.size() == 0) {
			throw new IllegalArgumentException("Provided set of virtual networks was empty.");
		}

		if (instance == null) {
			instance = new VnePmMdvneAlgorithm(sNet, vNets);
		}
		instance.sNet = sNet;
		instance.vNets = new HashSet<VirtualNetwork>();
		instance.vNets.addAll(vNets);

		instance.checkPreConditions();
		return instance;
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

		delta2Ilp(delta);
		GlobalMetricsManager.measureMemory();
		final Set<VirtualNetwork> rejectedNetworks = solveIlp();

		rejectedNetworks.addAll(ignoredVnets);
		embedNetworks(rejectedNetworks);
		GlobalMetricsManager.endDeployTime();
		GlobalMetricsManager.measureMemory();
		return rejectedNetworks.isEmpty();
	}

	/**
	 * Solves the created ILP problem, embeds all accepted elements and returns a
	 * set of virtual networks that could not be embedded.
	 * 
	 * @return Set of virtual networks that could not be embedded.
	 */
	protected Set<VirtualNetwork> solveIlp() {
		GlobalMetricsManager.startIlpTime();
		final Statistics solve = ilpSolver.solve();
		GlobalMetricsManager.endIlpTime();
		Set<VirtualNetwork> rejectedNetworks = new HashSet<VirtualNetwork>();
		if (solve.isFeasible()) {
			GlobalMetricsManager.startDeployTime();
			rejectedNetworks = updateMappingsAndEmbed(ilpSolver.getMappings());
		} else {
			throw new IlpSolverException("Problem was infeasible.");
		}
		return rejectedNetworks;
	}

	/**
	 * Translates the given pattern matching delta to an ILP formulation.
	 * 
	 * @param delta Pattern matching delta to translate into an ILP formulation.
	 */
	protected void delta2Ilp(final PatternMatchingDelta delta) {
		final IlpDeltaGenerator gen = new IlpDeltaGenerator(ilpSolver, facade, variablesToMatch, 
				this::getCost, this::getRejectionCost);

		// add new elements
		addElementsToSolver(gen);

		// add new matches
		delta.getNewServerMatchPositives().stream()
				.filter(m -> !ignoredVnets.contains(((VirtualServer) m.getVirtual()).getNetwork()))
				.filter(m -> vNets.contains(((VirtualServer) m.getVirtual()).getNetwork()))
				.forEach(gen::addServerMatch);
		delta.getNewSwitchMatchPositives().stream()
				.filter(m -> !ignoredVnets.contains(((VirtualSwitch) m.getVirtual()).getNetwork()))
				.filter(m -> vNets.contains(((VirtualSwitch) m.getVirtual()).getNetwork()))
				.forEach(gen::addSwitchMatch);

		// Important: Due to the fact that both link constraint generating methods check
		// the existence
		// of the node mapping variables, the link constraints have to be added *after*
		// all node
		// constraints.
		delta.getNewLinkPathMatchPositives().stream()
				.filter(m -> !ignoredVnets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
				.filter(m -> vNets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
				.forEach(gen::addLinkPathMatch);
		delta.getNewLinkServerMatchPositives().stream()
				.filter(m -> !ignoredVnets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
				.filter(m -> vNets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
				.forEach(gen::addLinkServerMatch);

		// apply delta in ILP generator
		gen.apply();
	}

	/**
	 * Checks the overall resource availability for all nodes of all virtual
	 * networks and all nodes of the substrate network. If a network can not be
	 * placed on the substrate network at all, the method adds it to the set of
	 * ignored networks.
	 */
	protected void checkOverallResources() {
		// Calculate total residual resources for substrate servers
		// Datatype long is needed, because of the possible large values of substrate
		// server residual
		// resources (e.g. from the diss scenario).
		long subTotalResidualCpu = 0;
		long subTotalResidualMem = 0;
		long subTotalResidualSto = 0;

		for (final Node n : sNet.getNodes()) {
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

			for (final Node n : vNet.getNodes()) {
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
	 * Embeds all virtual networks that are not part of the given rejected networks
	 * set to the substrate network.
	 * 
	 * @param rejectedNetworks Set of virtual networks that could not be embedded.
	 */
	protected void embedNetworks(final Set<VirtualNetwork> rejectedNetworks) {
		for (final VirtualNetwork vNet : vNets) {
			if (!rejectedNetworks.contains(vNet)) {
				facade.embedNetworkToNetwork(sNet.getName(), vNet.getName());
			}
		}
	}

	/**
	 * Adds the elements of the substrate and the virtual network to the given delta
	 * generator (solver).
	 * 
	 * @param gen ILP delta generator to add elements to.
	 */
	protected void addElementsToSolver(final IlpDeltaGenerator gen) {
		// Substrate network
		for (final Node n : sNet.getNodes()) {
			if (n instanceof SubstrateServer) {
				gen.addNewSubstrateServer((SubstrateServer) n);
			} else if (n instanceof SubstrateSwitch) {
				// Nothing to do here
			}
		}

		for (final Link l : sNet.getLinks()) {
			if (l instanceof SubstrateLink) {
				gen.addNewSubstrateLink((SubstrateLink) l);
			}
		}

		// Virtual networks
		final Iterator<VirtualNetwork> it = vNets.iterator();
		while (it.hasNext()) {
			final VirtualNetwork vNet = it.next();
			if (ignoredVnets.contains(vNet)) {
				continue;
			}

			for (final Node n : vNet.getNodes()) {
				if (n instanceof VirtualServer) {
					gen.addNewVirtualServer((VirtualServer) n);
				} else if (n instanceof VirtualSwitch) {
					gen.addNewVirtualSwitch((VirtualSwitch) n);
				}
			}

			for (final Link l : vNet.getLinks()) {
				if (l instanceof VirtualLink) {
					gen.addNewVirtualLink((VirtualLink) l);
				}
			}

			// Network match
			gen.addNewNetworkMatch(new Match(vNet, sNet));
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
	protected Set<VirtualNetwork> updateMappingsAndEmbed(final Map<String, Boolean> mappings) {
		// Embed elements
		final Set<VirtualNetwork> rejectedNetworks = new HashSet<VirtualNetwork>();
		final EmoflonGt engine = (EmoflonGt) patternMatcher;

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
			case MANUAL:
				final VirtualElement ve = (VirtualElement) m.getVirtual();
				final SubstrateElement se = (SubstrateElement) m.getSubstrate();
				if (ve instanceof VirtualServer) {
					facade.embedServerToServer(se.getName(), ve.getName());
				} else if (ve instanceof VirtualSwitch) {
					facade.embedSwitchToNode(se.getName(), ve.getName());
				} else if (ve instanceof VirtualLink) {
					if (se instanceof SubstrateServer) {
						facade.embedLinkToServer(se.getName(), ve.getName());
					} else if (se instanceof SubstratePath) {
						facade.embedLinkToPath(se.getName(), ve.getName());
					}
				}
				break;
			}
		}

		return rejectedNetworks;
	}

	/**
	 * Initializes the algorithm by creating a new incremental solver object and a
	 * new pattern matcher object.
	 */
	public void init() {
		// Create new ILP solver object on every method call.
		ilpSolver = new IncrementalGurobiSolver(IlpSolverConfig.TIME_OUT, IlpSolverConfig.RANDOM_SEED);

		if (patternMatcher == null) {
			patternMatcher = new EmoflonGtFactory().create();
		}
	}

	/**
	 * Sets the substrate network to a given one. This method is needed for the
	 * child classes of this implementation.
	 * 
	 * @param sNet Substrate network to set.
	 */
	protected static void setSnet(final SubstrateNetwork sNet) {
		instance.sNet = sNet;
	}

	/**
	 * Sets the virtual networks to given ones. This method is needed for the child
	 * classes of this implementation.
	 * 
	 * @param vNets Virtual networks to set.
	 */
	protected static void setVnets(final Set<VirtualNetwork> vNets) {
		instance.vNets = vNets;
	}

	/*
	 * Cost functions.
	 */

	public double getCost(final VirtualElement virt, final SubstrateElement host) {
		if (virt instanceof Link) {
			return IlpSolverConfig.transformObj(getLinkCost((VirtualLink) virt, host));
		} else if (virt instanceof Node && host instanceof Node) {
			return IlpSolverConfig.transformObj(getNodeCost((VirtualNode) virt, (SubstrateNode) host));
		}

		throw new IllegalArgumentException();
	}

	public double getNodeCost(final VirtualNode virt, final SubstrateNode sub) {
		switch (AlgorithmConfig.obj) {
		case TOTAL_PATH_COST:
			return CostUtility.getTotalPathCostNode(virt, sub);
		case TOTAL_COMMUNICATION_COST_A:
			return CostUtility.getTotalCommunicationCostNodeAB();
		case TOTAL_COMMUNICATION_COST_B:
			return CostUtility.getTotalCommunicationCostNodeAB();
		case TOTAL_COMMUNICATION_COST_C:
			return CostUtility.getTotalCommunicationCostNodeC(virt, sub);
		case TOTAL_COMMUNICATION_COST_D:
			return CostUtility.getTotalCommunicationCostNodeD(virt, sub);
		default:
			throw new UnsupportedOperationException();
		}
	}

	public double getLinkCost(final VirtualLink virt, final SubstrateElement sub) {
		switch (AlgorithmConfig.obj) {
		case TOTAL_PATH_COST:
			return CostUtility.getTotalPathCostLink(sub);
		case TOTAL_COMMUNICATION_COST_A:
			return CostUtility.getTotalCommunicationCostLinkA(virt, sub);
		case TOTAL_COMMUNICATION_COST_B:
			return CostUtility.getTotalCommunicationCostLinkBCD(virt, sub);
		case TOTAL_COMMUNICATION_COST_C:
			return CostUtility.getTotalCommunicationCostLinkBCD(virt, sub);
		case TOTAL_COMMUNICATION_COST_D:
			return CostUtility.getTotalCommunicationCostLinkBCD(virt, sub);
		default:
			throw new UnsupportedOperationException();
		}
	}

	public double getNetRejCost(final VirtualNetwork vNet) {
		if (AlgorithmConfig.netRejCostDynamic) {
			return IlpSolverConfig.transformObj(CostUtility.getNetworkRejectionCost(vNet));
		} else {
			return IlpSolverConfig.transformObj(CostUtility.getNetworkRejectionCost());
		}
	}

	@Override
	public double getCost(Object... args) {
		return getCost((VirtualNode) args[0], (SubstrateNode) args[1]);
	}
	
	@Override
	public double getRejectionCost(Object...args) {
		return getNetRejCost((VirtualNetwork) args[0]);
	}

}
