package algorithms.gips;

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.gips.core.milp.SolverOutput;
import org.emoflon.gips.gipsl.examples.mdvne.MdvneGipsIflyeAdapter;
import org.emoflon.gips.gipsl.examples.mdvne.heap.MdvneGipsHeapIflyeAdapter;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import facade.ModelFacade;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * GIPS-based VNE algorithm implementation with an inverse objective function.
 * I.e., this algorithm uses a heap-like allocation and prefers completely empty
 * substrate resources.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsHeapAlgorithm extends AbstractAlgorithm implements GipsAlgorithm {

	/**
	 * Relative base path of the GIPS MdVNE project.
	 */
	private final static String GIPS_PROJECT_BASE_PATH = "../../gips-examples/org.emoflon.gips.gipsl.examples.mdvne.heap";

	/**
	 * The GIPS MdVNE adapter.
	 */
	private final MdvneGipsHeapIflyeAdapter iflyeAdapter;

	/**
	 * The most recent GIPS MdVNE output.
	 */
	private MdvneGipsIflyeAdapter.MdvneIflyeOutput iflyeOutput;

	/**
	 * Initialize the algorithm with the global model facade.
	 */
	public VneGipsHeapAlgorithm() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Initialize the algorithm with the given model facade.
	 * 
	 * @param modelFacade Model facade to work with.
	 */
	public VneGipsHeapAlgorithm(final ModelFacade modelFacade) {
		super(modelFacade);

		iflyeAdapter = new MdvneGipsHeapIflyeAdapter();
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
				GIPS_PROJECT_BASE_PATH + "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/heap/api/gips/gips-model.xmi",
				GIPS_PROJECT_BASE_PATH + "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/heap/api/ibex-patterns.xmi",
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/heap/hipe/engine/hipe-network.xmi");

		final boolean gipsSuccess = this.iflyeOutput.solverOutput().solutionCount() > 0;

		// Workaround to fix the residual bandwidth of other paths possibly affected by
		// virtual link to substrate path embeddings
		getModelFacade().updateAllPathsResidualBandwidth(sNet.getName());

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
		VneGipsAlgorithmUtils.checkGivenVnets(getModelFacade(), vNets);

		super.prepare(sNet, vNets);
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
	@Override
	public void dispose() {
		iflyeAdapter.resetInit();
	}

}
