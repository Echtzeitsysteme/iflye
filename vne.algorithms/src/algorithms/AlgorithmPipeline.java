package algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import facade.ModelFacade;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Implementation of a generic algorithm pipeline
 *
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public abstract class AlgorithmPipeline extends AbstractAlgorithm {

	/**
	 * List of algorithms that are part of the pipeline.
	 */
	protected final List<AbstractAlgorithm> pipeline = new ArrayList<>();

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public AlgorithmPipeline() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param pipeline List of algorithms that are part of the pipeline.
	 */
	public AlgorithmPipeline(final Collection<AbstractAlgorithm> pipeline) {
		this(ModelFacade.getInstance(), pipeline);
	}

	/**
	 * Constructor.
	 */
	public AlgorithmPipeline(final ModelFacade modelFacade) {
		this(modelFacade, List.of());
	}

	/**
	 * Constructor that gets the ModelFacade and the algorithms that are part of the
	 * pipeline.
	 *
	 * @param modelFacade ModelFacade instance
	 * @param pipeline    List of algorithms that are part of the pipeline.
	 */
	public AlgorithmPipeline(final ModelFacade modelFacade, final Collection<AbstractAlgorithm> pipeline) {
		super(modelFacade);

		this.pipeline.addAll(pipeline);
	}

	/**
	 * Prepare the algorithm for execution
	 */
	@Override
	public void prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super.prepare(sNet, vNets);

		for (AbstractAlgorithm algo : pipeline) {
			algo.prepare(sNet, vNets);
		}
	}

	/**
	 * Resets the ILP solver and the pattern matcher.
	 */
	@Override
	public void dispose() {
		super.dispose();

		for (AbstractAlgorithm algo : pipeline) {
			algo.dispose();
		}
	}

	/**
	 * Execute the pipeline.
	 */
	@Override
	public boolean execute() {
		for (AbstractAlgorithm algo : pipeline) {
			if (algo.execute()) {
				return true;
			}
		}

		return false;
	}

}
