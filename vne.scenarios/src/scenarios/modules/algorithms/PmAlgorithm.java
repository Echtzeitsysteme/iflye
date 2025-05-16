package scenarios.modules.algorithms;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import algorithms.AlgorithmConfig;
import algorithms.pm.VnePmMdvneAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithmMigration;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesA;
import algorithms.pm.VnePmMdvneAlgorithmPipelineThreeStagesB;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackA;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesRackB;
import algorithms.pm.VnePmMdvneAlgorithmPipelineTwoStagesVnet;
import metrics.manager.MetricsManager;
import scenarios.load.Experiment;
import scenarios.modules.AbstractModule;
import scenarios.modules.AlgorithmModule;

/**
 * Add an option to configure the experiment to use the
 * {@link VnePmMdvneAlgorithm} with different characteristics.
 * 
 * Options: -t / --tries <arg>, -a / --algorithm
 * <pm/pm-migration/pm-pipeline2-vnet/pm-pipeline2-racka/pm-pipeline2-rackb/pm-pipeline3a/pm-pipeline3b>
 * 
 * @see {@link VnePmMdvneAlgorithm}
 * @see {@link VnePmMdvneAlgorithmMigration}
 * @see {@link VnePmMdvneAlgorithmPipelineTwoStagesVnet}
 * @see {@link VnePmMdvneAlgorithmPipelineTwoStagesRackA}
 * @see {@link VnePmMdvneAlgorithmPipelineTwoStagesRackB}
 * @see {@link VnePmMdvneAlgorithmPipelineThreeStagesA}
 * @see {@link VnePmMdvneAlgorithmPipelineThreeStagesB}
 */
public class PmAlgorithm extends AbstractModule implements AlgorithmModule.AlgorithmConfiguration {
	protected final Option tries = Option.builder()//
			.option("t")//
			.longOpt("tries")//
			.desc("number of migration tries for the PM algorithm")//
			.hasArg()//
			.build();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(final AlgorithmModule algorithmModule) {
		algorithmModule.addAlgorithm("pm", VnePmMdvneAlgorithm::new);
		algorithmModule.addAlgorithm("pm-migration", VnePmMdvneAlgorithmMigration::new);
		algorithmModule.addAlgorithm("pm-pipeline2-vnet", VnePmMdvneAlgorithmPipelineTwoStagesVnet::new);
		algorithmModule.addAlgorithm("pm-pipeline2-racka", VnePmMdvneAlgorithmPipelineTwoStagesRackA::new);
		algorithmModule.addAlgorithm("pm-pipeline2-rackb", VnePmMdvneAlgorithmPipelineTwoStagesRackB::new);
		algorithmModule.addAlgorithm("pm-pipeline3a", VnePmMdvneAlgorithmPipelineThreeStagesA::new);
		algorithmModule.addAlgorithm("pm-pipeline3b", VnePmMdvneAlgorithmPipelineThreeStagesB::new);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(tries);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		if (cmd.getOptionValue(this.tries) != null) {
			AlgorithmConfig.pmNoMigrations = Integer.valueOf(cmd.getOptionValue(this.tries));
			MetricsManager.getInstance().addTags("tries", cmd.getOptionValue(this.tries));
		}
	}

}
