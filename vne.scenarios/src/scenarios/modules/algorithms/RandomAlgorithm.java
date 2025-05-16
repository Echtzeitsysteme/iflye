package scenarios.modules.algorithms;

import java.util.function.Function;

import org.apache.commons.cli.CommandLine;

import algorithms.AbstractAlgorithm;
import algorithms.random.RandomVneAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import scenarios.load.Experiment;
import scenarios.modules.AbstractModule;
import scenarios.modules.AlgorithmModule;

/**
 * Add an option to configure the experiment to use the
 * {@link RandomVneAlgorithm}.
 * 
 * Options: -a / --algorithm <random>
 * 
 * @see {@link RandomVneAlgorithm}
 */
public class RandomAlgorithm extends AbstractModule implements AlgorithmModule.AlgorithmConfiguration {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(final AlgorithmModule algorithmModule) {
		algorithmModule.addAlgorithm("random", RandomVneAlgorithm::new);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<ModelFacade, AbstractAlgorithm> configure(final Experiment experiment, final String algoConfig,
			final CommandLine cmd, final Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) {
		if (algoConfig.equals("random")) {
			ModelFacadeConfig.IGNORE_BW = true;
		}

		return previousAlgoFactory;
	}

}
