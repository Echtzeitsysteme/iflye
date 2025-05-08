package algorithms.pm;

import java.util.List;
import algorithms.pm.stages.VnePmMdvneAlgorithmPipelineStageRackB;
import facade.ModelFacade;

/**
 * Implementation of the model-driven virtual network algorithm that uses
 * pattern matching as a way to reduce the search space of the ILP solver. This
 * implementation uses a two-stage pipeline approach that first tries to embed a
 * whole virtual network onto a substrate rack B.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackB extends VnePmMdvnePipelineAlgorithm {

	/**
	 * Initialize the algorithm with the global model facade.
	 */
	public VnePmMdvneAlgorithmPipelineTwoStagesRackB() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Initialize the algorithm with the given model facade.
	 * 
	 * @param modelFacade Model facade to work with.
	 */
	public VnePmMdvneAlgorithmPipelineTwoStagesRackB(final ModelFacade modelFacade) {
		super(modelFacade,
				List.of(new VnePmMdvneAlgorithmPipelineStageRackB(modelFacade), new VnePmMdvneAlgorithm(modelFacade)));
	}

}
