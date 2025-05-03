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
import scenarios.load.DissScenarioLoad;
import scenarios.modules.AbstractModule;
import scenarios.modules.AlgorithmModule;

public class GipsAlgorithm extends AbstractModule implements AlgorithmModule.AlgorithmConfiguration {

	protected final Option gipsSolverThreadsOption = Option.builder()//
			.longOpt("gips-solver-threads")//
			.desc("number of threads for the GIPS ILP solver")//
			.hasArg()//
			.type(Integer.class)//
			.build();

	public GipsAlgorithm(final DissScenarioLoad experiment) {
		super(experiment);
	}

	@Override
	public void register(final Options options) {
		options.addOption(gipsSolverThreadsOption);
	}

	@Override
	public Function<ModelFacade, AbstractAlgorithm> getAlgorithmFactory(final String algoConfig, final CommandLine cmd,
			final Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) throws ParseException {
		if (!algoConfig.startsWith("gips")) {
			return previousAlgoFactory;
		}

		final int gipsSolverThreads = cmd.getParsedOptionValue(gipsSolverThreadsOption, -1);
		if (gipsSolverThreads > 0) {
			this.getExperiment().getMetricsManager().addTags("gips.solver_threads", String.valueOf(gipsSolverThreads));
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
