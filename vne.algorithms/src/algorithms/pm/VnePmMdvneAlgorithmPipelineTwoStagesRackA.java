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
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public VnePmMdvneAlgorithmPipelineTwoStagesRackA() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Constructor.
	 */
	public VnePmMdvneAlgorithmPipelineTwoStagesRackA(final ModelFacade modelFacade) {
		super(modelFacade,
				List.of(new VnePmMdvneAlgorithmPipelineStageRackA(modelFacade), new VnePmMdvneAlgorithm(modelFacade)));
	}

}
