package scenarios.modules.algorithms;

import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import algorithms.AbstractAlgorithm;
import algorithms.ilp.VneFakeIlpAlgorithm;
import algorithms.ilp.VneFakeIlpBatchAlgorithm;
import facade.ModelFacade;
import ilp.wrapper.config.IlpSolverConfig;
import metrics.manager.MetricsManager;
import scenarios.load.Experiment;
import scenarios.modules.AbstractModule;
import scenarios.modules.AlgorithmModule;

/**
 * Add options to configure the experiment to use the
 * {@link VneFakeIlpAlgorithm}.
 * 
 * Options: -i / --ilptimeout <arg>, -r / --ilprandomseed <arg>, -m /
 * --ilpopttol <arg>, -y / --ilpobjscaling <arg>, -x / --ilpobjlog, -a /
 * --algorithm <ilp/ilp-batch>
 * 
 * @see {@link VneFakeIlpAlgorithm}
 * @see {@link VneFakeIlpBatchAlgorithm}
 */
public class IlpAlgorithm extends AbstractModule implements AlgorithmModule.AlgorithmConfiguration {
	protected final Option ilpTimeout = Option.builder()//
			.option("i")//
			.longOpt("ilptimeout")//
			.desc("ILP solver timeout value in seconds")//
			.hasArg()//
			.build();

	protected final Option ilpRandomSeed = Option.builder()//
			.option("r")//
			.longOpt("ilprandomseed")//
			.desc("ILP solver random seed")//
			.hasArg()//
			.build();

	protected final Option ilpOptTol = Option.builder()//
			.option("m")//
			.longOpt("ilpopttol")//
			.desc("ILP solver optimality tolerance")//
			.hasArg()//
			.build();

	protected final Option ilpObjScaling = Option.builder()//
			.option("y")//
			.longOpt("ilpobjscaling")//
			.desc("ILP solver objective scaling")//
			.hasArg()//
			.build();

	protected final Option ilpObjLog = Option.builder()//
			.option("x")//
			.longOpt("ilpobjlog")//
			.desc("ILP solver objective logarithm")//
			.build();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(ilpTimeout);
		options.addOption(ilpRandomSeed);
		options.addOption(ilpOptTol);
		options.addOption(ilpObjScaling);
		options.addOption(ilpObjLog);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		if (cmd.getOptionValue("ilptimeout") != null) {
			IlpSolverConfig.TIME_OUT = Integer.valueOf(cmd.getOptionValue("ilptimeout"));
			MetricsManager.getInstance().addTags("ilptimeout", cmd.getOptionValue("ilptimeout"));
		}

		if (cmd.getOptionValue("ilprandomseed") != null) {
			IlpSolverConfig.RANDOM_SEED = Integer.valueOf(cmd.getOptionValue("ilprandomseed"));
			MetricsManager.getInstance().addTags("ilprandomseed", cmd.getOptionValue("ilprandomseed"));
		}

		if (cmd.getOptionValue("ilpopttol") != null) {
			IlpSolverConfig.OPT_TOL = Double.valueOf(cmd.getOptionValue("ilpopttol"));
			MetricsManager.getInstance().addTags("ilpopttol", cmd.getOptionValue("ilpopttol"));
		}

		if (cmd.getOptionValue("ilpobjscaling") != null) {
			IlpSolverConfig.OBJ_SCALE = Double.valueOf(cmd.getOptionValue("ilpobjscaling"));
			MetricsManager.getInstance().addTags("ilpobjscaling", cmd.getOptionValue("ilpobjscaling"));
		}

		IlpSolverConfig.OBJ_LOG = cmd.hasOption("ilpobjlog");
		if (cmd.hasOption("ilpobjlog")) {
			MetricsManager.getInstance().addTags("ilpobjlog", String.valueOf(cmd.hasOption("ilpobjlog")));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<ModelFacade, AbstractAlgorithm> getAlgorithmFactory(final Experiment experiment,
			final String algoConfig, final CommandLine cmd,
			final Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) {
		switch (algoConfig) {
		case "ilp":
			return VneFakeIlpAlgorithm::new;
		case "ilp-batch":
			return VneFakeIlpBatchAlgorithm::new;
		default:
			return previousAlgoFactory;
		}
	}
}
