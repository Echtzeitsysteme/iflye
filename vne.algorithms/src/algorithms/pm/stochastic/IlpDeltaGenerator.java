package algorithms.pm.stochastic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.gt.api.GraphTransformationMatch;

import facade.ModelFacade;
import ilp.wrapper.IlpDelta;
import ilp.wrapper.IncrementalIlpSolver;
import model.ModelPackage;
import model.SubstrateNode;
import model.SubstrateServer;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import network.model.rules.stochastic.api.matches.EmbeddedNetworkMatch;
import network.model.rules.stochastic.api.matches.LinkPathMatchPositiveMatch;
import network.model.rules.stochastic.api.matches.LinkServerMatchPositiveMatch;
import network.model.rules.stochastic.api.matches.ServerMatchPositiveBackupMatch;
import network.model.rules.stochastic.api.matches.ServerMatchPositiveMatch;
import network.model.rules.stochastic.api.matches.SwitchNodeMatchPositiveMatch;

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
	final protected Map<String, List<String>> sosMappings = new HashMap<>();

	/**
	 * ModelFacade instance.
	 */
	final protected ModelFacade facade;

	/**
	 * Mapping of string (name) to matches.
	 */
	final protected Map<String, GraphTransformationMatch<?,?>> variablesToMatch;

	final protected Function<Object[], Double> costFunction;
	final protected Function<Object[], Double> rejectionCostFunction;
	
	protected Map<EObject, Map<String, EAttribute>> integerAttributeConstraints = new HashMap<>();
	protected Map<EObject, String> mappingConstraints = new HashMap<>();
	protected Map<EObject, String> optimizationVariables = new HashMap<>();

	public IlpDeltaGenerator(final IncrementalIlpSolver ilpSolver, final ModelFacade facade,
			final Map<String, GraphTransformationMatch<?,?>> variablesToMatch, final Function<Object[], Double> costFunction,
			final Function<Object[], Double> rejectionCostFunction) {
		this.ilpSolver = ilpSolver;
		this.facade = facade;
		this.variablesToMatch = variablesToMatch;
		this.costFunction = costFunction;
		this.rejectionCostFunction = rejectionCostFunction;
	}
	
	public IlpDelta getDelta() {
		return delta;
	}
	
	public void addMatch(final GraphTransformationMatch<?,?> match) {
		if(match instanceof EmbeddedNetworkMatch netMatch) {
			addNetworkMatch(netMatch);
		} else if(match instanceof ServerMatchPositiveMatch svrMatch) {
			addServerMatch(svrMatch);
		} else if(match instanceof ServerMatchPositiveBackupMatch svrbMatch) {
			addServerBackupMatch(svrbMatch);
		} else if(match instanceof SwitchNodeMatchPositiveMatch swMatch) {
			addSwitchMatch(swMatch);
		} else if(match instanceof LinkPathMatchPositiveMatch lpMatch) {
			addLinkPathMatch(lpMatch);
		} else if(match instanceof LinkServerMatchPositiveMatch lsMatch) {
			addLinkServerMatch(lsMatch);
		} else {
			throw new UnsupportedOperationException("Match type: " + match.getClass().getSimpleName()+" unsupported!");
		}
	}
	
	protected void addNetworkMatch(final EmbeddedNetworkMatch match) {
		VirtualNetwork vn = match.getVirtualNetwork();
		String variable = createNetworkVariable(vn);
		variablesToMatch.put(variable, match);
	}

	protected void addServerMatch(final ServerMatchPositiveMatch match) {
		final SubstrateServer sSrv = match.getSubstrateNode();
		final VirtualServer vSrv = match.getVirtualNode();
		final VirtualNetwork vNet = match.getVirtualNetwork();
		
		final String varName = vSrv.getName()+"_"+sSrv.getName();		
		delta.addVariable(varName, costFunction.apply(new Object[] { vSrv, sSrv }));
		
		Map<String, EAttribute> sSrvConstraints = createSubstrateServerConstraints(sSrv);
		sSrvConstraints.forEach((name, atr) -> {
			delta.setVariableWeightForConstraint(name, (int) vSrv.eGet(atr), varName);
		});
		
		String vsVariable = mappingConstraints.get(vSrv);
		if(vsVariable == null) {
			vsVariable = "vs_"+vSrv.getName();
			mappingConstraints.put(vSrv, vsVariable);
			
			delta.addEqualsConstraint(vsVariable, 1);
			String vnVariable = createNetworkVariable(vNet);
			delta.setVariableWeightForConstraint(vsVariable, 1, vnVariable);
		}
		
	}

	protected void addServerBackupMatch(final ServerMatchPositiveBackupMatch match) {
//		TODO: Later ...
	}
	
	protected void addSwitchMatch(final SwitchNodeMatchPositiveMatch match) {
		final SubstrateNode sn = match.getSubstrateNode();
		final VirtualSwitch sw = match.getVirtualSwitch();
		final VirtualNetwork vnet = match.getVirtualNetwork();
		
		final String varName = sw.getName()+"_"+sn.getName();		
		delta.addVariable(varName, costFunction.apply(new Object[] { sw, sn }));
		
		String swConstraint = createSwitchConstraint(sw);
		String netVariable = createNetworkVariable(vnet);
		
		delta.setVariableWeightForConstraint(swConstraint, 1, varName);
		delta.setVariableWeightForConstraint(swConstraint, 1, netVariable);
		
		variablesToMatch.put(varName, match);
	}
	
	protected void addLinkPathMatch(final LinkPathMatchPositiveMatch match) {
		
	}
	
	protected void addLinkServerMatch(final LinkServerMatchPositiveMatch match) {
		
	}
	
	protected String createNetworkVariable(final VirtualNetwork vnet) {
		String vnVariable = optimizationVariables.get(vnet);
		if(vnVariable != null) {
			return vnVariable;
		}
		
		vnVariable = "rej_"+vnet.getName();
		optimizationVariables.put(vnet, vnVariable);
		
		delta.addVariable(vnVariable, rejectionCostFunction.apply(new Object[] { vnet }));
		return vnVariable;
	}
	
	protected Map<String, EAttribute> createSubstrateServerConstraints(final SubstrateServer server) {
		Map<String, EAttribute> srvConstraints = integerAttributeConstraints.get(server);
		if(srvConstraints == null) {
			srvConstraints = new LinkedHashMap<>();
			integerAttributeConstraints.put(server, srvConstraints);
		} else {
			return srvConstraints;
		}
		
		String cpu = "cpu_"+server.getName();
		String mem = "mem_"+server.getName();
		String sto = "sto_"+server.getName();
		
		delta.addLessOrEqualsConstraint(cpu, (int) server.getResidualCpu());
		srvConstraints.put(cpu, ModelPackage.Literals.SERVER__CPU);
		delta.addLessOrEqualsConstraint(mem, (int) server.getResidualMemory());
		srvConstraints.put(mem, ModelPackage.Literals.SERVER__MEMORY);
		delta.addLessOrEqualsConstraint(sto, (int) server.getResidualStorage());
		srvConstraints.put(sto, ModelPackage.Literals.SERVER__STORAGE);
		
		return srvConstraints;
	}
	
	protected String createSwitchConstraint(final VirtualSwitch vswitch) {
		String swConstraint = mappingConstraints.get(vswitch);
		if(swConstraint != null) {
			return swConstraint;
		}
		
		swConstraint = "vsw_"+vswitch.getName();
		mappingConstraints.put(vswitch, swConstraint);
		
		delta.addEqualsConstraint(swConstraint, 1);
		
		return swConstraint;	
	}

}
