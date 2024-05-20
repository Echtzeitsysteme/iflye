package transform.encoding;

import java.util.HashMap;
import facade.ModelFacade;
import model.Network;
import model.Node;
import model.SubstrateSwitch;
import model.VirtualSwitch;

// Mapping zwischen Node Name und Position in einer Matrix Zeile oder Spalte
public class NodeOrder {
	/*
	 * saves the mapping between node names and vertices number
	 */
	public final HashMap<String, Integer> nodes = new HashMap<>();
	public final HashMap<Integer, String> reverseNodes = new HashMap<>();
	private int size = 0;
	private boolean ignoreSwitches = false;

	public void setIgnoreSwitches(boolean ignoreSwitches) {
		this.ignoreSwitches = ignoreSwitches;
	}

	public NodeOrder() {
	}

	private void addNode(Node node) {
		if (this.ignoreSwitches) {
			if (node instanceof VirtualSwitch || node instanceof SubstrateSwitch) {
				return;
			}
		}

		this.nodes.put(node.getName(), this.size);
		this.reverseNodes.put(this.size, node.getName());
		this.size += 1;
	}

	public final void addNodes(String[] netIds) {
		for (String netId : netIds) {
			this.addNodes(netId);
		}
	}

	public final void addNodes(String netId) {
		Network net = ModelFacade.getInstance().getNetworkById(netId);
		// this always gives the nodes in the same order
		for (Node node : net.getNodes()) {
			this.addNode(node);
		}
	}

	public int getDimension() {
		return this.size;
	}

	public void print() {
		System.out.println("NodeOrder");
		for (int i = 0; i < this.size; i++) {
			System.out.println(this.reverseNodes.get(i) + " -> " + i);
		}
	}

}
