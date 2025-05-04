package scenarios.modules.algorithms;

import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.pm.VnePmMdvneAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesA;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesB;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackB;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesVnet;
import facade.ModelFacade;
import metrics.manager.MetricsManager;
import scenarios.load.Experiment;
import scenarios.modules.AbstractModule;
import scenarios.modules.AlgorithmModule;

public class PmAlgorithm extends AbstractModule implements AlgorithmModule.AlgorithmConfiguration {
	protected final Option tries = Option.builder()//
			.option("t")//
			.longOpt("tries")//
			.desc("number of migration tries for the PM algorithm")//
			.hasArg()//
			.build();

	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(tries);
	}

	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		if (cmd.getOptionValue("tries") != null) {
			AlgorithmConfig.pmNoMigrations = Integer.valueOf(cmd.getOptionValue("tries"));
			MetricsManager.getInstance().addTags("tries", cmd.getOptionValue("tries"));
		}
	}

	@Override
	public Function<ModelFacade, AbstractAlgorithm> getAlgorithmFactory(final Experiment experiment,
			final String algoConfig, final CommandLine cmd,
			final Function<ModelFacade, AbstractAlgorithm> previousAlgoFactory) {
		switch (algoConfig) {
		case "pm":
			return VnePmMdvneAlgorithm::new;
		case "pm-migration":
			return VnePmMdvneAlgorithmMigration::new;
		case "pm-pipeline2-vnet":
			return VnePmMdvneAlgorithmPipelineTwoStagesVnet::new;
		case "pm-pipeline2-racka":
			return VnePmMdvneAlgorithmPipelineTwoStagesRackA::new;
		case "pm-pipeline2-rackb":
			return VnePmMdvneAlgorithmPipelineTwoStagesRackB::new;
		case "pm-pipeline3a":
			return VnePmMdvneAlgorithmPipelineThreeStagesA::new;
		case "pm-pipeline3b":
			return VnePmMdvneAlgorithmPipelineThreeStagesB::new;
		default:
			return previousAlgoFactory;
		}
	}
}
