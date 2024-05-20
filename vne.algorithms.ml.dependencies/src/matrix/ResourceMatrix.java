package matrix;

import facade.ModelFacade;
import model.Node;
import model.SubstrateServer;
import model.VirtualServer;
import transform.encoding.NodeOrder;

public class ResourceMatrix extends Matrix {
	protected NodeOrder nodeOrder;

	public ResourceMatrix(String netIds[]) {
		super();
		this.nodeOrder = new NodeOrder();
		nodeOrder.setIgnoreSwitches(true);
		nodeOrder.addNodes(netIds);
		this.columns = 3;
		this.rows = nodeOrder.getDimension();
		this.build();
	}

	public void build() {
		this.initialize();
		for (int i = 0; i < this.rows; i++) {
			String nodeId = this.nodeOrder.reverseNodes.get(i);
			Node node = ModelFacade.getInstance().getNodeById(nodeId);
			if (node instanceof SubstrateServer) {
				SubstrateServer sServer = (SubstrateServer) node;
				this.setValue(sServer.getName(), sServer.getResidualMemory(), sServer.getResidualCpu(),
						sServer.getResidualStorage());
			}
			if (node instanceof VirtualServer) {
				VirtualServer vServer = (VirtualServer) node;
				this.setValue(vServer.getName(), vServer.getMemory(), vServer.getCpu(), vServer.getStorage());
			}
		}
	}

	private void setValue(String nodeId, long memory, long cpu, long storage) {
		int i = nodeOrder.nodes.get(nodeId);
		this.matrix[i][0] = memory;
		this.matrix[i][1] = cpu;
		this.matrix[i][2] = storage;
	}

	public float getMemValue(String nodeId) {
		this.nodeOrder.nodes.get(nodeId);
		return this.matrix[this.nodeOrder.nodes.get(nodeId)][0];
	}

	public float getCpuValue(String nodeId) {
		this.nodeOrder.nodes.get(nodeId);
		return this.matrix[this.nodeOrder.nodes.get(nodeId)][1];
	}

	public float getStorageValue(String nodeId) {
		this.nodeOrder.nodes.get(nodeId);
		return this.matrix[this.nodeOrder.nodes.get(nodeId)][2];
	}

	public float[] getMemValues() {
		return this.getColumnValues(0);
	}

	public float[] getCpuValues() {
		return this.getColumnValues(1);
	}

	public float[] getStorageValues() {
		return this.getColumnValues(2);
	}

	public void print() {
		String[] tableHeader = new String[3];
		tableHeader[0] = "Memory";
		tableHeader[1] = "CPU";
		tableHeader[2] = "Storage";
		int columnWidth = this.printTableHeader(tableHeader);

		for (int i = 0; i < this.rows; i++) {
			// print name in beginning of row
			String name = this.nodeOrder.reverseNodes.get(i);
			this.printValueInCell(name, columnWidth);

			// print rest of table row
			for (int j = 0; j < this.columns; j++) {
				this.printValueInCell(this.matrix[i][j], columnWidth);
			}
			System.out.println();
		}
	}
}
