package algorithms.gips;

import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.gips.core.milp.SolverOutput;
import org.emoflon.gips.gipsl.examples.mdvne.MdvneGipsIflyeAdapter;
import org.emoflon.gips.gipsl.examples.mdvne.bwignore.MdvneGipsBwIgnoreIflyeAdapter;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * GIPS-based VNE algorithm implementation that ignores all bandwidth
 * constraints.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsBwIgnoreAlgorithm extends AbstractAlgorithm implements GipsAlgorithm {

	/**
	 * Relative base path of the GIPS MdVNE project.
	 */
	private final static String GIPS_PROJECT_BASE_PATH = "../../gips-examples/org.emoflon.gips.gipsl.examples.mdvne.bwignore";

	/**
	 * The GIPS MdVNE adapter.
	 */
	private final MdvneGipsBwIgnoreIflyeAdapter iflyeAdapter;

	/**
	 * The most recent GIPS MdVNE output.
	 */
	private MdvneGipsIflyeAdapter.MdvneIflyeOutput iflyeOutput;

	/**
	 * Initialize the algorithm with the global model facade.
	 */
	public VneGipsBwIgnoreAlgorithm() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Initialize the algorithm with the given model facade.
	 * 
	 * @param modelFacade Model facade to work with.
	 */
	public VneGipsBwIgnoreAlgorithm(final ModelFacade modelFacade) {
		super(modelFacade);

		iflyeAdapter = new MdvneGipsBwIgnoreIflyeAdapter();
	}

	@Override
	public boolean execute() {
		// Check if correct objective is used
		if (AlgorithmConfig.obj != Objective.TOTAL_COMMUNICATION_OBJECTIVE_C) {
			throw new UnsupportedOperationException(
					"The VNE GIPS algorithm can only be used with the total communication cost C.");
		}

		if (!ModelFacadeConfig.IGNORE_BW) {
			throw new UnsupportedOperationException(
					"Bandwidth ignore must be globally enabled when using this VNE algorithm implementation.");
		}

		// TODO: Time measurement
		final ResourceSet model = getModelFacade().getResourceSet();
		iflyeOutput = iflyeAdapter.execute(model,
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/bwignore/api/gips/gips-model.xmi",
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/bwignore/api/ibex-patterns.xmi",
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/bwignore/hipe/engine/hipe-network.xmi");

		final boolean gipsSuccess = this.iflyeOutput.solverOutput().solutionCount() > 0;

		// The following workaround is not necessary because of the global bandwidth
		// ignoring needed for this VNE algorithm
//		// Workaround to fix the residual bandwidth of other paths possibly affected by
//		// virtual link to substrate path embeddings
//		getModelFacade().updateAllPathsResidualBandwidth(sNet.getName());
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

	/**
	 * Resets the algorithm instance.
	 */
	@Override
	public void dispose() {
		iflyeAdapter.resetInit();
	}

}
