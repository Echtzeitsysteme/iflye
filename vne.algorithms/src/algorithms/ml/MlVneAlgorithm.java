package algorithms.ml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.management.InvalidAttributeValueException;

import model.Link;
import model.Node;
import model.SubstrateNetwork;
import model.SubstratePath;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualSwitch;
import model.VirtualServer;
import transform.encoding.Preprocessing;
import algorithms.AbstractAlgorithm;
import bridge.ModelLoader;
import bridge.TensorflowModel;
import example.Config;
import metrics.manager.GlobalMetricsManager;
import mlmodel.ModelPrediction;

//singleton
public class MlVneAlgorithm extends AbstractAlgorithm {
	private static ModelPrediction model;
	private static TensorflowModel tensorflowModel;
	private static MlVneAlgorithm instance;

	private static boolean doLinkEmbedding = true;
	
	public MlVneAlgorithm(SubstrateNetwork sNet, Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
	}

	private void checkPreConditions() {
		if (this.vNets.size() > 1) {
			throw new UnsupportedOperationException("ML algorithm only works with one vnr at a time.");
		}
	}

	private boolean doEmbedding(HashMap<String, String> embedding) {
		VirtualNetwork vNet = this.getFirstVnet();
		try {
			// embedding of all predicted nodes
			boolean allVirtServerOnSameServer = true;
			String previousSubstrateServerName = "";
			for (String virtNodeName : embedding.keySet()) {
				String subNodeName = embedding.get(virtNodeName);
				System.out.println("applying embedding: " + virtNodeName + " onto " + subNodeName);
				facade.embedGeneric(subNodeName, virtNodeName);
				if(previousSubstrateServerName.equals("")) {
					// first iteration
					previousSubstrateServerName = subNodeName;
				}
				if(!previousSubstrateServerName.equals(subNodeName)) {
					allVirtServerOnSameServer = false;
				}
				previousSubstrateServerName = subNodeName;
			}
			
			if(!Config.predictSwitches) {
				// switch embedding
				for (Node virtNode : facade.getAllSwitchesOfNetwork(vNet.getName())) {
					String host = null;
					if(allVirtServerOnSameServer) {
						host = previousSubstrateServerName;	
					}else {
						// TODO add more algorithmic sugar
						host = "sub_csw_0";
					}
					System.out.println("applying embedding: " + virtNode.getName() + " onto " + host);
					facade.embedGeneric(host, virtNode.getName());
				}
			}
			
			// link embedding
			if (doLinkEmbedding) {
				// embed every link of virtual network
				for (Link link : vNet.getLinks()) {
					// get substrate host of virtual source and target nodes
					Node sourceVirtNode = link.getSource();
					Node targetVirtNode = link.getTarget();
					String subSource = "";
					if(sourceVirtNode instanceof VirtualServer) {
						subSource = ((VirtualServer) sourceVirtNode).getHost().getName();
					}else if (sourceVirtNode instanceof VirtualSwitch) {
						subSource = ((VirtualSwitch) sourceVirtNode).getHost().getName();
					}
					String subTarget = "";
					if(targetVirtNode instanceof VirtualServer) {
						subTarget = ((VirtualServer) targetVirtNode).getHost().getName();
					}else if (targetVirtNode instanceof VirtualSwitch) {
						subTarget = ((VirtualSwitch) targetVirtNode).getHost().getName();
					}
					
					// do link embedding
					String linkHostElement;
					// source and target on same server
					if (subSource.equals(subTarget)) {
						linkHostElement = subSource;
					} else {
						// source and target on different server
						SubstratePath path = facade.getPathFromSourceToTarget(subSource, subTarget);
						if (path == null) {
							System.out.println("no path between " + subSource + " and " + subTarget);
							return false;
						}
						linkHostElement = path.getName();
					}
					System.out.println("applying embedding: " + link.getName() + " onto " + linkHostElement);
					facade.embedGeneric(linkHostElement, link.getName());
				}
			}

			// network embedding
			System.out.println("applying embedding: " + vNet.getName() + " onto " + sNet.getName());
			facade.embedGeneric(sNet.getName(), vNet.getName());
		} catch (UnsupportedOperationException e) {
			System.out.println(e.getMessage());
			facade.unembedVirtualNetwork2(vNet);
			return false;
		}

		return true;
	}

	@Override
	public boolean execute() {
		System.out.println("MLAlgorithm::execute");
		checkPreConditions();
		VirtualNetwork vNet = this.getFirstVnet();
		float[] inputVector = model.getInEncoding().get(sNet.getName(), vNet.getName());
		if (model.isStandardize()) {
			inputVector = Preprocessing.standardize(inputVector);
		}
		float[] result = tensorflowModel.predict(inputVector);
//		GlobalMetricsManager.getMlVneRuntime().addInferenceEmbeddingTime(tensorflowModel.getRuntimeLastInference());
		HashMap<String, String> embedding;
		try {
			embedding = model.getEmbeddingLabel().processModelResult(result, sNet.getName(), vNet.getName());
		} catch (InvalidAttributeValueException e) {
			e.printStackTrace();
			return false;
		}
//		GlobalMetricsManager.getMlVneRuntime().startEmbeddingTime();
		boolean success = doEmbedding(embedding);
//		GlobalMetricsManager.getMlVneRuntime().endEmbeddingTime();
		return success;
	}

	public static MlVneAlgorithm prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets,
			ModelPrediction _model) {
		System.out.println("MLAlgorithm::prepare");
		if (sNet == null || vNets == null) {
			throw new IllegalArgumentException("One of the provided network objects was null.");
		}

		if (vNets.size() == 0) {
			throw new IllegalArgumentException("Provided set of virtual networks was empty.");
		}

		if (instance == null) {
			instance = new MlVneAlgorithm(sNet, vNets);
		}
		instance.sNet = sNet;
		instance.vNets = new HashSet<>();
		instance.vNets.addAll(vNets);
		model = _model;
		tensorflowModel = ModelLoader.prepare(model.getPath());
		return instance;
	}

	public static void dispose() {
		System.out.println("MLAlgorithm::dispose");
		if (instance == null) {
			return;
		}
		instance = null;
		model = null;
		tensorflowModel = null;
	}

}