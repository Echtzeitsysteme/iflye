package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.GoogleFatTreeNetworkGenerator;
import generators.config.GoogleFatTreeConfig;
import model.Path;

/**
 * Test class for the ModelFacade that tests all Yen path related creations for Google Fat Tree
 * networks. This test class only tests the number of created paths against a reference function.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadePathYenGoogleFatTreeTest {
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

  @BeforeEach
  public void resetModel() {
    ModelFacade.getInstance().resetAll();

    // Save old values
    oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
    oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
    oldK = ModelFacadeConfig.YEN_K;
    oldYen = ModelFacadeConfig.YEN_PATH_GEN;

    // Setup itself
    ModelFacadeConfig.YEN_PATH_GEN = true;
    // ModelFacadeConfig.YEN_K = 2;
  }

  @AfterEach
  public void restoreConfig() {
    ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
    ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
    ModelFacadeConfig.YEN_K = oldK;
    ModelFacadeConfig.YEN_PATH_GEN = oldYen;
  }

  @Test
  public void testK4GoogleFatTreePathLength1() {
    ModelFacadeConfig.YEN_K = 1;
    setExactPathLength(1);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(calcNumberOfPathsRef(4, 1), allPaths.size());
  }

  @Test
  public void testK4GoogleFatTreePathLength2() {
    ModelFacadeConfig.YEN_K = 1;
    setExactPathLength(2);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(calcNumberOfPathsRef(4, 2), allPaths.size());
  }

  @Test
  public void testK4GoogleFatTreePathLength3() {
    ModelFacadeConfig.YEN_K = 3;
    setExactPathLength(3);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    ModelFacade.getInstance().persistModel();

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(calcNumberOfPathsRef(4, 3), allPaths.size());
  }

  @Test
  public void testK4GoogleFatTreePathLength4() {
    ModelFacadeConfig.YEN_K = 3;
    setExactPathLength(4);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    ModelFacade.getInstance().persistModel();

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(calcNumberOfPathsRef(4, 4), allPaths.size());
  }

  /*
   * Utility methods
   */

  private List<Path> createNetworkAndGetPaths(final int k) {
    final GoogleFatTreeConfig subConfig = new GoogleFatTreeConfig(k);
    final GoogleFatTreeNetworkGenerator gen = new GoogleFatTreeNetworkGenerator(subConfig);
    gen.createNetwork("sub", false);

    ModelFacade.getInstance().createAllPathsForNetwork("sub");

    return ModelFacade.getInstance().getAllPathsOfNetwork("sub");
  }

  private void setExactPathLength(final int length) {
    ModelFacadeConfig.MIN_PATH_LENGTH = length;
    ModelFacadeConfig.MAX_PATH_LENGTH = length;
  }

  private int calcNumberOfPathsRef(final int k, final int hops) {
    int counter = 0;

    final int numberOfServers = (int) Math.pow(k / 2, 2) * k;
    final int numberOfEdgeSwitchesPerPod = k / 2;
    final int numberOfAggrSwitchesPerPod = k / 2;
    final int numberOfCoreSwitches = (int) Math.pow(k / 2, 2);

    switch (hops) {
      case 1:
        counter = numberOfServers;
        break;
      case 2:
        counter = numberOfServers * 2 + numberOfServers / 2;
        break;
      case 3:
        counter =
            numberOfServers * numberOfAggrSwitchesPerPod + numberOfServers * numberOfCoreSwitches;
        break;
      case 4:
        counter = numberOfServers * 16;
        break;
      default:
        throw new IllegalArgumentException();
    }

    // Two model paths for one actual path
    return counter * 2;
  }

}
