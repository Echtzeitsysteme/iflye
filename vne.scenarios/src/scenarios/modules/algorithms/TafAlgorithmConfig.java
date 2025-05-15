package scenarios.modules.algorithms;

import java.util.function.Function;

import org.apache.commons.cli.CommandLine;

import algorithms.AbstractAlgorithm;
import algorithms.heuristics.TafAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import scenarios.load.Experiment;
import scenarios.modules.AbstractModule;
import scenarios.modules.AlgorithmModule;

/**
 * Add an option to configure the experiment to use the {@link TafAlgorithm}.
 * 
 * Options: -a / --algorithm <taf>
 * 
 * @see {@link TafAlgorithm}
 */
public class TafAlgorithmConfig extends AbstractModule implements AlgorithmModule.AlgorithmConfiguration {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<ModelFacade, AbstractAlgorithm> getAlgorithmFactory(final Experiment experiment,
			final String algoConfig, final CommandLine cmd,
			final Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) {
		if (algoConfig.equals("taf")) {
			ModelFacadeConfig.IGNORE_BW = true;
			return TafAlgorithm::new;
		}

		return previousAlgoFactory;
	}

}
