package scenarios.modules;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import scenarios.load.Experiment;

/**
 * An abstract module blueprint that has no-operation for all interface methods.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public abstract class AbstractModule implements Module {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Experiment experiment, final Options options) {
		// noop
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		// noop
	}

}
