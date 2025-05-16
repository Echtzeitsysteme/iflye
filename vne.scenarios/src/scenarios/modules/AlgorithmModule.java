package scenarios.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Configure the algorithm to use for the experiment.
 * 
 * The supported algorithms are provided by the {@link AlgorithmConfiguration}
 * implementations. General-purpose options are provided by this class to
 * configure certain options of several algorithms.
 * 
 * Options: -e / --embedding <emoflon/emoflon_wo_update/manual>, -a /
 * --algorithm <arg>, -o / --objective
 * <total-path/total-comm-a/total-comm-b/total-obj-c/total-obj-d/total-taf-comm>,
 * -l / --path-length <auto/int>, -k / --kfastestpaths <int>
 */
public class AlgorithmModule extends AbstractModule {
	protected final Option emb = Option.builder()//
			.option("e")//
			.longOpt("embedding")//
			.desc("embedding to use for the PM algorithm")//
			.hasArg()//
			.build();

	protected static final String ALGORITHM_DESCRIPTION = "Algorithm to use. Values: \n%s";
	protected final Option algo = Option.builder()//
			.option("a")//
			.longOpt("algorithm")//
			.desc(ALGORITHM_DESCRIPTION.formatted(" -- No algorithms registered --"))//
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

	/**
	 * The list of all configured algorithms.
	 */
	protected final Map<String, Function<ModelFacade, AbstractAlgorithm>> algorithms = new HashMap<>();

	/**
	 * All submodules that could configure the algorithms.
	 */
	protected final List<AlgorithmConfiguration> submodules = new ArrayList<>();

	/**
	 * Initialize the default algorithms.
	 */
	public AlgorithmModule() {
		this(defaultSubmodules());
	}

	/**
	 * Initialize the given algorithms.
	 * 
	 * @param algorithms the algorithms to initialize.
	 */
	public AlgorithmModule(final Map<String, Function<ModelFacade, AbstractAlgorithm>> algorithms) {
		this(algorithms, defaultSubmodules());
	}

	/**
	 * Initialize the given submodules.
	 * 
	 * @param submodules the submodules to initialize.
	 */
	public AlgorithmModule(final Collection<AlgorithmConfiguration> submodules) {
		this(Map.of(), submodules);
	}

	/**
	 * Initialize the given algorithms.
	 * 
	 * @param algorithms the algorithms to initialize.
	 * @param submodules the submodules to initialize.
	 */
	public AlgorithmModule(final Map<String, Function<ModelFacade, AbstractAlgorithm>> algorithms,
			final Collection<AlgorithmConfiguration> submodules) {
		super();

		this.algorithms.putAll(algorithms);
		this.addSubmodule(submodules);
	}

	/**
	 * Get a list of all the default algorithms.
	 */
	public static List<AlgorithmConfiguration> defaultSubmodules() {
		return List.of(new GipsAlgorithm(), new IlpAlgorithm(), new PmAlgorithm(), new RandomAlgorithm(),
				new TafAlgorithmConfig());
	}

	/**
	 * Add all algorithms to the list of algorithms.
	 * 
	 * @param algorithms The algorithms to add.
	 * @return This module.
	 */
	public AlgorithmModule addAlgorithm(final Map<String, Function<ModelFacade, AbstractAlgorithm>> algorithms) {
		this.algorithms.putAll(algorithms);
		return this;
	}

	/**
	 * Add an algorithm to the list of algorithms.
	 * 
	 * @param algorithm              The name of the algorithm.
	 * @param algorithmConfiguration The algorithm to add.
	 * @return This module.
	 */
	public AlgorithmModule addAlgorithm(final String algorithm,
			final Function<ModelFacade, AbstractAlgorithm> algorithmConfiguration) {
		this.algorithms.put(algorithm, algorithmConfiguration);
		return this;
	}

	/**
	 * Add all submodules to the list of submodules.
	 * 
	 * @param submodules The submodules to add.
	 * @return This module.
	 */
	public AlgorithmModule addSubmodule(final Collection<AlgorithmConfiguration> submodules) {
		submodules.forEach(this::addSubmodule);

		return this;
	}

