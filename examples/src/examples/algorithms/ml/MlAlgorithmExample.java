package examples.algorithms.ml;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import algorithms.ml.MlVneAlgorithm;
import facade.ModelFacade;
import metrics.manager.GlobalMetricsManager;
import mlmodel.ModelPrediction;
//import model.ModelPrediction;
import model.SubstrateNetwork;
import model.VirtualNetwork;
//import scenario.AbstractScenario;
//import scenario.Runner;
//import scenario.Sub_10_Virt_2_Scenario;
//import transform.encoding.NodeAttributesEncoding;
//import transform.label.MultiClassLabel;
import transform.encoding.NodeAttributesEncoding;
import transform.label.MultiClassLabel;

public class MlAlgorithmExample {

	public static void main(final String[] args) {
		final Map<Long, Integer> successRates = new HashMap<Long, Integer>();

		final int upperLimit = 24;
		for (int i = 24; i <= upperLimit; i++) {
			final int successCounter = run(i);
			System.out.println("Successful runs: " + successCounter + "/" + 10);
			successRates.put((long) i, successCounter);
		}

		// Print results
		successRates.forEach((k, v) -> {
			System.out.println("Seed: " + k + "; Result: " + v);
		});
	}

	private static int run(final long seed) {
//		Runner runner = new Runner();
//		runner.setScenario(new Sub_10_Virt_2_Scenario(seed));
//		runner.setUp();
//
		ModelPrediction model = new ModelPrediction();
		model.setInEncoding(new NodeAttributesEncoding());
		model.setStandardizeInputVector(true);
		model.setEmbeddingLabel(new MultiClassLabel());

		ModelFacade.getInstance().loadModel("resources/ml/Sub_10_Virt_2_Scenario.xmi");

		final SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
//		Set<VirtualNetwork> vNets = runner.retrieveVirtualNetworks();
		final List<VirtualNetwork> sortedvNets = new LinkedList<VirtualNetwork>();
//		sortedvNets.addAll(vNets);
		ModelFacade.getInstance().getAllNetworks().forEach(net -> {
			if (net instanceof VirtualNetwork vnet) {
				sortedvNets.add(vnet);
			}
		});
		Collections.sort(sortedvNets, new Comparator<VirtualNetwork>() {
			@Override
			public int compare(final VirtualNetwork arg0, final VirtualNetwork arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		int successCounter = 0;
//		if (GlobalMetricsManager.getMlVneRuntime() == null) {
//			GlobalMetricsManager.startMlVneRuntime();
//		}
		for (VirtualNetwork vNet : sortedvNets) {
			System.err.println("===> " + vNet.getName() + "; CPU: " + vNet.getCpu() + ", RAM: " + vNet.getMemory()
					+ ", Storage: " + vNet.getStorage());
			final MlVneAlgorithm algo = MlVneAlgorithm.prepare(sNet, Set.of(vNet), model);
			final boolean success = algo.execute();
			if (success) {
				successCounter++;
			}
			System.out.println(success);
			MlVneAlgorithm.dispose();
		}

		return successCounter;
	}

}
