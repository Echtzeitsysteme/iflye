package scenarios.modules;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import scenarios.load.Experiment;

public abstract class AbstractModule implements Module {

	@Override
	public void register(final Experiment experiment, final Options options) {
		// noop
	}

	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		// noop
	}

}
