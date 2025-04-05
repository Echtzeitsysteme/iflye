package algorithms.pm;

import java.util.List;
import algorithms.pm.stages.VnePmMdvneAlgorithmPipelineStageRackB;
import algorithms.pm.stages.VnePmMdvneAlgorithmPipelineStageVnet;
import facade.ModelFacade;

/**
 * Implementation of the model-driven virtual network algorithm that uses
 * pattern matching as a way to reduce the search space of the ILP solver. This
 * implementation uses a three-stage pipeline approach with rack B
 * implementation
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineThreeStagesB extends VnePmMdvnePipelineAlgorithm {

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 */
	public VnePmMdvneAlgorithmPipelineThreeStagesB() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Constructor.
	 */
	public VnePmMdvneAlgorithmPipelineThreeStagesB(final ModelFacade modelFacade) {
		super(modelFacade, List.of(new VnePmMdvneAlgorithmPipelineStageVnet(modelFacade),
				new VnePmMdvneAlgorithmPipelineStageRackB(modelFacade), new VnePmMdvneAlgorithm(modelFacade)));
	}

}
