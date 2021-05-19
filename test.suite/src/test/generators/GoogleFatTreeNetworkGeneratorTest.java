package test.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import generators.GoogleFatTreeNetworkGenerator;
import generators.config.GoogleFatTreeConfig;
import generators.config.OneTierConfig;
import model.Link;
import model.Node;
import model.Server;
import model.VirtualNetwork;

/**
 * Test class for the GoogleFatTreeNetworkGenerator.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class GoogleFatTreeNetworkGeneratorTest extends IGeneratorTest {

  /*
   * Positive tests
   */

  @Test
  public void testNoNetworksAfterInit() {
    final GoogleFatTreeConfig config = new GoogleFatTreeConfig(4);
    new GoogleFatTreeNetworkGenerator(config);
    assertTrue(ModelFacade.getInstance().getAllNetworks().isEmpty());
  }

  @Test
  public void testNumberOfElementsKFour() {
    checkNumberOfElementsK(4);
  }

  @Test
  public void testNumberOfElementsKSix() {
    checkNumberOfElementsK(6);
  }

  @Test
  public void testNumberOfElementsKEight() {
    checkNumberOfElementsK(8);
  }

  @Test
  public void testAtLeastOnePathGenerated() {
    basicKFourSetup();

    assertTrue(1 < facade.getAllPathsOfNetwork("test").size());
  }

  @Test
  public void testVirtualNetwork() {
    final GoogleFatTreeConfig config = new GoogleFatTreeConfig(4);
    final GoogleFatTreeNetworkGenerator gen = new GoogleFatTreeNetworkGenerator(config);
    gen.createNetwork("virt", true);

    assertNotNull(facade.getNetworkById("virt"));
    assertTrue(facade.getNetworkById("virt") instanceof VirtualNetwork);
    assertTrue(facade.getAllPathsOfNetwork("virt").isEmpty());
  }

  @Test
  public void testServerParameters() {
    final GoogleFatTreeConfig config = new GoogleFatTreeConfig(4);
    final OneTierConfig rack = new OneTierConfig(-1, -1, false, 8, 9, 10, 1);
    config.setRack(rack);
    final GoogleFatTreeNetworkGenerator gen = new GoogleFatTreeNetworkGenerator(config);
    gen.createNetwork("test", false);

    assertFalse(facade.getAllServersOfNetwork("test").isEmpty());

    for (final Node n : facade.getAllServersOfNetwork("test")) {
      final Server s = (Server) n;
      assertEquals(8, s.getCpu());
      assertEquals(9, s.getMemory());
      assertEquals(10, s.getStorage());
    }
  }

  @Test
  public void testTwoSubstrateNetworks() {
    final GoogleFatTreeConfig config = new GoogleFatTreeConfig(4);
    final OneTierConfig rack = new OneTierConfig(-1, -1, false, 8, 9, 10, 1);
    config.setRack(rack);
    final GoogleFatTreeNetworkGenerator genA = new GoogleFatTreeNetworkGenerator(config);
    genA.createNetwork("test1", false);
    final GoogleFatTreeNetworkGenerator genB = new GoogleFatTreeNetworkGenerator(config);
    genB.createNetwork("test2", false);

    assertFalse(facade.getAllServersOfNetwork("test1").isEmpty());
    assertFalse(facade.getAllServersOfNetwork("test2").isEmpty());
  }

  @Test
  public void testServerDepth() {
    basicKFourSetup();

    final List<Node> servers = facade.getAllServersOfNetwork("test");
    for (final Node s : servers) {
      assertEquals(3, s.getDepth());
    }
  }

  @Test
  public void testServerOnlyOneBidirectionalLink() {
    basicKFourSetup();

    final List<Node> servers = facade.getAllServersOfNetwork("test");
    for (final Node s : servers) {
      assertEquals(1, s.getOutgoingLinks().size());
      assertEquals(1, s.getIncomingLinks().size());
    }
  }

  @Test
  public void testInvalidKParameterModulo() {
    final GoogleFatTreeConfig config = new GoogleFatTreeConfig(11);
    final GoogleFatTreeNetworkGenerator gen = new GoogleFatTreeNetworkGenerator(config);
    gen.createNetwork("test", false);

    // k % 2 != 0 must result in k = 4 (default value)
    assertEquals(4, filterNodesByDepth(facade.getAllSwitchesOfNetwork("test"), 0).size());
  }

  /*
   * Negative tests
   */

  @Test
  public void rejectConfigIsNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      new GoogleFatTreeNetworkGenerator(null);
    });
  }

  /*
   * Utility methods
   */

  /**
   * Sets a default GoogleFatTree Network with k = 4 up. It's name/ID is "test".
   */
  private void basicKFourSetup() {
    final GoogleFatTreeConfig config = new GoogleFatTreeConfig(4);
    final GoogleFatTreeNetworkGenerator gen = new GoogleFatTreeNetworkGenerator(config);
    gen.createNetwork("test", false);
  }

  /**
   * Checks the amount of elements (servers, switches, links) for a given parameter k. This method
   * also covers specific switch types (core, aggregation, and edge switches) as well as different
   * link bandwidths (core to aggregation, aggregation to edge, and edge to servers).
   * 
   * @param k Google Fat Tree build parameter.
   */
  private void checkNumberOfElementsK(final int k) {
    final int uniqueCoreBw = 7;
    final int uniqueAggrBw = 4;
    final int uniqueEdgeBw = 2;

    final GoogleFatTreeConfig config = new GoogleFatTreeConfig(k);
    config.setBwAggrToEdge(uniqueAggrBw);
    config.setBwCoreToAggr(uniqueCoreBw);
    final OneTierConfig rack = new OneTierConfig(-1, -1, false, 1, 1, 1, uniqueEdgeBw);
    config.setRack(rack);
    final GoogleFatTreeNetworkGenerator gen = new GoogleFatTreeNetworkGenerator(config);
    gen.createNetwork("test", false);

    final int numberOfPods = k;
    final int numberOfServersPerPod = (int) Math.pow(k / 2, 2);
    final int numberOfServers = numberOfPods * numberOfServersPerPod;
    final int numberOfCoreSwitches = (int) Math.pow(k / 2, 2);
    final int numberOfEdgeSwitchesPerPod = k / 2;
    final int numberOfEdgeSwitches = numberOfPods * numberOfEdgeSwitchesPerPod;
    final int numberOfAggregationSwitchesPerPod = k / 2;
    final int numberOfAggregationSwitches = numberOfPods * numberOfAggregationSwitchesPerPod;

    final int numberOfLinksSrvEdgeSw = numberOfServers;
    final int numberOfLinksEdgeSwAggrSw =
        numberOfPods * numberOfAggregationSwitchesPerPod * numberOfEdgeSwitchesPerPod;
    final int numberOfLinksAggrSwCoreSw = numberOfCoreSwitches * numberOfPods;
    final int numberOfLinks =
        numberOfLinksSrvEdgeSw + numberOfLinksEdgeSwAggrSw + numberOfLinksAggrSwCoreSw;

    // Amount of servers
    assertEquals(numberOfServers, facade.getAllServersOfNetwork("test").size());

    // Amount of switches
    assertEquals(numberOfCoreSwitches,
        filterNodesByDepth(facade.getAllSwitchesOfNetwork("test"), 0).size());
    assertEquals(numberOfAggregationSwitches,
        filterNodesByDepth(facade.getAllSwitchesOfNetwork("test"), 1).size());
    assertEquals(numberOfEdgeSwitches,
        filterNodesByDepth(facade.getAllSwitchesOfNetwork("test"), 2).size());

    // Number of links times two, because of all bidirectional links
    // Total amount of links
    assertEquals(2 * numberOfLinks, facade.getAllLinksOfNetwork("test").size());

    // Core links
    assertEquals(2 * numberOfLinksAggrSwCoreSw,
        filterLinksByBandwidth(facade.getAllLinksOfNetwork("test"), uniqueCoreBw).size());

    // Aggregation links
    assertEquals(2 * numberOfLinksEdgeSwAggrSw,
        filterLinksByBandwidth(facade.getAllLinksOfNetwork("test"), uniqueAggrBw).size());

    // Edge links
    assertEquals(2 * numberOfLinksSrvEdgeSw,
        filterLinksByBandwidth(facade.getAllLinksOfNetwork("test"), uniqueEdgeBw).size());
  }

  /**
   * Filters a given list of nodes by their depth.
   * 
   * @param input Input list of nodes to filter.
   * @param depth Depth parameter to filter for.
   * @return Set of all nodes of the input list but without every node that did not match the given
   *         depth.
   */
  private Set<Node> filterNodesByDepth(final List<Node> input, final int depth) {
    return input.stream() //
        .filter(n -> n.getDepth() == depth) //
        .collect(Collectors.toSet());
  }

  /**
   * Filters a given list of links by their bandwidth.
   * 
   * @param input Input list of links to filter.
   * @param bandwidth Bandwidth parameter to filter for.
   * @return Set of all links of the input list but without every link that did not match the given
   *         bandwidth.
   */
  private Set<Link> filterLinksByBandwidth(final List<Link> input, final int bandwidth) {
    return input.stream() //
        .filter(l -> l.getBandwidth() == bandwidth) //
        .collect(Collectors.toSet());
  }

}
