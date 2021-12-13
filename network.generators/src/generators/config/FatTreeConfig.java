package generators.config;

/**
 * Configuration (container-)class for the fat tree [1] network generator.
 *
 * [1] ALFARES , Mohammad ; L OUKISSAS , Alexander ; V AHDAT , Amin: A Scalable,
 * Commodity Data Center Network Architecture. In: Proceedings of the ACM
 * SIGCOMM 2008 conference on Data communication. (2008), S. pp. 63–74
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class FatTreeConfig implements IGeneratorConfig {

	/**
	 * Parameter k that defines the network size.
	 */
	private int k = 4;

	/**
	 * One OneTierConfig is a rack in a fat tree network.
	 */
	private OneTierConfig rack;

	/**
	 * Bandwidth from core switches to aggregation switches.
	 */
	private int bwCoreToAggr = 1;

	/**
	 * Bandwidth from aggregation switches to edge switches.
	 */
	private int bwAggrToEdge = 1;

	/**
	 * Constructor initializing only k. The parameter k force the structure of the
	 * GoogleFatTreeNetwork.
	 *
	 * @param kparameter K force the structure.
	 */
	public FatTreeConfig(final int kparameter) {
		if (kparameter >= this.k && kparameter % 2 == 0) {
			this.k = kparameter;
		}

		// Default values for the rack configuration
		this.rack = new OneTierConfig(getServersPerEdgeSwitch(), 1, false, 1, 1, 1, 10);
	}

	public OneTierConfig getRack() {
		return rack;
	}

	public void setRack(OneTierConfig rack) {
		this.rack = rack;
	}

	public int getBwCoreToAggr() {
		return bwCoreToAggr;
	}

	public void setBwCoreToAggr(final int bwCoreToAggr) {
		this.bwCoreToAggr = bwCoreToAggr;
	}

	public int getBwAggrToEdge() {
		return bwAggrToEdge;
	}

	public void setBwAggrToEdge(final int bwAggrToEdge) {
		this.bwAggrToEdge = bwAggrToEdge;
	}

	/**
	 * Returns the number of aggregation-switches as a function of k.
	 *
	 * @return Number of aggregation-switches per pod.
	 */
	public int getAggregationSwitchesPerPod() {
		return k / 2;
	}

	/**
	 * Returns the number of core-switches as a function of k.
	 *
	 * @return Number of core-switches.
	 */
	public int getCoreSwitches() {
		// (k/2)^2
		return (int) Math.pow(k / 2, 2);
	}

	/**
	 * Returns the number of edge-switches as a function of k.
	 *
	 * @return Number of edge-switches per pod.
	 */
	public int getEdgeSwitchesPerPod() {
		// k/2
		return k / 2;
	}

	/**
	 * Returns the number of pods (equals k).
	 *
	 * @return Number of pods.
	 */
	public int getPods() {
		// k
		return k;
	}

	/**
	 * Returns the number of servers per pod as a function of k.
	 *
	 * @return Number of servers per pod.
	 */
	public int getServersPerPod() {
		// (k/2)^2
		return (int) Math.pow(k / 2, 2);
	}

	/**
	 * Returns the number of servers per edge switch.
	 *
	 * @return Number of servers per edge switch.
	 */
	public int getServersPerEdgeSwitch() {
		// servers_per_pod / edge switches_per_pod
		return getServersPerPod() / getEdgeSwitchesPerPod();
	}

}
