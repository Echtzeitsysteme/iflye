package algorithms.pm.stages;

import java.util.Map;
import java.util.function.Function;

import algorithms.pm.IlpDeltaGenerator;
import facade.ModelFacade;
import gt.PatternMatchingDelta.Match;
import ilp.wrapper.IncrementalIlpSolver;
import model.SubstrateServer;
import model.VirtualNetwork;

/**
 * ILP delta generator that converts matches and given model objects into ILP
 * constraints for the solver.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
class IlpDeltaGeneratorVnet extends IlpDeltaGenerator {

	public IlpDeltaGeneratorVnet(IncrementalIlpSolver ilpSolver, ModelFacade facade,
			Map<String, Match> variablesToMatch, Function<Object[], Double> costFunction,
			Function<Object[], Double> rejectionCostFunction) {
		super(ilpSolver, facade, variablesToMatch, costFunction, rejectionCostFunction);
	}

	/**
	 * Translates and adds a match from a virtual network to a substrate server.
	 * 
	 * @param match Match to get information from.
	 */
	public void addNetworkToServerMatch(final Match match) {
		final VirtualNetwork vnet = (VirtualNetwork) match.getVirtual();
		final String varName = match.getVirtual().getName() + "_" + match.getSubstrate().getName();
		delta.addVariable(varName, costFunction.apply(new Object[] { vnet, (SubstrateServer) match.getSubstrate() }));
		delta.setVariableWeightForConstraint("vsnet" + match.getVirtual().getName(), 1, varName);

		delta.setVariableWeightForConstraint("cpu" + match.getSubstrate().getName(), vnet.getCpu(), varName);
		delta.setVariableWeightForConstraint("mem" + match.getSubstrate().getName(), vnet.getMemory(), varName);
		delta.setVariableWeightForConstraint("sto" + match.getSubstrate().getName(), vnet.getStorage(), varName);
		variablesToMatch.put(varName, match);

		// SOS match
		addSosMappings(match.getVirtual().getName(), varName);
	}

	/**
	 * Adds a new virtual network.
	 * 
	 * @param vnet VirtualNetwork to get information from.
	 */
	public void addNewVirtualNetwork(final VirtualNetwork vnet) {
		delta.addEqualsConstraint("vsnet" + vnet.getName(), 1);
		delta.setVariableWeightForConstraint("vsnet" + vnet.getName(), 1, "rej" + vnet.getName());
	}

}
