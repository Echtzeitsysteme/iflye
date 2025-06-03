package algorithms.gips;

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.gips.core.milp.SolverOutput;
import org.emoflon.gips.core.util.IMeasurement;
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

	/**
	 * The GIPS MdVNE adapter.
	 */
	private final MdvneGipsIflyeAdapter iflyeAdapter;

	/**
	 * The most recent GIPS MdVNE output.
	 */
	private MdvneGipsIflyeAdapter.MdvneIflyeOutput iflyeOutput;

	/**
	 * Initializes a new GIPS algorithm with the global model facade.
	 */
	public VneGipsAlgorithm() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Create a new GIPS algorithm instance with the given model facade.
	 * 
	 * @param modelFacade The model facade to use.
	 */
	public VneGipsAlgorithm(final ModelFacade modelFacade) {
		this(modelFacade, 0);
	}

	/**
	 * Constructor.
	 * 
	 * @param modelFacade              the model facade to use
	 * @param numberOfIlpSolverThreads the number of threads to use for the ILP
	 *                                 solver
	 */
	public VneGipsAlgorithm(final ModelFacade modelFacade, final int numberOfIlpSolverThreads) {
		super(modelFacade);

		iflyeAdapter = new MdvneGipsIflyeAdapter();

		if (numberOfIlpSolverThreads > 0) {
			iflyeAdapter.setIlpSolverThreadCount(numberOfIlpSolverThreads);
		}
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
		return this.iflyeOutput != null ? this.iflyeOutput.solverOutput() : null;
	}

	@Override
	public Map<String, String> getMatches() {
		return this.iflyeOutput != null ? this.iflyeOutput.matches() : null;
	}

	@Override
	public Map<String, IMeasurement> getMeasurements() {
		return this.iflyeOutput != null ? this.iflyeOutput.measurements() : null;
	}

	/**
	 * Resets the algorithm instance.
	 */
	@Override
	public void dispose() {
		iflyeAdapter.resetInit();
	}

}
