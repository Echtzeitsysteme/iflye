package algorithms.gips;

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.gips.core.milp.SolverOutput;
import org.emoflon.gips.gipsl.examples.mdvne.MdvneGipsIflyeAdapter;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import facade.ModelFacade;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * GIPS-based VNE algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsAlgorithm extends AbstractAlgorithm implements GipsAlgorithm {

	/**
	 * Relative base path of the GIPS MdVNE project.
	 */
	private final static String GIPS_PROJECT_BASE_PATH = "../../gips-examples/org.emoflon.gips.gipsl.examples.mdvne";

	private final MdvneGipsIflyeAdapter iflyeAdapter;

	private MdvneGipsIflyeAdapter.MdvneIflyeOutput iflyeOutput;

	/**
	 * Initializes a new abstract algorithm
	 */
	public VneGipsAlgorithm() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Constructor.
	 */
	public VneGipsAlgorithm(final ModelFacade modelFacade) {
		super(modelFacade);

		iflyeAdapter = new MdvneGipsIflyeAdapter();
	}

	@Override
	public void prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		VneGipsAlgorithmUtils.checkGivenVnets(getModelFacade(), vNets);

		super.prepare(sNet, vNets);
	}

	@Override
	public boolean execute() {
		// Check if correct objective is used
		if (AlgorithmConfig.obj != Objective.TOTAL_COMMUNICATION_OBJECTIVE_C) {
			throw new UnsupportedOperationException(
					"The VNE GIPS algorithm can only be used with the total communication cost C.");
		}

		// TODO: Time measurement
		final ResourceSet model = getModelFacade().getResourceSet();
		this.iflyeOutput = iflyeAdapter.execute(model,
				GIPS_PROJECT_BASE_PATH + "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/api/gips/gips-model.xmi",
				GIPS_PROJECT_BASE_PATH + "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/api/ibex-patterns.xmi",
				GIPS_PROJECT_BASE_PATH + "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/hipe/engine/hipe-network.xmi");

		final boolean gipsSuccess = this.iflyeOutput.solverOutput().solutionCount() > 0;

		// Workaround to fix the residual bandwidth of other paths possibly affected by
		// virtual link to substrate path embeddings
		getModelFacade().updateAllPathsResidualBandwidth(sNet.getName());

		return gipsSuccess;
	}

	@Override
	public SolverOutput getSolverOutput() {
		return this.iflyeOutput.solverOutput();
	}

	@Override
	public Map<String, String> getMatches() {
		return this.iflyeOutput.matches();
	}

	/**
	 * Resets the algorithm instance.
	 */
	public void dispose() {
		iflyeAdapter.resetInit();
	}

}
