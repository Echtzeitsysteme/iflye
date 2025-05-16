package scenarios.modules;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import metrics.manager.MetricsManager;
import scenarios.load.Experiment;

/**
 * Configure an experiment with all model-related parameters.
 * 
 * This module requires the paths to the substrate and virtual networks and can
 * optionally enable model persistence after each step and remove unembedded
 * vNets.
 * 
 * Options: -s / --snetfile <path>, -v / --vnetfile <path>, --persist-model
 * [<path>], --remove-unembedded-vnets
 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final Experiment experiment, final Options options) {
		options.addOption(subNetFile);
		options.addOption(virtNetFile);
		options.addOption(modelPersist);
		options.addOption(removeUnembeddedVnetsOption);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Experiment experiment, final CommandLine cmd) throws ParseException {
		final String subNetPath = cmd.getOptionValue("snetfile");
		experiment.setSubNetPath(subNetPath);
		MetricsManager.getInstance().addTags("substrate network", getNetworkConfigurationName(subNetPath));

		final String virtNetsPath = cmd.getOptionValue("vnetfile");
		experiment.setVirtNetsPath(virtNetsPath);
		MetricsManager.getInstance().addTags("virtual network", getNetworkConfigurationName(virtNetsPath));

		if (cmd.hasOption(modelPersist)) {
			final String filePath = cmd.getParsedOptionValue(modelPersist, "");
			experiment.setPersistModel(true);
			experiment.setPersistModelPath(filePath.isBlank() ? null : filePath);
		}

		experiment.setRemoveUnembeddedVnets(cmd.hasOption(removeUnembeddedVnetsOption));
	}

	/**
	 * Extract the name of the network configuration from its file path. Assumes
	 * that the name corresponds to the parent folder's name.
	 * 
	 * @return the name of the network configuration.
	 */
	public static String getNetworkConfigurationName(final String filePath) {
		Path p = Paths.get(filePath);
		return p.getParent().getFileName().toString();
	}

}
