package scenarios.load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import scenarios.modules.AlgorithmModule;
import scenarios.modules.CsvModule;
import scenarios.modules.MemoryModule;
import scenarios.modules.ModelConfigurationModule;
import scenarios.modules.Module;
import scenarios.modules.NotionModule;

/**
 * Configure an experiment using the provided modules by parsing the CLI
 * arguments.
 *
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class ExperimentConfigurator {

	protected List<Module> modules = new ArrayList<>();

	public ExperimentConfigurator() {
	}

	public ExperimentConfigurator(final Collection<Module> modules) {
		this();

		this.addAll(modules);
	}

	public static class Default extends ExperimentConfigurator {
		public Default() {
			super(defaultModules());
		}
	}

	public static List<Module> defaultModules() {
		return List.of(//
				new AlgorithmModule(), //
				new CsvModule(), //
				new MemoryModule(), //
				new ModelConfigurationModule(), //
				new NotionModule() //
		);
	}

	public static <T extends Experiment> T of(final T experiment, final String[] args) throws ParseException {
		ExperimentConfigurator experimentConfigurator = new ExperimentConfigurator.Default();

		return experimentConfigurator.configure(experiment, args);
	}

	public void add(final Module module) {
		this.modules.add(module);
	}

	public void addAll(final Collection<Module> modules) {
		this.modules.addAll(modules);
	}

	/**
	 * Configure the experiment by parsing the given arguments.
	 *
	 * @param args Arguments to parse.
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
			System.out.println("=> Arguments: " + Arrays.toString(args));
		} catch (final ParseException e) {
			System.err.println(e.getMessage());
			System.err.println();
			formatter.printHelp("cli parameters", options);
			System.exit(1);
			// return is easier to spot
			return experiment;
		}

		return experiment;
	}

}
