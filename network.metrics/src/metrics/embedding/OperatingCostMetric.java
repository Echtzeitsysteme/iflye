package metrics.embedding;

import metrics.IMetric;
import model.Node;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.SubstrateSwitch;

/**
 * Operating cost metric implementation. This metric calculates the operating
 * costs of the whole substrate network based on: The number of used substrate
 * servers, the number of used substrate switches and the amount of used
 * resources per substrate server.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class OperatingCostMetric implements IMetric {

	public static final int STATIC_COST_PER_SERVER = 1;
	public static final int STATIC_COST_PER_SWITCH = 1;
	public static final int DYNAMIC_COST_PER_SERVER = 1;

	private final double value;

	/**
	 * Creates a new instance of this metric for the provided substrate network.
	 *
	 * @param sNet Substrate network to calculate the metric for.
	 */
	public OperatingCostMetric(final SubstrateNetwork sNet) {
		double value = 0;

		for (final Node n : sNet.getNodes()) {
			// Server costs
			if (n instanceof SubstrateServer) {
				final SubstrateServer ssrv = (SubstrateServer) n;
				if (!ssrv.getGuestServers().isEmpty()) {
					value += STATIC_COST_PER_SERVER;
					final double c = ssrv.getCpu() - ssrv.getResidualCpu();
					final double m = ssrv.getMemory() - ssrv.getResidualMemory();
					final double s = ssrv.getStorage() - ssrv.getResidualStorage();

					// Mean over all used server's resources
					value += (c / ssrv.getCpu() + m / ssrv.getMemory() + s / ssrv.getStorage()) / 3
							* DYNAMIC_COST_PER_SERVER;
				}
			} else if (n instanceof SubstrateSwitch) {
				// Switch costs (embedding)
				final SubstrateSwitch ssw = (SubstrateSwitch) n;
				if (!ssw.getGuestSwitches().isEmpty()) {
					value += STATIC_COST_PER_SWITCH;
				}
			}
		}

		this.value = value;
	}

	@Override
	public double getValue() {
		return this.value;
	}

}
