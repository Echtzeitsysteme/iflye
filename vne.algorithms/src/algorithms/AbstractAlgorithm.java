package algorithms;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import facade.ModelFacade;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * An abstract algorithm class that acts as a common type for embedding
 * algorithms.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public abstract class AbstractAlgorithm implements Algorithm {

	/**
	 * ModelFacade instance.
	 */
	protected ModelFacade modelFacade;

	/**
	 * The substrate network (model).
	 */
	protected SubstrateNetwork sNet;

	/**
	 * The virtual networks (model).
	 */
	protected Set<VirtualNetwork> vNets;

	/**
	 * Execution method that starts the algorithm itself.
	 *
	 * @return True if embedding process was successful.
	 */
	@Override
	public abstract boolean execute();

	/**
	 * Initializes a new abstract algorithm
	 */
	public AbstractAlgorithm() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Initializes a new abstract algorithm
	 *
	 * @param modelFacade The ModelFacade to use
	 */
	public AbstractAlgorithm(final ModelFacade modelFacade) {
		Objects.requireNonNull(modelFacade);

		this.modelFacade = modelFacade;
	}

	/**
	 * Prepare the algorithm for execution
	 * 
	 * @param sNet  Substrate network to work with.
	 * @param vNets A set of virtual networks to work with.
	 */
	@Override
	public void prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		if (sNet == null || vNets == null) {
			throw new IllegalArgumentException("One of the provided network objects was null!");
		}

		if (vNets.size() == 0) {
			throw new IllegalArgumentException("Provided set of virtual networks was empty.");
		}

		this.sNet = sNet;
		this.vNets = new HashSet<>();
		this.vNets.addAll(vNets);
	}

	@Override
	public void dispose() {
	}

	@Override
	public String getAlgorithmName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Returns the first virtual network from this super type.
	 *
	 * @return First virtual network from this super type.
	 */
	protected VirtualNetwork getFirstVnet() {
		final Iterator<VirtualNetwork> it = vNets.iterator();
		return it.next();
	}

	/**
	 * Returns the currently used ModelFacade instance.
	 * 
	 * @return The used ModelFacade instance.
	 */
	public ModelFacade getModelFacade() {
		return this.modelFacade;
	}

}
