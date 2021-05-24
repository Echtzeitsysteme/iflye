package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.Path;

/**
 * Test class for the ModelFacade that tests all Yen path related creations.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadePathYenTest {
  /*
   * Variables to save the ModelFacade's configuration of path limits to.
   */

  /**
   * Old lower limit value.
   */
  private int oldLowerLimit;

  /**
   * Old upper limit value.
   */
  private int oldUpperLimit;

  /**
   * Old K parameter.
   */
  private int oldK;

  /**
   * Old Yen flag.
   */
  private boolean oldYen;

  /**
   * Basic tests to run.
   */
  private ModelFacadePathBasicTest basic;

  @BeforeEach
  public void resetModel() {
    ModelFacade.getInstance().resetAll();
    basic = new ModelFacadePathBasicTest();

    // Save old values
    oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
    oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
    oldK = ModelFacadeConfig.YEN_K;
    oldYen = ModelFacadeConfig.YEN_PATH_GEN;

    // Setup itself
    ModelFacadeConfig.YEN_PATH_GEN = true;
    ModelFacadeConfig.YEN_K = 2;
  }

  @AfterEach
  public void restoreConfig() {
    ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
    ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
    ModelFacadeConfig.YEN_K = oldK;
    ModelFacadeConfig.YEN_PATH_GEN = oldYen;
  }

  @Test
  public void testNoPathsAfterNetworkCreation() {
    basic.testNoPathsAfterNetworkCreation();
  }

  @Test
  public void testOneTierPathCreationTwoServers() {
    basic.testOneTierPathCreationTwoServers();
  }

  @Test
  public void testOneTierPathCreationFourServers() {
    basic.testOneTierPathCreationFourServers();
  }

  @Test
  public void testTwoTierPathCreationFourServers() {
    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    ModelFacadeConfig.YEN_K = 10;

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(96, allPaths.size());

    // Check individual source and targets
    final Map<String, String> mapping = new HashMap<String, String>();
    mapping.put("srv1", "rsw1");
    mapping.put("srv1", "csw1");
    mapping.put("srv1", "rsw2");
    mapping.put("srv1", "srv2");
    mapping.put("srv1", "srv3");
    mapping.put("srv1", "srv4");

    mapping.put("srv2", "srv1");
    mapping.put("srv2", "srv3");
    mapping.put("srv2", "srv4");
    mapping.put("srv2", "rsw1");
    mapping.put("srv2", "rsw2");
    mapping.put("srv2", "csw1");

    mapping.put("srv3", "srv1");
    mapping.put("srv3", "srv2");
    mapping.put("srv3", "srv4");
    mapping.put("srv3", "rsw1");
    mapping.put("srv3", "rsw2");
    mapping.put("srv3", "csw1");

    mapping.put("srv4", "srv1");
    mapping.put("srv4", "srv2");
    mapping.put("srv4", "srv3");
    mapping.put("srv4", "rsw1");
    mapping.put("srv4", "rsw2");
    mapping.put("srv4", "csw1");

    mapping.put("rsw1", "srv1");
    mapping.put("rsw1", "srv2");
    mapping.put("rsw1", "srv3");
    mapping.put("rsw1", "srv4");

    mapping.put("rsw2", "srv1");
    mapping.put("rsw2", "srv2");
    mapping.put("rsw2", "srv3");
    mapping.put("rsw2", "srv4");

    mapping.put("csw1", "srv1");
    mapping.put("csw1", "srv2");
    mapping.put("csw1", "srv3");
    mapping.put("csw1", "srv4");

    basic.checkPathSourcesAndTargets(mapping, allPaths);
  }

  @Test
  public void testOneTierPathCreationTwoServersTwoCoreSwitches() {
    ModelFacadeConfig.MIN_PATH_LENGTH = 2;
    ModelFacadeConfig.MAX_PATH_LENGTH = 2;
    ModelFacadePathBasicTest.oneTierSetupTwoServers();

    ModelFacade.getInstance().addSwitchToNetwork("sw2", "net", 0);
    ModelFacade.getInstance().addLinkToNetwork("ln5", "net", 1, "srv1", "sw2");
    ModelFacade.getInstance().addLinkToNetwork("ln6", "net", 2, "srv2", "sw2");
    ModelFacade.getInstance().addLinkToNetwork("ln7", "net", 1, "sw2", "srv1");
    ModelFacade.getInstance().addLinkToNetwork("ln8", "net", 2, "sw2", "srv2");

    ModelFacade.getInstance().createAllPathsForNetwork("net");

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(4, allPaths.size());
  }

  @Test
  public void testOneTierPathCreationTwoServersTwoCoreSwitchesConnected() {
    ModelFacadeConfig.MIN_PATH_LENGTH = 3;
    ModelFacadeConfig.MAX_PATH_LENGTH = 3;
    ModelFacadePathBasicTest.oneTierSetupTwoServers();

    ModelFacade.getInstance().addSwitchToNetwork("sw2", "net", 0);
    // Removed the links from srv1 to sw2 and vice versa on purpose
    ModelFacade.getInstance().addLinkToNetwork("ln6", "net", 2, "srv2", "sw2");
    ModelFacade.getInstance().addLinkToNetwork("ln8", "net", 2, "sw2", "srv2");
    ModelFacade.getInstance().addLinkToNetwork("ln9", "net", 10, "sw", "sw2");
    ModelFacade.getInstance().addLinkToNetwork("ln10", "net", 10, "sw2", "sw");

    ModelFacade.getInstance().createAllPathsForNetwork("net");

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(2, allPaths.size());
  }

  @Test
  public void testOneTierPathCreationTwoServersThreeCoreSwitches() {
    ModelFacadeConfig.MIN_PATH_LENGTH = 2;
    ModelFacadeConfig.MAX_PATH_LENGTH = 2;
    ModelFacadeConfig.YEN_K = 3;
    ModelFacadePathBasicTest.oneTierSetupTwoServers();

    ModelFacade.getInstance().addSwitchToNetwork("sw2", "net", 0);
    ModelFacade.getInstance().addSwitchToNetwork("sw3", "net", 0);
    ModelFacade.getInstance().addLinkToNetwork("ln5", "net", 1, "srv1", "sw2");
    ModelFacade.getInstance().addLinkToNetwork("ln6", "net", 2, "srv2", "sw2");
    ModelFacade.getInstance().addLinkToNetwork("ln7", "net", 1, "sw2", "srv1");
    ModelFacade.getInstance().addLinkToNetwork("ln8", "net", 2, "sw2", "srv2");
    ModelFacade.getInstance().addLinkToNetwork("ln9", "net", 1, "srv1", "sw3");
    ModelFacade.getInstance().addLinkToNetwork("ln10", "net", 2, "srv2", "sw3");
    ModelFacade.getInstance().addLinkToNetwork("ln11", "net", 1, "sw3", "srv1");
    ModelFacade.getInstance().addLinkToNetwork("ln12", "net", 2, "sw3", "srv2");

    ModelFacade.getInstance().createAllPathsForNetwork("net");

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(6, allPaths.size());
  }

  @Test
  public void testTwoTierPathCreationFourServersTwoCoreSwitchesLength1() {
    ModelFacadePathBasicTest.twoTierSetupFourServersTwoCoreSwitches();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 1;

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    for (final Path p : allPaths) {
      System.out.println(p.getSource().getName() + " -> " + p.getTarget().getName());
    }

    // Check total number of paths
    assertEquals(16, allPaths.size());
  }

  @Test
  public void testTwoTierPathCreationFourServersTwoCoreSwitchesLength2() {
    ModelFacadePathBasicTest.twoTierSetupFourServersTwoCoreSwitches();
    ModelFacadeConfig.MIN_PATH_LENGTH = 2;
    ModelFacadeConfig.MAX_PATH_LENGTH = 2;
    ModelFacadeConfig.YEN_K = 20;

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    int ref = 0;
    ref += 2 * 4 * 2; // Csw_n to servers * 2 (possibilities)
    ref += 4 * 5 * 2; // Srv_n to servers and core switches * 2 (possibilities)
    assertEquals(ref, allPaths.size());
  }

  @Test
  public void testTwoTierPathCreationFourServersTwoCoreSwitchesLength3() {
    ModelFacadePathBasicTest.twoTierSetupFourServersTwoCoreSwitches();
    ModelFacadeConfig.MIN_PATH_LENGTH = 3;
    ModelFacadeConfig.MAX_PATH_LENGTH = 3;
    ModelFacadeConfig.YEN_K = 10;

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(32, allPaths.size());
  }

  @Test
  public void testTwoTierPathCreationFourServersTwoCoreSwitchesLength4() {
    ModelFacadePathBasicTest.twoTierSetupFourServersTwoCoreSwitches();
    ModelFacadeConfig.MIN_PATH_LENGTH = 4;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    ModelFacadeConfig.YEN_K = 10;

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    int ref = 0;
    ref += 2 * 4 * 2; // Csw_n * Srv_n * 2 (possibilities)
    ref += 4 * (2 * 2 + 4 * (4 - 1)); // Srv_n * (Csw_n * 2 (possibilities) + 4 (possibilities) *
                                      // (Srv_n - 1))
    assertEquals(ref, allPaths.size());
  }

  /**
   * Setup: Two servers and four switches srv1 - sw1; srv2 - sw2; sw1 - sw3; sw2 - sw3; sw1 - sw4;
   * sw2 - sw4;
   */
  @Test
  public void testOneTierPathCreationTwoServersForCoreSwitchesFourHops() {
    // Setup
    ModelFacade.getInstance().addNetworkToRoot("net", false);
    ModelFacade.getInstance().addSwitchToNetwork("sw1", "net", 0);
    ModelFacade.getInstance().addSwitchToNetwork("sw2", "net", 0);
    ModelFacade.getInstance().addSwitchToNetwork("sw3", "net", 0);
    ModelFacade.getInstance().addSwitchToNetwork("sw4", "net", 0);

    ModelFacade.getInstance().addServerToNetwork("srv1", "net", 0, 0, 0, 1);
    ModelFacade.getInstance().addServerToNetwork("srv2", "net", 0, 0, 0, 1);

    ModelFacade.getInstance().addLinkToNetwork("ln1", "net", 0, "srv1", "sw1");
    ModelFacade.getInstance().addLinkToNetwork("ln2", "net", 0, "srv2", "sw2");
    ModelFacade.getInstance().addLinkToNetwork("ln3", "net", 0, "sw1", "sw3");
    ModelFacade.getInstance().addLinkToNetwork("ln4", "net", 0, "sw1", "sw4");
    ModelFacade.getInstance().addLinkToNetwork("ln5", "net", 0, "sw2", "sw3");
    ModelFacade.getInstance().addLinkToNetwork("ln6", "net", 0, "sw2", "sw4");

    ModelFacade.getInstance().addLinkToNetwork("ln7", "net", 0, "sw1", "srv1");
    ModelFacade.getInstance().addLinkToNetwork("ln8", "net", 0, "sw2", "srv2");
    ModelFacade.getInstance().addLinkToNetwork("ln9", "net", 0, "sw3", "sw1");
    ModelFacade.getInstance().addLinkToNetwork("ln10", "net", 0, "sw4", "sw1");
    ModelFacade.getInstance().addLinkToNetwork("ln11", "net", 0, "sw3", "sw2");
    ModelFacade.getInstance().addLinkToNetwork("ln12", "net", 0, "sw4", "sw2");

    ModelFacadeConfig.MIN_PATH_LENGTH = 4;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    ModelFacadeConfig.YEN_K = 2;

    // Create paths and check values
    ModelFacade.getInstance().createAllPathsForNetwork("net");
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(12, allPaths.size());
  }

  @Test
  public void testOneTierNumberOfHopsPerPath() {
    basic.testOneTierNumberOfHopsPerPath();
  }

  @Test
  public void testTwoTierNumberOfHopsPerPath() {
    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    ModelFacadeConfig.YEN_K = 10;

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

    int counterOneHop = 0;
    int counterTwoHops = 0;
    int counterThreeHops = 0;
    int counterFourHops = 0;

    for (final Path p : allPaths) {
      // Number of links must be number of hops
      assertEquals(p.getLinks().size(), p.getHops());

      // if source or target is a core switch
      if (p.getHops() == 1) {
        counterOneHop += 1;
      } else if (p.getHops() == 2) {
        counterTwoHops += 1;
      } else if (p.getHops() == 3) {
        counterThreeHops += 1;
      } else if (p.getHops() == 4) {
        counterFourHops += 1;
      }
    }

    assertEquals(16, counterOneHop);
    assertEquals(40, counterTwoHops);
    assertEquals(16, counterThreeHops);
    assertEquals(24, counterFourHops);
  }

  @Test
  public void testOneTierBandwidthAmoutPerPath() {
    basic.testOneTierBandwidthAmoutPerPath();
  }

  @Test
  public void testOneTierContainedLinksAmount() {
    basic.testOneTierContainedLinksAmount();
  }

  @Test
  public void testOneTierContainedLinksNames() {
    basic.testOneTierContainedLinksNames();
  }

  @Test
  public void testOneTierContainedNodesAmount() {
    basic.testOneTierContainedNodesAmount();
  }

  @Test
  public void testOneTierContainedNodesNames() {
    basic.testOneTierContainedNodesNames();
  }

  @Test
  public void testTwoTierNoNameIsNull() {
    basic.testTwoTierNoNameIsNull();
  }

  @Test
  public void testNoPathsLowerLimit() {
    basic.testNoPathsLowerLimit();
  }

  @Test
  public void testNoPathsUpperLimit() {
    basic.testNoPathsUpperLimit();
  }

  @Test
  public void testOnlyPathsWithTwoHops() {
    basic.testOnlyPathsWithTwoHops();
  }

  @Test
  public void testOnlyPathsWithThreeHops() {
    // Setup for this test
    ModelFacadeConfig.MIN_PATH_LENGTH = 3;
    ModelFacadeConfig.MAX_PATH_LENGTH = 3;
    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacade.getInstance().createAllPathsForNetwork("net");

    final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(generatedPaths.isEmpty());

    assertEquals(16, generatedPaths.size());
  }

  @Test
  public void testResidualBandwidth() {
    basic.testResidualBandwidth();
  }

}
