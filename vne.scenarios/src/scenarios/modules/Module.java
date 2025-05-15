package scenarios.modules;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import scenarios.load.Experiment;

/**
 * Interface for modules that can be registered to configure an experiment.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public interface Module {

	/**
	 * Register any CLI options for this module.
	 * 
	 * @param experiment the experiment to register the module for
	 * @param options    the options collection to add the options to
	 */
	public void register(final Experiment experiment, final Options options);

	/**
	 * Configure the experiment using the provided command line arguments.
	 * 
	 * @param experiment the experiment to configure
	 * @param cmd        the command line arguments
	 * @throws ParseException if an error occurs while parsing the command line
	 *                        arguments
	 */
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException;

}