	/**
	 * Add a submodule to the list of submodules.
	 * 
	 * @param submodule The submodule to add.
	 * @return This module.
	 */
	public AlgorithmModule addSubmodule(final AlgorithmConfiguration submodule) {
		this.submodules.add(submodule);
		submodule.initialize(this);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Experiment experiment, final Options options) {
		algo.setDescription(ALGORITHM_DESCRIPTION
				.formatted(String.join("\n", algorithms.keySet().stream().map((k) -> " - " + k).toList())));

		options.addOption(algo);
		options.addOption(obj);
		options.addOption(pathLength);
		options.addOption(paths);
		options.addOption(emb);

		for (final AlgorithmConfiguration algorithmConfiguration : submodules) {
			algorithmConfiguration.register(experiment, options);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		final String algoConfig = cmd.getOptionValue(this.algo);
		MetricsManager.getInstance().addTags("algorithm", algoConfig);

		MetricsManager.getInstance().addTags("objective", cmd.getOptionValue("objective"));
		switch (cmd.getOptionValue(this.obj)) {
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
		String pathLengthParam = cmd.getOptionValue(this.pathLength);
		if (pathLengthParam != null) {
			MetricsManager.getInstance().addTags("path-length", pathLengthParam);
			if (pathLengthParam.equals("auto")) {
				ModelFacadeConfig.MAX_PATH_LENGTH_AUTO = true;
			} else {
				ModelFacadeConfig.MAX_PATH_LENGTH = Integer.valueOf(pathLengthParam);
			}
		}

		if (cmd.getOptionValue(this.paths) != null) {
			final int K = Integer.valueOf(cmd.getOptionValue(this.paths));
			MetricsManager.getInstance().addTags("kfastestpaths", cmd.getOptionValue(this.paths));
			if (K > 1) {
				ModelFacadeConfig.YEN_PATH_GEN = true;
				ModelFacadeConfig.YEN_K = K;
			}
		}

		if (cmd.getOptionValue(this.emb) != null) {
			MetricsManager.getInstance().addTags("embedding", cmd.getOptionValue("embedding"));
			switch (cmd.getOptionValue(this.emb)) {
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

		if (!this.algorithms.containsKey(algoConfig.toLowerCase())) {
			throw new IllegalArgumentException("Unknown value for --" + algo.getLongOpt() + ": " + algoConfig);
		}

		Function<ModelFacade, AbstractAlgorithm> algorithmFactory = this.algorithms.get(algoConfig.toLowerCase());

		for (final AlgorithmConfiguration algorithmConfiguration : submodules) {
			algorithmConfiguration.configure(experiment, cmd);
			algorithmFactory = algorithmConfiguration.configure(experiment, algoConfig, cmd, algorithmFactory);
		}

		experiment.setAlgoFactory(algorithmFactory);
	}

	/**
	 * Interface for algorithm modules.
	 * 
	 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
	 */
	public static interface AlgorithmConfiguration extends Module {

		/**
		 * Initialize the algorithm module with the desired algorithm configurations.
		 * 
		 * @param algorithmModule The algorithm module to configure.
		 */
		default public void initialize(final AlgorithmModule algorithmModule) {
			// noop
		}

		/**
		 * Get the next algorithm factory for the given algoConfig and the previous
		 * algorithm factory.
		 * 
		 * @param experiment          the experiment to configure
		 * @param algoConfig          the algorithm configuration
		 * @param cmd                 the command line arguments
		 * @param previousAlgoFactory the previous algorithm factory
		 * @return the algorithm factory for the given algoConfig
		 * @throws ParseException if an error occurs while parsing the command line
		 *                        arguments
		 */
		default public Function<ModelFacade, AbstractAlgorithm> configure(Experiment experiment, String algoConfig,
				CommandLine cmd, Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) throws ParseException {
			return previousAlgoFactory;
		}

	}
}
