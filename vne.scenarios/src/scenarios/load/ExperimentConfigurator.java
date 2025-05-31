package scenarios.load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import iflye.dependencies.logging.IflyeLogger;
import scenarios.modules.Module;

/**
 * Configure an experiment using the provided modules by parsing the CLI
 * arguments.
 *
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class ExperimentConfigurator extends IflyeLogger {

	/**
	 * All modules that are registered for configuration.
	 */
	protected List<Module> modules = new ArrayList<>();

	/**
	 * Create a new ExperimentConfigurator.
	 */
	public ExperimentConfigurator() {
	}

	/**
	 * Create a new ExperimentConfigurator with the given modules.
	 * 
	 * @param modules the modules to use for configuration.
	 */
	public ExperimentConfigurator(final Collection<Module> modules) {
		this();

		this.addAll(modules);
	}

	/**
	 * Configure the given experiment with the default modules by parsing the given
	 * command line arguments.
	 * 
	 * @param experiment the {@link Experiment} to configure
	 * @param args       the command line arguments to parse
	 * @return the configured experiment
	 * @throws ParseException if the supplied CLI arguments could not be parsed
	 */
	public static <T extends Experiment> T of(final T experiment, final String[] args) throws ParseException {
		ExperimentConfigurator experimentConfigurator = new ExperimentConfigurator(
				experiment.getConfigurationModules());

		return experimentConfigurator.configure(experiment, args);
	}

	/**
	 * Configure the given experiment with the default modules by parsing the given
	 * command line arguments.
	 * 
	 * @param experiment a supplier to create the {@link Experiment} to configure
	 * @param args       the command line arguments to parse
	 * @return the configured experiment
	 * @throws ParseException if the supplied CLI arguments could not be parsed
	 */
	public static <T extends Experiment> T of(final Supplier<T> experiment, final String[] args) throws ParseException {
		return ExperimentConfigurator.of(experiment.get(), args);
	}

	/**
	 * Add a new module to the list of configuration modules.
	 * 
	 * @param module the module to add.
	 */
	public void add(final Module module) {
		this.modules.add(module);
	}

	/**
	 * Add all given modules to the list of configuration modules.
	 * 
	 * @param modules a collection of all modules to add.
	 */
	public void addAll(final Collection<Module> modules) {
		this.modules.addAll(modules);
	}

	/**
	 * Configure the experiment by parsing the given arguments.
	 *
	 * @param experiment The supplier to create a new {@link Experiment} to
	 *                   configure.
	 * @param args       Arguments to parse.
	 * @throws ParseException
	 */
	public <T extends Experiment> T configure(final Supplier<T> experiment, final String[] args) throws ParseException {
		return configure(experiment.get(), args);
	}

	/**
	 * Configure the experiment by parsing the given arguments.
	 *
	 * @param experiment The {@link Experiment} to configure.
	 * @param args       Arguments to parse.
	 * @throws ParseException
	 */
	public <T extends Experiment> T configure(final T experiment, final String[] args) throws ParseException {
		final Options options = new Options();

		final Option help = Option.builder().option("h").longOpt("help").desc("Display this help message").build();
		options.addOption(help);

		modules.forEach((module) -> module.register(experiment, options));

		final CommandLineParser parser = new DefaultParser();
		final HelpFormatter formatter = new HelpFormatter();

		try {
			final CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption(help)) {
				formatter.printHelp("cli parameters", options);
				System.exit(0);
				// return is easier to spot
				return experiment;
			}

			for (final Module module : modules) {
				module.configure(experiment, cmd);
			}

			// Print arguments into logs/system outputs
			logger.info("=> Arguments: " + Arrays.toString(args));
		} catch (final ParseException e) {
			logger.warning(e.getMessage());
			formatter.printHelp("cli parameters", options);
			System.exit(1);
			// return is easier to spot
			return experiment;
		}

		return experiment;
	}

}
