package algorithms.roam;

import java.util.HashSet;
import java.util.Set;

import org.emoflon.gips.gipsl.examples.mdvne.MdvneGipsIflyeAdapter;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import facade.ModelFacade;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Roam-based VNE algorithm implementation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VneRoamAlgorithm extends AbstractAlgorithm {

	/**
	 * Algorithm instance (singleton).
	 */
	private static VneRoamAlgorithm instance;

	/**
	 * Default model saving path. Must be used for Roam to load the model.
	 */
	final private static String MODEL_FILE_PATH = "model-roam-algo-in.xmi";

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public VneRoamAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
	}

	@Override
	public boolean execute() {
		// Check if correct objective is used
		if (AlgorithmConfig.obj != Objective.TOTAL_COMMUNICATION_OBJECTIVE_C) {
			throw new UnsupportedOperationException(
					"The VNE Roam algorithm can only be used with the total communication cost C.");
		}

		// TODO: Time measurement
		ModelFacade.getInstance().persistModel(MODEL_FILE_PATH);
		final boolean roamSuccess = MdvneGipsIflyeAdapter.execute(MODEL_FILE_PATH);
		if (roamSuccess) {
			// Propagate solution to iflye model facade
			ModelFacade.getInstance().loadModel(MODEL_FILE_PATH);

			// Current workaround: Embed all virtual networks "by hand" if Roam run was
			// successful
			for (final VirtualNetwork v : vNets) {
				facade.embedNetworkToNetwork(sNet.getName(), v.getName());
			}
		}
		return roamSuccess;
	}

	/**
	 * Initializes a new instance of the Roam-based VNE algorithm.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 * @return Instance of this algorithm implementation.
	 */
	public static VneRoamAlgorithm prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		if (sNet == null || vNets == null) {
			throw new IllegalArgumentException("One of the provided network objects was null.");
		}

		if (vNets.size() == 0) {
			throw new IllegalArgumentException("Provided set of virtual networks was empty.");
		}

		if (instance == null) {
			instance = new VneRoamAlgorithm(sNet, vNets);
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
	}

}
