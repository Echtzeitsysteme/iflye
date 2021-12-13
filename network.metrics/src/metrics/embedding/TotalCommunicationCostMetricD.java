package metrics.embedding;

import java.util.List;

import metrics.CostUtility;
import metrics.IMetric;
import model.Link;
import model.Node;
import model.SubstrateNetwork;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;

/**
 * Implementation of the cost function of paper [1] and [2]. In comparison to
 * the paper [1], we define the cost of one hop as 1 as stated in [2]. Node
 * embedding of servers is defined as increasing function for substrate server
 * filling. This means, that an almost full substrate server is more "expensive"
 * than an empty one.
 *
 * [1] Meng, Xiaoqiao, Vasileios Pappas, and Li Zhang. "Improving the
 * scalability of data center networks with traffic-aware virtual machine
 * placement." 2010 Proceedings IEEE INFOCOM. IEEE, 2010.
 *
 * [2] M. G. Rabbani, R. P. Esteves, M. Podlesny, G. Simon, L. Z. Granville and
 * R. Boutaba, "On tackling virtual data center embedding problem," 2013
 * IFIP/IEEE International Symposium on Integrated Network Management (IM 2013),
 * 2013, pp. 177-184.
 *
 * From the paper [1]: "For the sake of illustration, we define C_ij as the
 * number of switches on the routing path from VM i to j."
 *
 * From the paper [2]: "We consider the hop-count between the virtual nodes
 * (i.e., VM or virtual switch) multiplied by the corresponding requested
 * bandwidth of the virtual link connecting the two virtual nodes"
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TotalCommunicationCostMetricD implements IMetric {

	/**
	 * Calculated cost.
	 */
	private double cost;

	/**
	 * Creates a new instance of this metric for the provided substrate network.
	 *
	 * @param sNet Substrate network to calculate the metric for.
	 */
	public TotalCommunicationCostMetricD(final SubstrateNetwork sNet) {
		double cost = 0;

		// Iterate over all virtual networks that are embedded on the substrate network
		for (final VirtualNetwork vNet : sNet.getGuests()) {
			final List<Link> guestLinks = facade.getAllLinksOfNetwork(vNet.getName());

			// Iterate over all virtual links
			for (final Link l : guestLinks) {
				final VirtualLink vl = (VirtualLink) l;
				cost += CostUtility.getTotalCommunicationCostLinkBCD(vl, vl.getHost());
			}

			final List<Node> guestServers = facade.getAllServersOfNetwork(vNet.getName());

			// Iterate over all virtual servers
			for (final Node s : guestServers) {
				final VirtualServer vs = (VirtualServer) s;
				cost += CostUtility.getTotalCommunicationCostNodeD(vs, vs.getHost());
			}
		}

		this.cost = cost;
	}

	@Override
	public double getValue() {
		return cost;
	}

}
