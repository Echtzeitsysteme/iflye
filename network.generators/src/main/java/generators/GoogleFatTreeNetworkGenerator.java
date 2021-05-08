package generators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import facade.ModelFacade;
import generators.config.GlobalGeneratorConfig;
import generators.config.GoogleFatTreeConfig;
import generators.config.IGeneratorConfig;

/**
 * Basic implementation of a google fat tree [1] network topology generator. [1] ALFARES , Mohammad
 * ; L OUKISSAS , Alexander ; V AHDAT , Amin: A Scalable, Commodity Data Center Network
 * Architecture. In: Proceedings of the ACM SIGCOMM 2008 conference on Data communication. (2008),
 * S. pp. 63–74
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class GoogleFatTreeNetworkGenerator implements INetworkGenerator {

  public static String CORE_SWITCH_PREFIX = "c" + GlobalGeneratorConfig.SWITCH;
  public static String AGGR_SWITCH_PREFIX = "aggr" + GlobalGeneratorConfig.SWITCH;
  public static String EDGE_SWITCH_PREFIX = "egde" + GlobalGeneratorConfig.SWITCH;

  /**
   * Configuration of this network generator instance.
   */
  private final GoogleFatTreeConfig config;

  private final List<String> coreSwitchIds;
  private final List<String> aggregationSwitchIds;
  private final List<String> edgeSwitchIds;
  private final List<String> serverIds;

  private int linkIdCounter = 0;

  /**
   * Private constructor to avoid direct instantiation of this class.
   * 
   * @param config Configuration for this generator.
   */
  public GoogleFatTreeNetworkGenerator(final IGeneratorConfig config) {
    if (!(config instanceof GoogleFatTreeConfig)) {
      throw new IllegalArgumentException("Configuration instance is not a GoogleFatTreeConfig.");
    }

    this.config = (GoogleFatTreeConfig) config;

    this.coreSwitchIds = new LinkedList<String>();
    this.aggregationSwitchIds = new LinkedList<String>();
    this.edgeSwitchIds = new LinkedList<String>();
    this.serverIds = new LinkedList<String>();
  }

  @Override
  public void createNetwork(final String networkId, final boolean isVirtual) {
    // Network
    if (!facade.networkExists(networkId)) {
      facade.addNetworkToRoot(networkId, isVirtual);
    }

    // Core switches
    for (int i = 0; i < config.getCoreSwitches(); i++) {
      final String id = getNextCoreSwitchId();
      coreSwitchIds.add(id);
      facade.addSwitchToNetwork(id, networkId, 0);
    }

    // Aggregation switches
    for (int i = 0; i < config.getAggregationSwitchesPerPod() * config.getPods(); i++) {
      final String id = getNextAggrSwitchId();
      aggregationSwitchIds.add(id);
      facade.addSwitchToNetwork(id, networkId, 1);
    }

    // Edge switches
    for (int i = 0; i < config.getEdgeSwitchesPerPod() * config.getPods(); i++) {
      final String id = getNextEdgeSwitchId();
      edgeSwitchIds.add(id);
      facade.addSwitchToNetwork(id, networkId, 2);
    }

    // Servers
    for (int i = 0; i < config.getServersPerPod() * config.getPods(); i++) {
      final String id = getNextServerId();
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
        facade.addLinkToNetwork(getNextLinkId(), networkId, config.getBwCoreToAggr(),
            aggregationSwitch, cswId);
        // Core switch -> Aggregation switch
        facade.addLinkToNetwork(getNextLinkId(), networkId, config.getBwCoreToAggr(), cswId,
            aggregationSwitch);
      }
    }

    // Aggregation switches <-> Edge switches
    for (int i = 0; i < config.getPods(); i++) {
      for (int j = 0; j < config.getAggregationSwitchesPerPod(); j++) {
        final String aggrSwId =
            aggregationSwitchIds.get((i * config.getAggregationSwitchesPerPod()) + j);
        for (int k = 0; k < config.getEdgeSwitchesPerPod(); k++) {
          final String edgeSwId = edgeSwitchIds.get((i * config.getEdgeSwitchesPerPod()) + k);
          // Edge switch -> Aggregation switch
          facade.addLinkToNetwork(getNextLinkId(), networkId, config.getBwAggrToEdge(), edgeSwId,
              aggrSwId);
          // Aggregation switch -> Edge switch
          facade.addLinkToNetwork(getNextLinkId(), networkId, config.getBwAggrToEdge(), aggrSwId,
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
        facade.addLinkToNetwork(getNextLinkId(), networkId, config.getRack().getBandwidthPerLink(),
            srvId, edgeSwId);
        // Edge switch -> Server
        facade.addLinkToNetwork(getNextLinkId(), networkId, config.getRack().getBandwidthPerLink(),
            edgeSwId, srvId);
      }
    }

    // Generate paths
    if (!isVirtual) {
      ModelFacade.getInstance().createAllPathsForNetwork(networkId);
    }
  }

  /*
   * Utility methods
   */

  private String getNextCoreSwitchId() {
    return CORE_SWITCH_PREFIX + GlobalGeneratorConfig.SEPARATOR + coreSwitchIds.size();
  }

  private String getNextAggrSwitchId() {
    return AGGR_SWITCH_PREFIX + GlobalGeneratorConfig.SEPARATOR + aggregationSwitchIds.size();
  }

  private String getNextEdgeSwitchId() {
    return EDGE_SWITCH_PREFIX + GlobalGeneratorConfig.SEPARATOR + edgeSwitchIds.size();
  }

  private String getNextServerId() {
    return GlobalGeneratorConfig.SERVER + GlobalGeneratorConfig.SEPARATOR + serverIds.size();
  }

  private String getNextLinkId() {
    return GlobalGeneratorConfig.LINK + GlobalGeneratorConfig.SEPARATOR + linkIdCounter++;
  }

}
