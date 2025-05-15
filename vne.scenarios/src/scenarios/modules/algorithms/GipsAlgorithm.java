package scenarios.modules.algorithms;

import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import algorithms.AbstractAlgorithm;
import algorithms.gips.VneGipsAlgorithm;
import algorithms.gips.VneGipsBwIgnoreAlgorithm;
import algorithms.gips.VneGipsMigrationAlgorithm;
import algorithms.gips.VneGipsSeqAlgorithm;
import facade.ModelFacade;
import metrics.manager.MetricsManager;
import scenarios.load.Experiment;
import scenarios.modules.AbstractModule;
import scenarios.modules.AlgorithmModule;

/**
 * Add an option to configure the experiment to use the {@link VneGipsAlgorithm}
 * with different characteristics.
 * 
 * Options: --gips-solver-threads <arg>, -a / --algorithm
 * <gips/gips-mig/gips-seq/gips-bwignore>
 * 
 * @see {@link VneGipsAlgorithm}
 * @see {@link VneGipsMigrationAlgorithm}
 * @see {@link VneGipsSeqAlgorithm}
 * @see {@link VneGipsBwIgnoreAlgorithm}
 */
public class GipsAlgorithm extends AbstractModule implements AlgorithmModule.AlgorithmConfiguration {

	protected final Option gipsSolverThreadsOption = Option.builder()//
			.longOpt("gips-solver-threads")//
			.desc("number of threads for the GIPS ILP solver")//
			.hasArg()//
			.type(Integer.class)//
			.build();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(gipsSolverThreadsOption);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<ModelFacade, AbstractAlgorithm> getAlgorithmFactory(final Experiment experiment,
			final String algoConfig, final CommandLine cmd,
			final Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) throws ParseException {
		if (!algoConfig.startsWith("gips")) {
			return previousAlgoFactory;
		}

		final int gipsSolverThreads = cmd.getParsedOptionValue(gipsSolverThreadsOption, -1);
		if (gipsSolverThreads > 0) {
			MetricsManager.getInstance().addTags("gips.solver_threads", String.valueOf(gipsSolverThreads));
		}

		switch (algoConfig) {
		case "gips":
			return (modelFacade) -> new VneGipsAlgorithm(modelFacade, gipsSolverThreads);
		case "gips-mig":
			return VneGipsMigrationAlgorithm::new;
		case "gips-seq":
			return VneGipsSeqAlgorithm::new;
		case "gips-bwignore":
			return VneGipsBwIgnoreAlgorithm::new;
		default:
			return previousAlgoFactory;
		}
	}

}
