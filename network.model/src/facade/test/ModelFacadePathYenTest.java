package facade.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.Path;

/**
 * Test class for the ModelFacade that tests all Yen path related creations.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class ModelFacadePathYenTest {

  /*
   * Variables to save the ModelFacade's configuration of path limits to.
   */
  /**
   * Old lower limit value.
   */
  int oldLowerLimit;

  /**
   * Old upper limit value.
   */
  int oldUpperLimit;

  /**
   * Old K parameter.
   */
  int oldK;

  /**
   * Old Yen flag.
   */
  boolean oldYen;

  /**
   * Basic tests to run.
   */
  ModelFacadePathBasicTest basic;

  @Before
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

  @After
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
    basic.testTwoTierPathCreationFourServers();
  }

  @Test
  public void testTwoTierPathCreationFourServersTwoCoreSwitches() {
    ModelFacadePathBasicTest.twoTierSetupFourServersTwoCoreSwitches();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(56, allPaths.size());
  }

  @Test
  public void testOneTierNumberOfHopsPerPath() {
    basic.testOneTierNumberOfHopsPerPath();
  }

  @Test
  public void testTwoTierNumberOfHopsPerPath() {
    basic.testTwoTierNumberOfHopsPerPath();
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
    basic.testOnlyPathsWithThreeHops();
  }

  @Test
  public void testResidualBandwidth() {
    basic.testResidualBandwidth();
  }

}
