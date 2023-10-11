package algorithms.ilp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.emoflon.ilp.BinaryVariable;
import org.emoflon.ilp.LinearConstraint;
import org.emoflon.ilp.LinearTerm;
import org.emoflon.ilp.Operator;
import org.emoflon.ilp.Problem;
import org.emoflon.ilp.SOS1Constraint;
import org.emoflon.ilp.Solver;
import org.emoflon.ilp.SolverConfig;
import org.emoflon.ilp.SolverConfig.SolverType;
import org.emoflon.ilp.SolverOutput;
import org.emoflon.ilp.Term;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import facade.config.ModelFacadeConfig;
import gt.PatternMatchingDelta;
import gt.PatternMatchingDelta.Match;
import ilp.wrapper.IlpSolverException;
import ilp.wrapper.config.IlpSolverConfig;
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
 * Implementation of the ILP fake algorithm.
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
public class VneFakeIlpAlgorithm extends AbstractAlgorithm {

	/**
	 * ILP delta generator that converts matches and given model objects into ILP
	 * constraints for the solver.
	 *
	 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
	 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
	 */
	public class IlpDeltaGenerator {
		protected Problem problem = new Problem();
		private Map<String, BinaryVariable> vars = new HashMap<>();

		public IlpDeltaGenerator() {
			this.problem.setObjective();
		}

		/**
		 * Adds a SOS1 mapping to the problem. This method immediately returns, if the
		 * algorithm configuration option for SOS1 constraints is disabled.
		 *
		 * @param v  Virtual element ID.
		 * @param vs Virtual to substrate element ID mapping.
		 */
		public void addSosMappings(final String v, final String vs) {
			// If the algorithm configuration for the SOS constraint feature is disabled,
			// return.
			if (!AlgorithmConfig.pmSosEnabled) {
				return;
			}

			if (!problem.hasConstraintWithName(v)) {
				final SOS1Constraint sos = new SOS1Constraint();
				sos.setName("sos1_" + v);
				problem.add(sos);
			}
			((SOS1Constraint) problem.getConstraintByName("sos1_" + v)).addVariable(vars.get(vs));
		}

		/**
		 * Adds a new match from a virtual to a substrate network.
		 *
		 * @param match Match to get information from.
		 */
		public void addNewNetworkMatch(final Match match) {
			final VirtualNetwork vNet = (VirtualNetwork) match.getVirtual();
			variablesToMatch.put("rej" + vNet.getName(), match);
			final BinaryVariable v = new BinaryVariable("rej" + vNet.getName());
			this.vars.put("rej" + vNet.getName(), v);
			problem.getObjective().addTerm(v, getNetRejCost(vNet));
		}

		/**
		 * Adds a match from a virtual link to a substrate server.
		 *
		 * @param match Match to get information from.
		 */
		public void addLinkServerMatch(final Match match) {
			final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();
			final VirtualLink vLink = (VirtualLink) facade.getLinkById(match.getVirtual().getName());

			// If the source node (target node) of the virtual link may not be embedded to
			// the substrate node, it's mapping variable is missing in the solver's model.
			// Due to the fact that there is no way to properly map the source node (target
			// node) onto this substrate server, the ILP solver does not have to deal with
			// the embedding of the link for this particular substrate node, to.
			final String sourceVarName = vLink.getSource().getName() + "_" + match.getSubstrate().getName();
			final String targetVarName = vLink.getTarget().getName() + "_" + match.getSubstrate().getName();

			if (!vars.containsKey(sourceVarName) || !vars.containsKey(targetVarName)) {
				return;
			}

			variablesToMatch.put(varName, match);
			final BinaryVariable v = new BinaryVariable(varName);
			this.vars.put(varName, v);
			problem.getObjective().addTerm(v, getCost(vLink, (SubstrateNode) match.getSubstrate()));

			((LinearConstraint) problem.getConstraintByName("vl" + match.getVirtual().getName())).addTerm(v, 1);

			final List<Term> cTerms = new ArrayList<Term>();
			final Term linkVar = new LinearTerm(v, 2);
			final Term sourceVar = new LinearTerm(vars.get(sourceVarName), -1);
			final Term targetVar = new LinearTerm(vars.get(targetVarName), -1);
			cTerms.add(linkVar);
			cTerms.add(sourceVar);
			cTerms.add(targetVar);
			final LinearConstraint c = new LinearConstraint(cTerms, Operator.LESS_OR_EQUAL, 0);
			c.setName("req" + varName);
			problem.add(c);

			// SOS match
			addSosMappings(match.getVirtual().getName(), varName);
		}

