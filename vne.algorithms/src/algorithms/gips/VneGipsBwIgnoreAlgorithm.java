package algorithms.gips;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.resource.ResourceSet;
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
public class VneGipsBwIgnoreAlgorithm extends AbstractAlgorithm {

	/**
	 * Relative base path of the GIPS MdVNE project.
	 */
	private final static String GIPS_PROJECT_BASE_PATH = "../../gips-examples/org.emoflon.gips.gipsl.examples.mdvne.bwignore";

	/**
	 * Algorithm instance (singleton).
	 */
	private static VneGipsBwIgnoreAlgorithm instance;

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public VneGipsBwIgnoreAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
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
		final ResourceSet model = ModelFacade.getInstance().getResourceSet();
		final boolean gipsSuccess = MdvneGipsBwIgnoreIflyeAdapter.execute(model,
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/bwignore/api/gips/gips-model.xmi",
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/bwignore/api/ibex-patterns.xmi",
				GIPS_PROJECT_BASE_PATH
						+ "/src-gen/org/emoflon/gips/gipsl/examples/mdvne/bwignore/hipe/engine/hipe-network.xmi");

		// The following workaround is not necessary because of the global bandwidth
		// ignoring needed for this VNE algorithm
//		// Workaround to fix the residual bandwidth of other paths possibly affected by
//		// virtual link to substrate path embeddings
//		ModelFacade.getInstance().updateAllPathsResidualBandwidth(sNet.getName());
		return gipsSuccess;
	}

	/**
	 * Initializes a new instance of the GIPS-based VNE algorithm.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 * @return Instance of this algorithm implementation.
	 */
	public static VneGipsBwIgnoreAlgorithm prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		if (sNet == null || vNets == null) {
			throw new IllegalArgumentException("One of the provided network objects was null.");
		}

		if (vNets.size() == 0) {
			throw new IllegalArgumentException("Provided set of virtual networks was empty.");
		}

		VneGipsAlgorithmUtils.checkGivenVnets(vNets);

		if (instance == null) {
			instance = new VneGipsBwIgnoreAlgorithm(sNet, vNets);
		}
		instance.sNet = sNet;
		instance.vNets = new HashSet<>();
		instance.vNets.addAll(vNets);

		return instance;
	}

	/**
	 * Resets the algorithm instance.
	 */
	public void dispose() {
		MdvneGipsBwIgnoreIflyeAdapter.resetInit();
		if (instance == null) {
			return;
		}
		instance = null;
	}

}
