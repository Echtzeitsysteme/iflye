package scenarios.modules;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public interface Module {
	public void register(final Options options);

	public void configure(final CommandLine cmd) throws ParseException;
}