		/**
		 * Adds a match from a virtual link to a substrate path.
		 *
		 * @param match Match to get information from.
		 */
		public void addLinkPathMatch(final Match match) {
			final VirtualLink vLink = (VirtualLink) facade.getLinkById(match.getVirtual().getName());
			final SubstratePath sPath = facade.getPathById(match.getSubstrate().getName());

			// If the source node (target node) of the virtual link may not be embedded to
			// the substrate paths source node (target node), it's mapping variable is
			// missing in the solver's model. Due to the fact that there is no way to
			// properly map the source node (target node) onto the substrate source node
			// (target node), the ILP solver does not have to deal with the embedding of the
			// link for this particular substrate node, to. This may e.g. be the case if the
			// virtual node is a server but the substrate node is a switch.
			final String sourceVarName = vLink.getSource().getName() + "_" + sPath.getSource().getName();
			final String targetVarName = vLink.getTarget().getName() + "_" + sPath.getTarget().getName();

			if (!vars.containsKey(sourceVarName) || !vars.containsKey(targetVarName)) {
				return;
			}
			final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();

			variablesToMatch.put(varName, match);
			final BinaryVariable v = new BinaryVariable(varName);
			this.vars.put(varName, v);
			problem.getObjective().addTerm(v, getCost(vLink, sPath));

			((LinearConstraint) problem.getConstraintByName("vl" + match.getVirtual().getName())).addTerm(v, 1);

			final List<Term> cTerms = new ArrayList<Term>();
			final Term linkVar = new LinearTerm(v, 2);
			final Term sourceVar = new LinearTerm(vars.get(sourceVarName), -1);
			final Term targetVar = new LinearTerm(vars.get(targetVarName), -1);
			cTerms.add(linkVar);
			cTerms.add(sourceVar);
			cTerms.add(targetVar);
			final LinearConstraint c = new LinearConstraint(cTerms, Operator.LESS_OR_EQUAL, 0);
			c.setName("req" + varName);
			problem.add(c);

			// For each link
			forEachLink(sPath, l -> {
				((LinearConstraint) problem.getConstraintByName("sl" + l.getName())).addTerm(v, vLink.getBandwidth());
			});

			// SOS match
			addSosMappings(match.getVirtual().getName(), varName);
		}

		/**
		 * Adds a (positive) match from a virtual server to a substrate server.
		 *
		 * @param match Match to get information from.
		 */
		public void addServerMatch(final Match match) {
			final VirtualServer vServer = (VirtualServer) facade.getServerById(match.getVirtual().getName());
			final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();

			final BinaryVariable v = new BinaryVariable(varName);
			problem.getObjective().addTerm(v, getCost(vServer, (SubstrateServer) match.getSubstrate()));
			((LinearConstraint) problem.getConstraintByName("vs" + match.getVirtual().getName())).addTerm(v, 1);

			((LinearConstraint) problem.getConstraintByName("cpu" + match.getSubstrate().getName())).addTerm(v,
					vServer.getCpu());
			((LinearConstraint) problem.getConstraintByName("mem" + match.getSubstrate().getName())).addTerm(v,
					vServer.getMemory());
			((LinearConstraint) problem.getConstraintByName("sto" + match.getSubstrate().getName())).addTerm(v,
					vServer.getStorage());

			this.vars.put(varName, v);

			variablesToMatch.put(varName, match);

			// SOS match
			addSosMappings(match.getVirtual().getName(), varName);
		}

