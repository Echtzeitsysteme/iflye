package algorithms.gips;

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.gips.core.milp.SolverOutput;
import org.emoflon.gips.gipsl.examples.mdvne.migration.MdvneMigrationGipsIflyeAdapter;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import facade.ModelFacade;
import model.Network;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * GIPS-based VNE algorithm implementation with always-on migration.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsMigrationAlgorithm extends AbstractAlgorithm implements GipsAlgorithm {

	/**
	 * Relative base path of the GIPS MdVNE project.
	 */
	private final static String GIPS_PROJECT_BASE_PATH = "../../gips-examples/org.emoflon.gips.gipsl.examples.mdvne.migration";

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public VneGipsMigrationAlgorithm() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Constructor.
	 */
	public VneGipsMigrationAlgorithm(final ModelFacade modelFacade) {
		super(modelFacade);
	}

	@Override
	public boolean execute() {
		// Check if correct objective is used
		if (AlgorithmConfig.obj != Objective.TOTAL_COMMUNICATION_OBJECTIVE_C) {
			throw new UnsupportedOperationException(
					"The VNE GIPS algorithm can only be used with the total communication cost C.");
		}

		// TODO: Time measurement

		// Remove all old embeddings
		for (final Network net : ModelFacade.getInstance().getAllNetworks()) {
			if (net instanceof VirtualNetwork vNet) {
				if (vNet.getHost() != null || vNet.getHostServer() != null) {
					ModelFacade.getInstance().removeNetworkEmbedding(vNet.getName());
				}
			}
		}

		// Sanity check
		ModelFacade.getInstance().validateModel();
		ModelFacade.getInstance().updateAllPathsResidualBandwidth(sNet.getName());

		final ResourceSet model = ModelFacade.getInstance().getResourceSet();
		final boolean gipsSuccess = MdvneMigrationGipsIflyeAdapter.execute(model,
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/migration/api/gips/gips-model.xmi",
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/migration/api/ibex-patterns.xmi",
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/migration/hipe/engine/hipe-network.xmi");

		// Workaround to fix the residual bandwidth of other paths possibly affected by
		// virtual link to substrate path embeddings
		ModelFacade.getInstance().updateAllPathsResidualBandwidth(sNet.getName());
		return gipsSuccess;
	}

	/**
	 * Initializes a new instance of the GIPS-based VNE algorithm.
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

		VneGipsAlgorithmUtils.checkGivenVnets(getModelFacade(), vNets);
		super.prepare(sNet, vNets);
	}

	@Override
	public SolverOutput getSolverOutput() {
		return null;
	}

	@Override
	public Map<String, String> getMatches() {
		return null;
	}

	/**
	 * Resets the algorithm instance.
	 */
	public void dispose() {
		MdvneMigrationGipsIflyeAdapter.resetInit();
	}

}
