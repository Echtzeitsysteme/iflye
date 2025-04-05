package algorithms.pm;

import java.util.List;
import algorithms.pm.stages.VnePmMdvneAlgorithmPipelineStageVnet;
import facade.ModelFacade;

/**
 * Implementation of the model-driven virtual network algorithm that uses
 * pattern matching as a way to reduce the search space of the ILP solver. This
 * implementation uses a two-stage pipeline approach that first tries to embed a
 * whole virtual network onto a substrate server.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmPipelineTwoStagesVnet extends VnePmMdvnePipelineAlgorithm {

	/**
	 * Constructor that gets the substrate as well as the virtual network.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public VnePmMdvneAlgorithmPipelineTwoStagesVnet() {
		this(ModelFacade.getInstance());
	}

	/**
	 * Constructor.
	 */
	public VnePmMdvneAlgorithmPipelineTwoStagesVnet(final ModelFacade modelFacade) {
		super(modelFacade,
				List.of(new VnePmMdvneAlgorithmPipelineStageVnet(modelFacade), new VnePmMdvneAlgorithm(modelFacade)));
	}

}
