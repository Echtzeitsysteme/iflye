package algorithms.pm;

import java.util.List;
import algorithms.pm.stages.VnePmMdvneAlgorithmPipelineStageRackA;
import facade.ModelFacade;

/**
 * Implementation of the model-driven virtual network algorithm that uses
 * pattern matching as a way to reduce the search space of the ILP solver. This
 * implementation uses a two-stage pipeline approach that first tries to embed a
 * whole virtual network onto a substrate rack A.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesRackA extends VnePmMdvnePipelineAlgorithm {

	/**
	 * Initialize the algorithm with the global model facade.
	 */
	public VnePmMdvneAlgorithmPipelineTwoStagesRackA() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Initialize the algorithm with the given model facade.
	 * 
	 * @param modelFacade Model facade to work with.
	 */
	public VnePmMdvneAlgorithmPipelineTwoStagesRackA(final ModelFacade modelFacade) {
		super(modelFacade,
				List.of(new VnePmMdvneAlgorithmPipelineStageRackA(modelFacade), new VnePmMdvneAlgorithm(modelFacade)));
	}

}
