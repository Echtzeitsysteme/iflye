package scenarios.modules;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import scenarios.load.DissScenarioLoad;

public class ModelConfigurationModule extends AbstractModule {
	protected final Option subNetFile = Option.builder()//
			.option("s")//
			.longOpt("snetfile")//
			.desc("JSON file for the substrate network to load")//
			.hasArg()//
			.required()//
			.build();

	protected final Option virtNetFile = Option.builder()//
			.option("v")//
			.longOpt("vnetfile")//
			.desc("JSON file for the virtual network(s) to load")//
			.hasArg()//
			.required()//
			.build();

	protected final Option modelPersist = Option.builder()//
			.longOpt("persist-model")//
			.desc("If the model should be persisted after execution, optionally supply the file name.")//
			.hasArg()//
			.optionalArg(true)//
			.type(String.class)//
			.build();

	protected final Option removeUnembeddedVnetsOption = Option.builder()//
			.longOpt("remove-unembedded-vnets")//
			.desc("If VNets that where not successfully embedded should be removed from the model to prevent from blocking further embeddings")//
			.hasArg(false)//
			.build();

	public ModelConfigurationModule(final DissScenarioLoad experiment) {
		super(experiment);
	}

	@Override
	public void register(final Options options) {
		options.addOption(subNetFile);
		options.addOption(virtNetFile);
		options.addOption(modelPersist);
		options.addOption(removeUnembeddedVnetsOption);
	}

	@Override
	public void configure(final CommandLine cmd) throws ParseException {
		final String subNetPath = cmd.getOptionValue("snetfile");
		this.getExperiment().setSubNetPath(subNetPath);
		this.getExperiment().getMetricsManager().addTags("substrate network", getNetworkConfigurationName(subNetPath));

		final String virtNetsPath = cmd.getOptionValue("vnetfile");
		this.getExperiment().setVirtNetsPath(virtNetsPath);
		this.getExperiment().getMetricsManager().addTags("virtual network", getNetworkConfigurationName(virtNetsPath));

		if (cmd.hasOption(modelPersist)) {
			final String filePath = cmd.getParsedOptionValue(modelPersist, "");
			this.getExperiment().setPersistModel(true);
			this.getExperiment().setPersistModelPath(filePath.isBlank() ? null : filePath);
		}

		this.getExperiment().setRemoveUnembeddedVnets(cmd.hasOption(removeUnembeddedVnetsOption));
	}

	public static String getNetworkConfigurationName(final String filePath) {
		Path p = Paths.get(filePath);
		return p.getParent().getFileName().toString();
	}
}
