package algorithms.gips;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.gips.gipsl.examples.mdvne.MdvneGipsIflyeAdapter;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import facade.ModelFacade;
import model.Root;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * GIPS-based VNE algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneGipsAlgorithm extends AbstractAlgorithm {

	/**
	 * Algorithm instance (singleton).
	 */
	private static VneGipsAlgorithm instance;

	/**
	 * Default model saving path. Must be used for GIPS to load the model.
	 */
	final public static String MODEL_FILE_PATH = "model-gips-algo-in.xmi";

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public VneGipsAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
	}

	@Override
	public boolean execute() {
		// Check if correct objective is used
		if (AlgorithmConfig.obj != Objective.TOTAL_COMMUNICATION_OBJECTIVE_C) {
			throw new UnsupportedOperationException(
					"The VNE GIPS algorithm can only be used with the total communication cost C.");
		}

		// TODO: Time measurement
//		ModelFacade.getInstance().persistModel(MODEL_FILE_PATH);
//		final ResourceSet model = ModelFacade.getInstance().getRoot().eResource().getResourceSet();
//		final Root root = ModelFacade.getInstance().getRoot();
		
		final ResourceSet model = ModelFacade.getInstance().getResourceSet();
		final boolean gipsSuccess = MdvneGipsIflyeAdapter.execute(model);
		
//		if (gipsSuccess) {
//			// Propagate solution to iflye model facade
//			ModelFacade.getInstance().loadModel(MODEL_FILE_PATH);
//		}
		return gipsSuccess;
	}

	/**
	 * Initializes a new instance of the GIPS-based VNE algorithm.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 * @return Instance of this algorithm implementation.
	 */
	public static VneGipsAlgorithm prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		if (sNet == null || vNets == null) {
			throw new IllegalArgumentException("One of the provided network objects was null.");
		}

		if (vNets.size() == 0) {
			throw new IllegalArgumentException("Provided set of virtual networks was empty.");
		}

		if (instance == null) {
			instance = new VneGipsAlgorithm(sNet, vNets);
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
		if (instance == null) {
			return;
		}
		instance = null;

		final File out = new File(VneGipsAlgorithm.MODEL_FILE_PATH);
		out.delete();
	}

}
