package metrics.embedding;

import java.util.List;

import metrics.CostUtility;
import metrics.IMetric;
import model.Link;
import model.SubstrateNetwork;
import model.VirtualLink;
import model.VirtualNetwork;

/**
 * Implementation of the cost function as defined in [1].
 *
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in
 * Rechenzentren, http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI
 * 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TotalCommunicationCostMetricA implements IMetric {

	/**
	 * Calculated cost.
	 */
	private double cost;

	/**
	 * Creates a new instance of this metric for the provided substrate network.
	 *
	 * @param sNet Substrate network to calculate the metric for.
	 */
	public TotalCommunicationCostMetricA(final SubstrateNetwork sNet) {
		double cost = 0;

		// Iterate over all virtual networks that are embedded on the substrate network
		for (final VirtualNetwork vNet : sNet.getGuests()) {
			final List<Link> guestLinks = vNet.getLinks();

			// Iterate over all virtual links
			for (final Link l : guestLinks) {
				final VirtualLink vl = (VirtualLink) l;
				cost += CostUtility.getTotalCommunicationCostLinkA(vl, vl.getHost());
			}
		}

		this.cost = cost;
	}

	@Override
	public double getValue() {
		return cost;
	}

}
