package algorithms.pm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import algorithms.AlgorithmConfig;
import facade.ModelFacade;
import gt.PatternMatchingDelta.Match;
import ilp.wrapper.IlpDelta;
import ilp.wrapper.IncrementalIlpSolver;
import model.SubstrateLink;
import model.SubstrateNode;
import model.SubstratePath;
import model.SubstrateServer;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualNode;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * ILP delta generator that converts matches and given model objects into ILP
 * constraints for the solver.
 * 
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 * @author Sebastian Ehmes {@literal <sebastian.ehmes@es.tu-darmstadt.de>}
 */
public class IlpDeltaGenerator {

	/**
	 * Incremental ILP solver to use.
	 */
	final protected IncrementalIlpSolver ilpSolver;

	/**
	 * ILP delta object that holds all information.
	 */
	final protected IlpDelta delta = new IlpDelta();

	/**
	 * Mappings for the SOS1 constraints. Each virtual element IDs is a key and the
	 * corresponding value is a list of virtual to substrate element ID mappings.
	 * 
	 * Example: vlink1 -> {vlink1spath1, vlink1spath2, vlink2sserver3,
	 * vlink2sserver7}
	 */
	final protected Map<String, List<String>> sosMappings = new HashMap<String, List<String>>();

	/**
	 * ModelFacade instance.
	 */
	final protected ModelFacade facade;

	/**
	 * Mapping of string (name) to matches.
	 */
	final protected Map<String, Match> variablesToMatch;

	final protected Function<Object[], Double> costFunction;
	final protected Function<Object[], Double> rejectionCostFunction;

	public IlpDeltaGenerator(final IncrementalIlpSolver ilpSolver, final ModelFacade facade, final Map<String, Match> variablesToMatch,
			final Function<Object[], Double> costFunction, final Function<Object[], Double> rejectionCostFunction) {
		this.ilpSolver = ilpSolver;
		this.facade = facade;
		this.variablesToMatch = variablesToMatch;
		this.costFunction = costFunction;
		this.rejectionCostFunction = rejectionCostFunction;
	}

