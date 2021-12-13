package algorithms.ilp;

import java.util.Set;

import org.cardygan.ilp.api.model.Model;

import ilp.wrapper.config.IlpSolverConfig;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Implementation of the ILP formulation of paper [1].
 *
 * Parts of this implementation are heavily inspired, taken or adapted from the
 * idyve project [2].
 *
 * [1] Tomaszek S., Leblebici E., Wang L., Schürr A. (2018) Virtual Network
 * Embedding: Reducing the Search Space by Model Transformation Techniques. In:
 * Rensink A., Sánchez Cuadrado J. (eds) Theory and Practice of Model
 * Transformation. ICMT 2018. Lecture Notes in Computer Science, vol 10888.
 * Springer, Cham
 *
 * [2] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in
 * Rechenzentren, http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI
 * 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VneIlpPathAlgorithmBatch extends VneIlpPathAlgorithm {

	/**
	 * Creates a new instance of this VNE ILP path algorithm.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public VneIlpPathAlgorithmBatch(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
	}

	@Override
	public boolean execute() {
		GlobalMetricsManager.measureMemory();

		// Add all currently embedded networks to job list
		sNet.getGuests().forEach(guest -> {
			vNets.add(guest);
		});
		// Remove embedding of every virtual network, that is currently embedded on
		// substrate one
		vNets.forEach(vn -> {
			if (vn.getHost() != null) {
				facade.removeNetworkEmbedding(vn.getName());
			}
		});

		model = new Model();
		createNetworkInformation();
		createAllVariables();

		// Every virtual link is mapped to one substrate link (path)
		// Every virtual node must be mapped exactly to one substrate node
		addConstraintsEveryVirtualElementIsMappedExactlyOnce();

		createAllNodeConstraints();

		// The start/target node of the virtual link are mapped to the start/target node
		// of the
		// substrate node. The bandwidth requirements are fulfilled.
		createAllLinkConstraints();

		createMinOveralCostsObjective();

		GlobalMetricsManager.startIlpTime();
		GlobalMetricsManager.measureMemory();
		ilpResult = model.solve(IlpSolverConfig.getSolver());
		GlobalMetricsManager.endIlpTime();

		if (isFeasible(ilpResult.getStatistics())) {
			// Node results
			for (int v = 0; v < nodeVariables.length; v++) {
				for (int s = 0; s < nodeVariables[v].length; s++) {
					if (ilpResult.getSolutions().getOrDefault(nodeVariables[v][s], -1.0) > 0.5) {
						// The value 0.5 is important because the ILP variables are internally
						// represented as
						// double values and, therefore, rounding errors must be taken into account
						resultVirtualToSubstrateNodes.put(virtualNodes.get(v), substrateNodes.get(s));
					}
				}
			}

			// Link results
			for (int v = 0; v < pathVariables.length; v++) {
				for (int s = 0; s < pathVariables[v].length; s++) {
					if (ilpResult.getSolutions().getOrDefault(pathVariables[v][s], -1.0) > 0.5) {
						// The value 0.5 is important because the ILP variables are internally
						// represented as
						// double values and, therefore, rounding errors must be taken into account
						resultVirtualToSubstrateLink.put(virtualLinks.get(v),
								allSubstratePaths.get(s).getLinksOrServer());
					}
				}
			}
		} else {
			System.err.println("Problem was infeasible.");
		}

		GlobalMetricsManager.startDeployTime();
		createEmbeddings();
		GlobalMetricsManager.endDeployTime();
		GlobalMetricsManager.measureMemory();

		return isFeasible(ilpResult.getStatistics());
	}

}
