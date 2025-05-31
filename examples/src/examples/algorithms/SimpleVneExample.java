package examples.algorithms;

import java.util.Set;

import algorithms.simple.SimpleVne;
import examples.AbstractIflyeExample;
import facade.ModelFacade;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Runnable example for the simple VNE algorithm.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class SimpleVneExample extends AbstractIflyeExample {

	/**
	 * Main method to start the example. String array of arguments will be ignored.
	 *
	 * @param args Will be ignored.
	 */
	public static void main(final String[] args) {
		// Substrate network = one tier network
		final OneTierConfig substrateConfig = new OneTierConfig(2, 1, false, 10, 10, 10, 20);
		final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(substrateConfig);
		subGen.createNetwork("sub", false);

		// Virtual network = one tier network
		final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 5);
		final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
		virtGen.createNetwork("virt", true);

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
		final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

		// Create and execute algorithm
		final SimpleVne svne = new SimpleVne();
		svne.prepare(sNet, Set.of(vNet));
		svne.execute();

		// Save model to file
		ModelFacade.getInstance().persistModel();
		logger.info("=> Execution finished.");

		System.exit(0);
	}

}
