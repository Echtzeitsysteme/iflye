package algorithms.pm;

import java.util.List;
import algorithms.pm.stages.VnePmMdvneAlgorithmPipelineStageRackA;
import algorithms.pm.stages.VnePmMdvneAlgorithmPipelineStageVnet;
import facade.ModelFacade;

/**
 * Implementation of the model-driven virtual network algorithm that uses
 * pattern matching as a way to reduce the search space of the ILP solver. This
 * implementation uses a three-stage pipeline approach with rack A
 * implementation
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineThreeStagesA extends VnePmMdvnePipelineAlgorithm {

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 */
	public VnePmMdvneAlgorithmPipelineThreeStagesA() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Constructor.
	 */
	public VnePmMdvneAlgorithmPipelineThreeStagesA(final ModelFacade modelFacade) {
		super(modelFacade, List.of(new VnePmMdvneAlgorithmPipelineStageVnet(modelFacade),
				new VnePmMdvneAlgorithmPipelineStageRackA(modelFacade), new VnePmMdvneAlgorithm(modelFacade)));
	}

}