		/**
		 * Adds a match from a virtual switch to a substrate switch.
		 *
		 * @param match Match to get information from.
		 */
		public void addSwitchMatch(final Match match) {
			final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();

			final BinaryVariable v = new BinaryVariable(varName);
			problem.getObjective().addTerm(v,
					getCost((VirtualNode) match.getVirtual(), (SubstrateNode) match.getSubstrate()));
			((LinearConstraint) problem.getConstraintByName("vw" + match.getVirtual().getName())).addTerm(v, 1);

			this.vars.put(varName, v);

			variablesToMatch.put(varName, match);

			// SOS match
			addSosMappings(match.getVirtual().getName(), varName);
		}

		/**
		 * Adds a new substrate server.
		 *
		 * @param server SubstrateServer to get information from.
		 */
		public void addNewSubstrateServer(final SubstrateServer server) {
			final LinearConstraint cpu = new LinearConstraint(Operator.LESS_OR_EQUAL, server.getResidualCpu());
			cpu.setName("cpu" + server.getName());
			final LinearConstraint mem = new LinearConstraint(Operator.LESS_OR_EQUAL, server.getResidualMemory());
			mem.setName("mem" + server.getName());
			final LinearConstraint sto = new LinearConstraint(Operator.LESS_OR_EQUAL, server.getResidualStorage());
			sto.setName("sto" + server.getName());

			problem.add(cpu);
			problem.add(mem);
			problem.add(sto);
		}

		/**
		 * Adds a new substrate link.
		 *
		 * @param link SubstrateLink to get information from.
		 */
		public void addNewSubstrateLink(final SubstrateLink link) {
			final LinearConstraint sl = new LinearConstraint(Operator.LESS_OR_EQUAL, link.getResidualBandwidth());
			sl.setName("sl" + link.getName());
			problem.add(sl);
		}

		/**
		 * Adds a new virtual server.
		 *
		 * @param server VirtualServer to get information from.
		 */
		public void addNewVirtualServer(final VirtualServer server) {
			final LinearConstraint vs = new LinearConstraint(Operator.EQUAL, 1);
			vs.setName("vs" + server.getName());
			vs.addTerm(this.vars.get("rej" + server.getNetwork().getName()), 1);
			problem.add(vs);
		}

		/**
		 * Adds a new virtual switch.
		 *
		 * @param sw VirtualSwitch to get information from.
		 */
		public void addNewVirtualSwitch(final VirtualSwitch sw) {
			final LinearConstraint vs = new LinearConstraint(Operator.EQUAL, 1);
			vs.setName("vw" + sw.getName());
			vs.addTerm(this.vars.get("rej" + sw.getNetwork().getName()), 1);
			problem.add(vs);
		}

		/**
		 * Adds a new virtual link.
		 *
		 * @param link VirtualLink to get information from.
		 */
		public void addNewVirtualLink(final VirtualLink link) {
			final LinearConstraint vl = new LinearConstraint(Operator.EQUAL, 1);
			vl.setName("vl" + link.getName());
			vl.addTerm(this.vars.get("rej" + link.getNetwork().getName()), 1);
			problem.add(vl);
		}

		/**
		 * Returns the underlying ILP problem.
		 * 
		 * @return ILP problem.
		 */
		public Problem getProblem() {
			return this.problem;
		}

	}

	/**
	 * Algorithm instance (singleton).
	 */
	protected static VneFakeIlpAlgorithm instance;

	/**
	 * eMoflon-ILP solver to use.
	 */
	protected Solver solver;

	/**
	 * Mapping of string (name) to matches.
	 */
	protected final Map<String, Match> variablesToMatch = new HashMap<>();

