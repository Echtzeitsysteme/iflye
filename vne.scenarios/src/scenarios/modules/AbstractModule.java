package scenarios.modules;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import scenarios.load.DissScenarioLoad;

public abstract class AbstractModule implements Module {

	protected final DissScenarioLoad experiment;

	@Override
	public void register(final Options options) {
		// noop
	}

	@Override
	public void configure(final CommandLine cmd) throws ParseException {
		// noop
	}

	public AbstractModule(final DissScenarioLoad experiment) {
		this.experiment = experiment;
	}

	public DissScenarioLoad getExperiment() {
		return this.experiment;
	}
}
