package scenarios.modules;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import metrics.MetricConfig;
import scenarios.load.Experiment;

/**
 * Configure an experiment to enable memory management with the old
 * GlobalMetricManager.
 * 
 * Options: -g / --memmeasurement
 * 
 * @deprecated Please use the new MetricManager with its own MemoryHandler
 *             instead.
 */
@Deprecated
public class MemoryModule extends AbstractModule {
	protected final Option memEnabled = Option.builder()//
			.option("g")//
			.longOpt("memmeasurement")//
			.desc("memory measurement metric enabled")//
			.deprecated()//
			.build();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(memEnabled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		MetricConfig.ENABLE_MEMORY = cmd.hasOption("memmeasurement");
	}
}
