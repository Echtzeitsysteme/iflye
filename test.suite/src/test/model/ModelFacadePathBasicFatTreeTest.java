package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.FatTreeNetworkGenerator;
import generators.config.FatTreeConfig;
import model.SubstratePath;

/**
 * Test class for the ModelFacade that tests all basic path related creations.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadePathBasicFatTreeTest {

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

  @BeforeEach
  public void resetModel() {
    ModelFacade.getInstance().resetAll();

    // Save old values
    oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
    oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
  }

  @AfterEach
  public void restoreConfig() {
    ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
    ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
  }

  @Test
  public void testPathsAfterNetworkCreation() {
    final FatTreeConfig subConfig = new FatTreeConfig(4);
    final FatTreeNetworkGenerator gen = new FatTreeNetworkGenerator(subConfig);
    gen.createNetwork("net", false);

    assertTrue(!ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());
  }

  @Test
  public void testK4GoogleFatTreePathLength1() {
    setExactPathLength(1);
    final List<SubstratePath> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    final Set<Tuple<String, String>> mapping = new HashSet<Tuple<String, String>>();

    int j = 0;
    for (int i = 0; i < 16; i++) {
      if (i % 2 == 0 && i != 0) {
        j++;
      }
      mapping.add(new Tuple<String, String>("sub_srv_" + i, "sub_esw_" + j));
      mapping.add(new Tuple<String, String>("sub_esw_" + j, "sub_srv_" + i));
    }

    // Check total number of paths
    assertEquals(32, allPaths.size());
    ModelFacadePathBasicTest.checkPathSourcesAndTargets(mapping, allPaths);
  }

  @Test
  public void testK4GoogleFatTreePathLength2() {
    setExactPathLength(2);
    final List<SubstratePath> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(16 + 4 * 16, allPaths.size());
  }

  @Test
  public void testK4GoogleFatTreePathLength3() {
    setExactPathLength(3);
    final List<SubstratePath> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(5 * 16 * 2, allPaths.size());
  }

  @Test
  public void testK4GoogleFatTreePathLength4() {
    setExactPathLength(4);
    final List<SubstratePath> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(16 * (2 + 2 * 6), allPaths.size());
  }

  /*
   * Utility methods
   */

  private List<SubstratePath> createNetworkAndGetPaths(final int k) {
    final FatTreeConfig subConfig = new FatTreeConfig(k);
    final FatTreeNetworkGenerator gen = new FatTreeNetworkGenerator(subConfig);
    gen.createNetwork("sub", false);

    ModelFacade.getInstance().createAllPathsForNetwork("sub");

    return ModelFacade.getInstance().getAllPathsOfNetwork("sub");
  }

  private void setExactPathLength(final int length) {
    ModelFacadeConfig.MIN_PATH_LENGTH = length;
    ModelFacadeConfig.MAX_PATH_LENGTH = length;
  }

}