	/**
	 * Adds a SOS1 mapping to the collection. This method immediately returns, if
	 * the algorithm configuration option for SOS1 constraints is disabled.
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

		if (!sosMappings.containsKey(v)) {
			sosMappings.put(v, new LinkedList<String>());
		}
		sosMappings.get(v).add(vs);
	}

	/**
	 * Adds a new match from a virtual to a substrate network.
	 * 
	 * @param match Match to get information from.
	 */
	public void addNewNetworkMatch(final Match match) {
		final VirtualNetwork vNet = (VirtualNetwork) match.getVirtual();
		delta.addVariable("rej" + vNet.getName(), rejectionCostFunction.apply(new Object[] { vNet }));
		variablesToMatch.put("rej" + vNet.getName(), match);
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
		// the substrate
		// node, it's mapping variable is missing in the solver's model. Due to the fact
		// that there is
		// no way to properly map the source node (target node) onto this substrate
		// server, the ILP
		// solver does not have to deal with the embedding of the link for this
		// particular substrate
		// node, to.
		final String sourceVarName = vLink.getSource().getName() + "_" + match.getSubstrate().getName();
		final String targetVarName = vLink.getTarget().getName() + "_" + match.getSubstrate().getName();

		if (!delta.hasAddVariable(sourceVarName) || !delta.hasAddVariable(targetVarName)) {
			return;
		}
		delta.addVariable(varName, costFunction.apply(new Object[] { vLink, (SubstrateNode) match.getSubstrate() }));
		delta.setVariableWeightForConstraint("vl" + match.getVirtual().getName(), 1, varName);
		delta.addLessOrEqualsConstraint("req" + varName, 0, new int[] { 2, -1, -1 },
				new String[] { varName, sourceVarName, targetVarName });
		variablesToMatch.put(varName, match);

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
		// the substrate
		// paths source node (target node), it's mapping variable is missing in the
		// solver's model.
		// Due to the fact that there is no way to properly map the source node (target
		// node) onto the
		// substrate source node (target node), the ILP solver does not have to deal
		// with the
		// embedding of the link for this particular substrate node, to.
		// This may e.g. be the case if the virtual node is a server but the substrate
		// node is a
		// switch.
		final String sourceVarName = vLink.getSource().getName() + "_" + sPath.getSource().getName();
		final String targetVarName = vLink.getTarget().getName() + "_" + sPath.getTarget().getName();

		if (!delta.hasAddVariable(sourceVarName) || !delta.hasAddVariable(targetVarName)) {
			return;
		}

		final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();

		delta.addVariable(varName, costFunction.apply(new Object[] { vLink, sPath }));
		delta.setVariableWeightForConstraint("vl" + match.getVirtual().getName(), 1, varName);
		delta.addLessOrEqualsConstraint("req" + varName, 0, new int[] { 2, -1, -1 },
				new String[] { varName, sourceVarName, targetVarName });
		PmAlgorithmUtils.forEachLink(sPath,
				l -> delta.setVariableWeightForConstraint("sl" + l.getName(), vLink.getBandwidth(), varName));
		variablesToMatch.put(varName, match);

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
		delta.addVariable(varName,
				costFunction.apply(new Object[] { vServer, (SubstrateServer) match.getSubstrate() }));
		delta.setVariableWeightForConstraint("vs" + match.getVirtual().getName(), 1, varName);

		delta.setVariableWeightForConstraint("cpu" + match.getSubstrate().getName(), vServer.getCpu(), varName);
		delta.setVariableWeightForConstraint("mem" + match.getSubstrate().getName(), vServer.getMemory(), varName);
		delta.setVariableWeightForConstraint("sto" + match.getSubstrate().getName(), vServer.getStorage(), varName);
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
		delta.addVariable(varName, costFunction
				.apply(new Object[] { (VirtualNode) match.getVirtual(), (SubstrateNode) match.getSubstrate() }));
		delta.setVariableWeightForConstraint("vw" + match.getVirtual().getName(), 1, varName);
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
		delta.addLessOrEqualsConstraint("cpu" + server.getName(), (int) server.getResidualCpu());
		delta.addLessOrEqualsConstraint("mem" + server.getName(), (int) server.getResidualMemory());
		delta.addLessOrEqualsConstraint("sto" + server.getName(), (int) server.getResidualStorage());
	}

	/**
	 * Adds a new substrate link.
	 * 
	 * @param link SubstrateLink to get information from.
	 */
	public void addNewSubstrateLink(final SubstrateLink link) {
		delta.addLessOrEqualsConstraint("sl" + link.getName(), link.getResidualBandwidth());
	}

	/**
	 * Adds a new virtual server.
	 * 
	 * @param server VirtualServer to get information from.
	 */
	public void addNewVirtualServer(final VirtualServer server) {
		delta.addEqualsConstraint("vs" + server.getName(), 1);
		delta.setVariableWeightForConstraint("vs" + server.getName(), 1, "rej" + server.getNetwork().getName());
	}

	/**
	 * Adds a new virtual switch.
	 * 
	 * @param sw VirtualSwitch to get information from.
	 */
	public void addNewVirtualSwitch(final VirtualSwitch sw) {
		delta.addEqualsConstraint("vw" + sw.getName(), 1);
		delta.setVariableWeightForConstraint("vw" + sw.getName(), 1, "rej" + sw.getNetwork().getName());
	}

	/**
	 * Adds a new virtual link.
	 * 
	 * @param link VirtualLink to get information from.
	 */
	public void addNewVirtualLink(final VirtualLink link) {
		delta.addEqualsConstraint("vl" + link.getName(), 1);
		delta.setVariableWeightForConstraint("vl" + link.getName(), 1, "rej" + link.getNetwork().getName());
	}

	/**
	 * Applies the delta to the ILP solver object.
	 */
	public void apply() {
		// Before applying the ILP delta, add all SOS1 constraint mappings to it. This
		// can't be done
		// beforehand, because the individual mappings may be extended while adding more
		// matches.
		for (final String key : sosMappings.keySet()) {
			delta.addSosConstraint(key, sosMappings.get(key));
		}
		delta.apply(ilpSolver);
	}

}
