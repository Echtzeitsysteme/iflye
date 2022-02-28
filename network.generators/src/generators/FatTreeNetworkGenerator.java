package generators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import generators.config.FatTreeConfig;
import generators.config.GlobalGeneratorConfig;
import generators.config.IGeneratorConfig;

/**
 * Basic implementation of a fat tree [1] network topology generator.
 *
 * [1] ALFARES , Mohammad ; L OUKISSAS , Alexander ; V AHDAT , Amin: A Scalable,
 * Commodity Data Center Network Architecture. In: Proceedings of the ACM
 * SIGCOMM 2008 conference on Data communication. (2008), S. pp. 63–74
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class FatTreeNetworkGenerator implements INetworkGenerator {

	/**
	 * Core switch ID prefix.
	 */
	public static String CORE_SWITCH_PREFIX = "c" + GlobalGeneratorConfig.SWITCH;

	/**
	 * Aggregation switch ID prefix.
	 */
	public static String AGGR_SWITCH_PREFIX = "a" + GlobalGeneratorConfig.SWITCH;

	/**
	 * Edge switch (rack switch) ID prefix.
	 */
	public static String EDGE_SWITCH_PREFIX = "e" + GlobalGeneratorConfig.SWITCH;

	/**
	 * Configuration of this network generator instance.
	 */
	private final FatTreeConfig config;

	/**
	 * List of generated core switch IDs.
	 */
	private final List<String> coreSwitchIds;

	/**
	 * List of generated aggregation switch IDs.
	 */
	private final List<String> aggregationSwitchIds;

	/**
	 * List of generated edge switch IDs.
	 */
	private final List<String> edgeSwitchIds;

	/**
	 * List of generated server IDs.
	 */
	private final List<String> serverIds;

	/**
	 * Counter of the generated links. Will be used to generate the next free link
	 * ID.
	 */
	private int linkIdCounter = 0;

	/**
	 * Private constructor to avoid direct instantiation of this class.
	 *
	 * @param config Configuration for this generator.
	 */
	public FatTreeNetworkGenerator(final IGeneratorConfig config) {
		if (!(config instanceof FatTreeConfig)) {
			throw new IllegalArgumentException("Configuration instance is not a GoogleFatTreeConfig.");
		}

		this.config = (FatTreeConfig) config;

		this.coreSwitchIds = new LinkedList<>();
		this.aggregationSwitchIds = new LinkedList<>();
		this.edgeSwitchIds = new LinkedList<>();
		this.serverIds = new LinkedList<>();
	}

	@Override
	public void createNetwork(final String networkId, final boolean isVirtual) {
		// Network
		if (!facade.networkExists(networkId)) {
			facade.addNetworkToRoot(networkId, isVirtual);
		}

		// Core switches
		for (int i = 0; i < config.getCoreSwitches(); i++) {
			final String id = getNextCoreSwitchId(networkId);
			coreSwitchIds.add(id);
			facade.addSwitchToNetwork(id, networkId, 0);
		}

		// Aggregation switches
		for (int i = 0; i < config.getAggregationSwitchesPerPod() * config.getPods(); i++) {
			final String id = getNextAggrSwitchId(networkId);
			aggregationSwitchIds.add(id);
			facade.addSwitchToNetwork(id, networkId, 1);
		}

		// Edge switches
		for (int i = 0; i < config.getEdgeSwitchesPerPod() * config.getPods(); i++) {
			final String id = getNextEdgeSwitchId(networkId);
			edgeSwitchIds.add(id);
			facade.addSwitchToNetwork(id, networkId, 2);
		}

		// Servers
		for (int i = 0; i < config.getServersPerPod() * config.getPods(); i++) {
			final String id = getNextServerId(networkId);
			serverIds.add(id);
			facade.addServerToNetwork(id, networkId, config.getRack().getCpuPerServer(),
					config.getRack().getMemoryPerServer(), config.getRack().getStoragePerServer(), 3);
		}

		// Links
		// Core switches <-> Aggregation switches
		final Iterator<String> itAggrSw = aggregationSwitchIds.iterator();
		Iterator<String> itCoreSw = coreSwitchIds.iterator();
		int numberOfCoreSwitches = 1;

		while (itAggrSw.hasNext()) {
			final String aggregationSwitch = itAggrSw.next();
			final List<String> coreSwitches = new LinkedList<>();

			// Reset the core-switch iterator after every pod
			if (numberOfCoreSwitches >= config.getCoreSwitches()) {
				itCoreSw = coreSwitchIds.iterator();
				numberOfCoreSwitches = 1;
			}
			// Go through the core-switches
			for (int i = 0; i < config.getAggregationSwitchesPerPod(); i++) {
				coreSwitches.add(itCoreSw.next());
				numberOfCoreSwitches++;
			}

			// Create actual links
			for (final String cswId : coreSwitches) {
				// Aggregation switch -> Core switch
				facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getBwCoreToAggr(),
						aggregationSwitch, cswId);
				// Core switch -> Aggregation switch
				facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getBwCoreToAggr(), cswId,
						aggregationSwitch);
			}
		}

		// Aggregation switches <-> Edge switches
		for (int i = 0; i < config.getPods(); i++) {
			for (int j = 0; j < config.getAggregationSwitchesPerPod(); j++) {
				final String aggrSwId = aggregationSwitchIds.get((i * config.getAggregationSwitchesPerPod()) + j);
				for (int k = 0; k < config.getEdgeSwitchesPerPod(); k++) {
					final String edgeSwId = edgeSwitchIds.get((i * config.getEdgeSwitchesPerPod()) + k);
					// Edge switch -> Aggregation switch
					facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getBwAggrToEdge(), edgeSwId,
							aggrSwId);
					// Aggregation switch -> Edge switch
					facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getBwAggrToEdge(), aggrSwId,
							edgeSwId);
				}
			}
		}

		// Edge switches <-> Servers
		for (int i = 0; i < config.getEdgeSwitchesPerPod() * config.getPods(); i++) {
			final String edgeSwId = edgeSwitchIds.get(i);
			for (int j = 0; j < config.getServersPerEdgeSwitch(); j++) {
				final String srvId = serverIds.get((i * config.getServersPerEdgeSwitch()) + j);
				// Server -> Edge switch
				facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getRack().getBandwidthPerLink(),
						srvId, edgeSwId);
				// Edge switch -> Server
				facade.addLinkToNetwork(getNextLinkId(networkId), networkId, config.getRack().getBandwidthPerLink(),
						edgeSwId, srvId);
			}
		}
	}

	/*
	 * Utility methods
	 */

	/**
	 * Returns the next free core switch ID.
	 *
	 * @param networkId Network ID to work with.
	 * @return Next free core switch ID.
	 */
	private String getNextCoreSwitchId(final String networkId) {
		return networkId + GlobalGeneratorConfig.SEPARATOR + CORE_SWITCH_PREFIX + GlobalGeneratorConfig.SEPARATOR
				+ coreSwitchIds.size();
	}

	/**
	 * Returns the next free aggregation switch ID.
	 *
	 * @param networkId Network ID to work with.
	 * @return Next free aggregation switch ID.
	 */
	private String getNextAggrSwitchId(final String networkId) {
		return networkId + GlobalGeneratorConfig.SEPARATOR + AGGR_SWITCH_PREFIX + GlobalGeneratorConfig.SEPARATOR
				+ aggregationSwitchIds.size();
	}

	/**
	 * Returns the next free edge switch ID.
	 *
	 * @param networkId Network ID to work with.
	 * @return Next free edge switch ID.
	 */
	private String getNextEdgeSwitchId(final String networkId) {
		return networkId + GlobalGeneratorConfig.SEPARATOR + EDGE_SWITCH_PREFIX + GlobalGeneratorConfig.SEPARATOR
				+ edgeSwitchIds.size();
	}

	/**
	 * Returns the next free server ID.
	 *
	 * @param networkId Network ID to work with.
	 * @return Next free server ID.
	 */
	private String getNextServerId(final String networkId) {
		return networkId + GlobalGeneratorConfig.SEPARATOR + GlobalGeneratorConfig.SERVER
				+ GlobalGeneratorConfig.SEPARATOR + serverIds.size();
	}

	/**
	 * Returns the next free link ID.
	 *
	 * @param networkId Network ID to work with.
	 * @return Next free link ID.
	 */
	private String getNextLinkId(final String networkId) {
		return networkId + GlobalGeneratorConfig.SEPARATOR + GlobalGeneratorConfig.LINK
				+ GlobalGeneratorConfig.SEPARATOR + linkIdCounter++;
	}

}
