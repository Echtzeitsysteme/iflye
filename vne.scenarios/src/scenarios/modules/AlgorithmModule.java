package scenarios.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Embedding;
import algorithms.AlgorithmConfig.Objective;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import metrics.manager.MetricsManager;
import scenarios.load.Experiment;
import scenarios.modules.algorithms.GipsAlgorithm;
import scenarios.modules.algorithms.IlpAlgorithm;
import scenarios.modules.algorithms.PmAlgorithm;
import scenarios.modules.algorithms.RandomAlgorithm;
import scenarios.modules.algorithms.TafAlgorithmConfig;

public class AlgorithmModule extends AbstractModule {
	protected final Option emb = Option.builder()//
			.option("e")//
			.longOpt("embedding")//
			.desc("embedding to use for the PM algorithm")//
			.hasArg()//
			.build();

	protected final Option algo = Option.builder()//
			.option("a")//
			.longOpt("algorithm")//
			.desc("algorithm to use")//
			.hasArg()//
			.required()//
			.build();

	protected final Option obj = Option.builder()//
			.option("o")//
			.longOpt("objective")//
			.desc("objective to use")//
			.hasArg()//
			.required()//
			.build();

	protected final Option pathLength = Option.builder()//
			.option("l")//
			.longOpt("path-length")//
			.desc("maximum path length to generate paths with")//
			.hasArg()//
			.build();

	protected final Option paths = Option.builder()//
			.option("k")//
			.longOpt("kfastestpaths")//
			.desc("k fastest paths between two nodes to generate")//
			.hasArg()//
			.build();

	protected final List<AlgorithmConfiguration> algorithms = new ArrayList<>();

	public AlgorithmModule() {
		this(defaultAlgorithms());
	}

	public AlgorithmModule(final Collection<AlgorithmConfiguration> algorithms) {
		super();

		this.algorithms.addAll(algorithms);
	}

	public static List<AlgorithmConfiguration> defaultAlgorithms() {
		return List.of(new GipsAlgorithm(), new IlpAlgorithm(), new PmAlgorithm(), new RandomAlgorithm(),
				new TafAlgorithmConfig());
	}

	public AlgorithmModule addAlgorithm(final AlgorithmConfiguration algorithmConfiguration) {
		this.algorithms.add(algorithmConfiguration);
		return this;
	}

	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(algo);
		options.addOption(obj);
		options.addOption(pathLength);
		options.addOption(paths);
		options.addOption(emb);

		this.algorithms.forEach((algorithm) -> algorithm.register(experiment, options));
	}

	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		final String algoConfig = cmd.getOptionValue("algorithm");
		MetricsManager.getInstance().addTags("algorithm", algoConfig);

		MetricsManager.getInstance().addTags("objective", cmd.getOptionValue("objective"));
		switch (cmd.getOptionValue("objective")) {
		case "total-path":
			AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
			break;
		case "total-comm-a":
			AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_A;
			break;
		case "total-comm-b":
			AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_COST_B;
			break;
		case "total-obj-c":
			AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_C;
			break;
		case "total-obj-d":
			AlgorithmConfig.obj = Objective.TOTAL_COMMUNICATION_OBJECTIVE_D;
			break;
		case "total-taf-comm":
			AlgorithmConfig.obj = Objective.TOTAL_TAF_COMMUNICATION_COST;
			break;
		}

		ModelFacadeConfig.MIN_PATH_LENGTH = 1;
		String pathLengthParam = cmd.getOptionValue("path-length");
		if (pathLengthParam != null) {
			MetricsManager.getInstance().addTags("path-length", pathLengthParam);
			if (pathLengthParam.equals("auto")) {
				ModelFacadeConfig.MAX_PATH_LENGTH_AUTO = true;
			} else {
				ModelFacadeConfig.MAX_PATH_LENGTH = Integer.valueOf(cmd.getOptionValue("path-length"));
			}
		}

		if (cmd.getOptionValue("kfastestpaths") != null) {
			final int K = Integer.valueOf(cmd.getOptionValue("kfastestpaths"));
			MetricsManager.getInstance().addTags("kfastestpaths", cmd.getOptionValue("kfastestpaths"));
			if (K > 1) {
				ModelFacadeConfig.YEN_PATH_GEN = true;
				ModelFacadeConfig.YEN_K = K;
			}
		}

		if (cmd.getOptionValue("embedding") != null) {
			MetricsManager.getInstance().addTags("embedding", cmd.getOptionValue("embedding"));
			switch (cmd.getOptionValue("embedding")) {
			case "emoflon":
				AlgorithmConfig.emb = Embedding.EMOFLON;
				break;
			case "emoflon_wo_update":
				AlgorithmConfig.emb = Embedding.EMOFLON_WO_UPDATE;
				break;
			case "manual":
				AlgorithmConfig.emb = Embedding.MANUAL;
				break;
			}
		}

		for (final AlgorithmConfiguration algorithm : this.algorithms) {
			algorithm.configure(experiment, cmd);
		}
		final Function<ModelFacade, AbstractAlgorithm> algorithmFactory = this.algorithms.stream().reduce(null,
				(factory, algorithmConfiguration) -> {
					try {
						return algorithmConfiguration.getAlgorithmFactory(experiment, algoConfig, cmd, factory);
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}, (factory1, factory2) -> factory2 == null ? factory1 : factory2);

		if (algorithmFactory == null) {
			throw new IllegalArgumentException("Configured algorithm not known.");
		}

		experiment.setAlgoFactory(algorithmFactory);
	}

	public static interface AlgorithmConfiguration extends Module {
		public Function<ModelFacade, AbstractAlgorithm> getAlgorithmFactory(Experiment experiment, String algoConfig,
				CommandLine cmd, Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) throws ParseException;
	}
}
