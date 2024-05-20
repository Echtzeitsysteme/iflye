package matrix;

import facade.ModelFacade;
import model.Node;
import model.SubstrateNode;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import transform.encoding.NodeOrder;

public class EmbeddingMatrix extends Matrix {
	public NodeOrder rowNodeOrder;
	public NodeOrder columnNodeOrder;
	private String vNetId;
	private boolean ignoreSwitches = true;

	public EmbeddingMatrix(String sNetId, String vNetId, boolean ignoreSwitches) {
		this.vNetId = vNetId;
		this.rowNodeOrder = new NodeOrder();
		this.columnNodeOrder = new NodeOrder();
		this.ignoreSwitches= ignoreSwitches;
		
		if(this.ignoreSwitches) {
			rowNodeOrder.setIgnoreSwitches(true);
			columnNodeOrder.setIgnoreSwitches(true);
		}

		rowNodeOrder.addNodes(sNetId);
		columnNodeOrder.addNodes(vNetId);
		this.rows = rowNodeOrder.getDimension();
		this.columns = columnNodeOrder.getDimension();
		this.build();
	}

	public void build() {
		this.initialize();
		VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById(this.vNetId);
		if (vNet.getHost() == null) {
			return;
		}
		// find vSwitch -> sNode, vServer -> sNode
		for (Node node : vNet.getNodes()) {
			SubstrateNode host = null;
			if (node instanceof VirtualSwitch) {
				if(this.ignoreSwitches) {
					continue;
				}
				host = ((VirtualSwitch) node).getHost();
			} else if (node instanceof VirtualServer) {
				host = ((VirtualServer) node).getHost();
			}
			this.setValue(node.getName(), host.getName());
		}
	}

	private void setValue(String vNode, String sNode) {
		Integer i = this.rowNodeOrder.nodes.get(sNode);
		Integer j = this.columnNodeOrder.nodes.get(vNode);
		this.matrix[i][j] = 1;
	}
	
	public float getValue(String vNode, String sNode) {
		Integer i = this.rowNodeOrder.nodes.get(sNode);
		Integer j = this.columnNodeOrder.nodes.get(vNode);
		return this.matrix[i][j];
	}

	public void print() {
		String[] tableHeader = new String[this.columns];
		for (int i = 0; i < this.columns; i++) {
			tableHeader[i] = this.columnNodeOrder.reverseNodes.get(i);
		}
		int columnWidth = this.printTableHeader(tableHeader);

		for (int i = 0; i < this.rows; i++) {
			String name = this.rowNodeOrder.reverseNodes.get(i);
			this.printValueInCell(name, this.defaultColumnWidth);

			// print rest of table row
			for (int j = 0; j < this.columns; j++) {
				this.printValueInCell(this.matrix[i][j], columnWidth);
			}
			System.out.println();
		}
	}
}
