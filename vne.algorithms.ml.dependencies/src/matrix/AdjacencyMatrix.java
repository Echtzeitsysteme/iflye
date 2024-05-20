package matrix;

import facade.ModelFacade;
import model.Link;
import model.Network;
import model.SubstrateLink;
import transform.encoding.NodeOrder;

// data structure to be used for transformations which need matrix data representation (e.g. AdjacencyMatrixTransformation)
public class AdjacencyMatrix extends Matrix {
	protected NodeOrder nodeOrder;
	private String sNetId;
	private String vNetId;

	// edge exists -> 1
	private final String booleanAttrKey = "binary";
	// for virtual networks the bandwidth requirement is used instead of residual bandwidth
	private final String residualBandwidthAttrKey = "residualBandwidth";

	private String edgeAttribute = booleanAttrKey;

	// factory methods
	public static AdjacencyMatrix createFromSubstrateNetwork(String sNetId) {
		AdjacencyMatrix instance = new AdjacencyMatrix(sNetId, null);
		instance.nodeOrder.addNodes(sNetId);
		instance.build();
		return instance;
	}

	public static AdjacencyMatrix createFromVirtualNetwork(String vNetId) {
		AdjacencyMatrix instance = new AdjacencyMatrix(null, vNetId);
		instance.nodeOrder.addNodes(vNetId);
		instance.build();
		return instance;
	}

	public static AdjacencyMatrix createFromSubstrateAndVirtualNetwork(String sNetId, String vNetId) {
		AdjacencyMatrix instance = new AdjacencyMatrix(sNetId, vNetId);
		instance.nodeOrder.addNodes(sNetId);
		instance.nodeOrder.addNodes(vNetId);
		instance.build();
		return instance;
	}

	// constructor
	private AdjacencyMatrix(String sNetId, String vNetId) {
		super();
		this.sNetId = sNetId;
		this.vNetId = vNetId;
		this.nodeOrder = new NodeOrder();
	}

	public void build() {
		this.rows = this.nodeOrder.getDimension();
		this.columns = this.nodeOrder.getDimension();
		this.initialize();

		if (this.sNetId != null) {
			this.addLinks(this.sNetId);
		}
		if (this.vNetId != null) {
			this.addLinks(this.vNetId);
		}
	}

	// sets the matrix value for all links of a network
	private void addLinks(String netId) {
		Network net = ModelFacade.getInstance().getNetworkById(netId);
		for (Link link : net.getLinks()) {
			int value = 1;
			if (link instanceof SubstrateLink) {
				SubstrateLink sLink = (SubstrateLink) link;
				if (this.edgeAttribute.equals(residualBandwidthAttrKey)) {
					value = sLink.getResidualBandwidth();
				}
			} else {
				// Link
				if (this.edgeAttribute.equals(residualBandwidthAttrKey)) {
					value = link.getBandwidth();
				}
			}
			this.setValue(link.getSource().getName(), link.getTarget().getName(), (float) value);
		}
	}

	// this function is public to add special embedding edges
	public void setValue(String node1, String node2, float value) {
		Integer i = this.nodeOrder.nodes.get(node1);
		Integer j = this.nodeOrder.nodes.get(node2);
		this.matrix[i][j] = value;
		this.matrix[j][i] = value;
	}

	public void setResidualBandwidthAsEdgeAttr() {
		this.edgeAttribute = residualBandwidthAttrKey;
		this.build();
	}

	public void setBinaryAttributeAsEdgeAttr() {
		this.edgeAttribute = booleanAttrKey;
		this.build();
	}

	// outputs the matrix on the console in a aligned format
	public void print() {
		String[] tableHeader = new String[this.columns];
		for (int i = 0; i < this.columns; i++) {
			tableHeader[i] = this.nodeOrder.reverseNodes.get(i);
		}
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