	/**
	 * Set of ignored virtual networks. Ignored virtual networks are requests, that
	 * can not fit on the substrate network at all and are therefore ignored (as
	 * they are not given to the ILP solver).
	 */
	protected final Set<VirtualNetwork> ignoredVnets = new HashSet<>();

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	protected VneFakeIlpAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
	}

	/**
	 * Initializes a new instance of the VNE fake ILP algorithm.
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
			instance = new VneFakeIlpAlgorithm(sNet, vNets);
		}
		instance.sNet = sNet;
		instance.vNets = new HashSet<>();
		instance.vNets.addAll(vNets);

		instance.checkPreConditions();
		return instance;
	}

	/**
	 * Resets the ILP solver.
	 */
	public void dispose() {
		if (instance == null) {
			return;
		}
		if (this.solver != null) {
			this.solver.terminate();
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
		vNets.addAll(repairedVnets);

		GlobalMetricsManager.startPmTime();
		final PatternMatchingDelta delta = createFakeMatches();
		GlobalMetricsManager.endPmTime();

		final Problem problem = delta2Ilp(delta);
		GlobalMetricsManager.measureMemory();
		final Set<VirtualNetwork> rejectedNetworks = solveIlp(problem);

		rejectedNetworks.addAll(ignoredVnets);
		embedNetworks(rejectedNetworks);
		GlobalMetricsManager.endDeployTime();
		GlobalMetricsManager.measureMemory();
		return rejectedNetworks.isEmpty();
	}

	/**
	 * Creates all possible combinations of matches.
	 *
	 * @return PatternMatchingDelta with all possible combinations of matches.
	 */
	protected PatternMatchingDelta createFakeMatches() {
		final PatternMatchingDelta delta = new PatternMatchingDelta();

		for (final VirtualNetwork actVNet : this.vNets) {
			// Virtual servers
			for (final Node actInnerNode : facade.getAllServersOfNetwork(actVNet.getName())) {
				final VirtualServer actVSrv = (VirtualServer) actInnerNode;

				for (final Node actOuterNode : facade.getAllServersOfNetwork(sNet.getName())) {
					final SubstrateServer actSSrv = (SubstrateServer) actOuterNode;
					delta.addServerMatchPositive(actVSrv, actSSrv);
				}
			}

			// Virtual switches
			for (final Node actInnerNode : facade.getAllSwitchesOfNetwork(actVNet.getName())) {
				final VirtualSwitch actVSw = (VirtualSwitch) actInnerNode;

				// To substrate switches
				for (final Node actOuterNode : facade.getAllSwitchesOfNetwork(sNet.getName())) {
					final SubstrateSwitch actSSw = (SubstrateSwitch) actOuterNode;
					delta.addSwitchMatchPositive(actVSw, actSSw);
				}

				// To substrate servers
				for (final Node actOuterNode : facade.getAllServersOfNetwork(sNet.getName())) {
					final SubstrateServer actSSrv = (SubstrateServer) actOuterNode;
					delta.addSwitchMatchPositive(actVSw, actSSrv);
				}
			}

			// Virtual links
			for (final Link actInnerLink : facade.getAllLinksOfNetwork(actVNet.getName())) {
				final VirtualLink actVL = (VirtualLink) actInnerLink;

				// To substrate paths
				for (final SubstratePath actOuterPath : facade.getAllPathsOfNetwork(sNet.getName())) {
					delta.addLinkPathMatchPositive(actVL, actOuterPath);
				}

				// To substrate servers
				for (final Node actOuterNode : facade.getAllServersOfNetwork(sNet.getName())) {
					final SubstrateServer actSSrv = (SubstrateServer) actOuterNode;
					delta.addLinkServerMatchPositive(actVL, actSSrv);
				}
			}
		}

		return delta;
	}

	/**
	 * Solves the created ILP problem, embeds all accepted elements and returns a
	 * set of virtual networks that could not be embedded.
	 *
	 * @return Set of virtual networks that could not be embedded.
	 */
	protected Set<VirtualNetwork> solveIlp(final Problem problem) {
		GlobalMetricsManager.startIlpTime();
		solver.buildILPProblem(problem);
		final SolverOutput out = solver.solve();
		solver.updateValuesFromSolution();
		GlobalMetricsManager.endIlpTime();
		Set<VirtualNetwork> rejectedNetworks = new HashSet<>();
		// Solution found?
		if (out.getSolCount() > 0) {
			GlobalMetricsManager.startDeployTime();
			rejectedNetworks = updateMappingsAndEmbed(getMappings(problem));
		} else {
			throw new IlpSolverException("Problem was infeasible.");
		}
		return rejectedNetworks;
	}

	/**
	 * Returns the mappings for a given (already solved) ILP problem instance.
	 * 
	 * @param problem Already solved ILP problem instance.
	 * @return Map of mappings (key = ID, value = true -> embed).
	 */
	private Map<String, Boolean> getMappings(final Problem problem) {
		return problem.getVariables().values().stream().collect(Collectors.toMap(v -> {
			return ((BinaryVariable) v).getName();
		}, v -> {
			return ((BinaryVariable) v).getValue() > 0.5;
		}));
	}

	/**
	 * Translates the given pattern matching delta to an ILP formulation.
	 *
	 * @param delta Pattern matching delta to translate into an ILP formulation.
	 */
	protected Problem delta2Ilp(final PatternMatchingDelta delta) {
		final IlpDeltaGenerator gen = new IlpDeltaGenerator();

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
		// the existence of the node mapping variables, the link constraints have to be
		// added *after* all node constraints.
		delta.getNewLinkPathMatchPositives().stream()
				.filter(m -> !ignoredVnets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
				.filter(m -> vNets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
				.forEach(gen::addLinkPathMatch);
		delta.getNewLinkServerMatchPositives().stream()
				.filter(m -> !ignoredVnets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
				.filter(m -> vNets.contains(((VirtualLink) m.getVirtual()).getNetwork()))
				.forEach(gen::addLinkServerMatch);

		// apply delta in ILP generator
		return gen.getProblem();
	}

	/**
	 * Checks the overall resource availability for all nodes of all virtual
	 * networks and all nodes of the substrate network. If a network can not be
	 * placed on the substrate network at all, the method adds it to the set of
	 * ignored networks.
	 */
	protected void checkOverallResources() {
		// Calculate total residual resources for substrate servers datatype long is
		// needed, because of the possible large values of substrate server residual
		// resources (e.g., from the diss scenario).
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

			// Network match
			gen.addNewNetworkMatch(new Match(vNet, sNet));

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
		final Set<VirtualNetwork> rejectedNetworks = new HashSet<>();

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

			// Embed element: Only use manual mode.
			switch (AlgorithmConfig.emb) {
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
			default:
				throw new UnsupportedOperationException("You have to use embedding mode = MANUAL.");
			}
		}

		return rejectedNetworks;
	}

	/**
	 * Initializes the algorithm.
	 */
	public void init() {
		final SolverConfig config = new SolverConfig();

		config.setDebugOutputEnabled(IlpSolverConfig.ENABLE_ILP_OUTPUT);
		switch (IlpSolverConfig.solver) {
		case GUROBI:
			config.setSolver(SolverType.GUROBI);
			break;
		case CPLEX:
			config.setSolver(SolverType.CPLEX);
			break;
		case GLPK:
			config.setSolver(SolverType.GLPK);
			break;
		}
		config.setTimeoutEnabled(true);
		config.setTimeout(IlpSolverConfig.TIME_OUT);
		config.setRandomSeedEnabled(true);
		config.setRandomSeed(IlpSolverConfig.RANDOM_SEED);

		this.solver = (new org.emoflon.ilp.SolverHelper(config)).getSolver();
	}

	/**
	 * Execute a consumer for each link of a given path object.
	 * 
	 * @param sPath     Substrate path to execute an action for each link for.
	 * @param operation The action to execute.
	 */
	public void forEachLink(final SubstratePath sPath, final Consumer<? super Link> operation) {
		sPath.getLinks().stream().forEach(operation);
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
		case TOTAL_COMMUNICATION_OBJECTIVE_C:
			return CostUtility.getTotalCommunicationCostObjectiveNodeC(virt, sub);
		case TOTAL_COMMUNICATION_OBJECTIVE_D:
			return CostUtility.getTotalCommunicationCostObjectiveNodeD(virt, sub);
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
		case TOTAL_COMMUNICATION_OBJECTIVE_C:
			return CostUtility.getTotalCommunicationCostLinkBCD(virt, sub);
		case TOTAL_COMMUNICATION_OBJECTIVE_D:
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

}
