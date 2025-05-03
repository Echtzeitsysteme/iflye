package scenarios.modules.algorithms;

import java.util.function.Function;

import org.apache.commons.cli.CommandLine;

import algorithms.AbstractAlgorithm;
import algorithms.random.RandomVneAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import scenarios.load.DissScenarioLoad;
import scenarios.modules.AbstractModule;
import scenarios.modules.AlgorithmModule;

public class RandomAlgorithm extends AbstractModule implements AlgorithmModule.AlgorithmConfiguration {

	public RandomAlgorithm(final DissScenarioLoad experiment) {
		super(experiment);
	}

	@Override
	public Function<ModelFacade, AbstractAlgorithm> getAlgorithmFactory(final String algoConfig, final CommandLine cmd,
			final Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) {
		if (algoConfig.equals("random")) {
			ModelFacadeConfig.IGNORE_BW = true;
			return RandomVneAlgorithm::new;
		}

		return previousAlgoFactory;
	}
}
