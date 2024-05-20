package transform.label;

import java.util.HashMap;

import javax.management.InvalidAttributeValueException;

import example.Config;
import facade.ModelFacade;
import matrix.EmbeddingMatrix;
import model.Link;
import model.Node;
import model.SubstrateHostLink;
import model.SubstrateNode;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

public class MultiClassLabel extends AbstractEmbeddingLabel {
	private static boolean verbose = true;

	@Override
	public float[] get(String sNetId, String vNetId) {
		System.out.println("MultiClassLabel::get with " + sNetId + ", " + vNetId);

		EmbeddingMatrix embeddingMatrix = new EmbeddingMatrix(sNetId, vNetId, !Config.predictSwitches);
		if (verbose) {
			printEmbeddingsOfVirtualNetwowrk(vNetId);
			embeddingMatrix.rowNodeOrder.print();
			embeddingMatrix.columnNodeOrder.print();
			embeddingMatrix.print();
		}
		return embeddingMatrix.flatten();
	}

	@Override
	public HashMap<String, String> processModelResult(float[] result, String sNetId, String vNetId)
			throws InvalidAttributeValueException {
		System.out.println("MultiClassLabel::processModelResult");

		for (int i = 0; i < result.length; i++) {
			if (result[i] < 0 || result[i] > 1.0) {
				throw new InvalidAttributeValueException("value range of result vector is invalid");
			}
		}

		EmbeddingMatrix resultMatrix = new EmbeddingMatrix(sNetId, vNetId, !Config.predictSwitches);
		this.setExpectedResultDimensionality(resultMatrix.rowNodeOrder.getDimension(),
				resultMatrix.columnNodeOrder.getDimension());
		this.validateModelResultLength(result.length);

		resultMatrix.unflatten(result);
		if (verbose) {
			resultMatrix.print();
		}

		HashMap<String, String> embedding = new HashMap<String, String>();
		for (int i = 0; i < resultMatrix.columnNodeOrder.getDimension(); i++) {
			float[] probabilities = resultMatrix.getColumnValues(i);
			int maxAt = 0;
			for (int j = 0; j < probabilities.length; j++) {
				maxAt = probabilities[j] > probabilities[maxAt] ? j : maxAt;
			}
			String subNodeName = resultMatrix.rowNodeOrder.reverseNodes.get(maxAt);
			String virtNodeName = resultMatrix.columnNodeOrder.reverseNodes.get(i);
			embedding.put(virtNodeName, subNodeName);
		}

		return embedding;
	}
	
	/*
	 * Copied utility methods.
	 */
	
	public static boolean printEmbeddingsOfVirtualNetwowrk(String vNetId) {
		VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(vNetId);
		if (vNet.getHost() == null) {
			System.out.println("Embeddings: VNR Failed");
			return false;
		}

		// TODO same logic as in EmbeddingMatrix
		// find vSwitch -> sNode, vServer -> sNode
		for (Node node : vNet.getNodes()) {
			SubstrateNode host = null;
			if (node instanceof VirtualSwitch) {
				host = ((VirtualSwitch) node).getHost();
			} else if (node instanceof VirtualServer) {
				host = ((VirtualServer) node).getHost();
			}
			System.out.println(node.getName() + "-> " + host.getName());
		}

		// find vLink -> sLink, vLink -> sPath, vLink -> sServer embedding
		for (Link link : vNet.getLinks()) {
			VirtualLink vLink = (VirtualLink) link;
			SubstrateHostLink host = vLink.getHost();
			System.out.println(vLink.getName() + "-> " + host.getName());
		}
		return true;
	}

}
