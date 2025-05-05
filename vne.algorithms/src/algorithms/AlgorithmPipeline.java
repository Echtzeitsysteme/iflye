package algorithms;

import java.util.List;
import java.util.Set;

import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Implementation of a generic algorithm pipeline
 *
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public interface AlgorithmPipeline extends Algorithm {

	/**
	 * List of algorithms that are part of the pipeline.
	 */
	public List<AbstractAlgorithm> getPipeline();

	/**
	 * Prepare the algorithm for execution
	 */
	@Override
	default public void prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		for (AbstractAlgorithm algo : getPipeline()) {
			algo.prepare(sNet, vNets);
		}
	}

	/**
	 * Resets the ILP solver and the pattern matcher.
	 */
	@Override
	default public void dispose() {
		for (AbstractAlgorithm algo : getPipeline()) {
			algo.dispose();
		}
	}

	/**
	 * Execute the pipeline.
	 */
	@Override
	default public boolean execute() {
		for (AbstractAlgorithm algo : getPipeline()) {
			if (algo.execute()) {
				return true;
			}
		}

		return false;
	}

}
