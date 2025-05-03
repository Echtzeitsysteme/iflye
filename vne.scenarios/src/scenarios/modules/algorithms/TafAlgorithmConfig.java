package scenarios.modules.algorithms;

import java.util.function.Function;

import org.apache.commons.cli.CommandLine;

import algorithms.AbstractAlgorithm;
import algorithms.heuristics.TafAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import scenarios.load.DissScenarioLoad;
import scenarios.modules.AbstractModule;
import scenarios.modules.AlgorithmModule;

public class TafAlgorithmConfig extends AbstractModule implements AlgorithmModule.AlgorithmConfiguration {

	public TafAlgorithmConfig(final DissScenarioLoad experiment) {
		super(experiment);
	}

	@Override
	public Function<ModelFacade, AbstractAlgorithm> getAlgorithmFactory(final String algoConfig, final CommandLine cmd,
			final Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) {
		if (algoConfig.equals("taf")) {
			ModelFacadeConfig.IGNORE_BW = true;
			return TafAlgorithm::new;
		}

		return previousAlgoFactory;
	}
}
