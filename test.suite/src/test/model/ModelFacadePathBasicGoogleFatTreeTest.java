package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.GoogleFatTreeNetworkGenerator;
import generators.config.GoogleFatTreeConfig;
import model.Link;
import model.Node;
import model.Path;

/**
 * Test class for the ModelFacade that tests all basic path related creations.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadePathBasicGoogleFatTreeTest {

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
    final GoogleFatTreeConfig subConfig = new GoogleFatTreeConfig(4);
    final GoogleFatTreeNetworkGenerator gen = new GoogleFatTreeNetworkGenerator(subConfig);
    gen.createNetwork("net", false);

    assertTrue(!ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());
  }

  @Test
  public void testK4GoogleFatTreePathLength1() {
    setExactPathLength(1);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(32, allPaths.size());
  }

  @Test
  public void testK4GoogleFatTreePathLength2() {
    setExactPathLength(2);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(16 + 4 * 16, allPaths.size());
  }

  @Test
  public void testK4GoogleFatTreePathLength3() {
    setExactPathLength(3);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(5 * 16 * 2, allPaths.size());
  }

  @Test
  public void testK4GoogleFatTreePathLength4() {
    setExactPathLength(4);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(16 * (2 + 2 * 6), allPaths.size());
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

  /**
   * Tests a list of a sets of strings against a list of paths. The check ensures, that all name
   * sets are contained within the list of paths (with links).
   * 
   * @param linkNames List of sets of strings with link names for each path.
   * @param pathsToCheck List of paths to check.
   */
  private void checkPathLinkNames(final List<Set<String>> linkNames,
      final List<Path> pathsToCheck) {
    List<Set<String>> pathLinks = new LinkedList<Set<String>>();
    for (final Path p : pathsToCheck) {
      final Set<String> fromPath = new HashSet<String>();
      for (Link l : p.getLinks()) {
        fromPath.add(l.getName());
      }
      pathLinks.add(fromPath);
    }

    assertTrue(linkNames.containsAll(pathLinks));
    assertTrue(pathLinks.containsAll(linkNames));
  }

  /**
   * Tests a list of sets of strings against a list of paths. The check ensures, that all name sets
   * are contained within the list of paths (with nodes).
   * 
   * @param nodeNames List of sets of strings with node names for each path.
   * @param pathsToCheck List of paths to check.
   */
  private void checkPathNodeNames(final List<Set<String>> nodeNames,
      final List<Path> pathsToCheck) {
    List<Set<String>> pathNodes = new LinkedList<Set<String>>();
    for (final Path p : pathsToCheck) {
      final Set<String> fromPath = new HashSet<String>();
      for (Node n : p.getNodes()) {
        fromPath.add(n.getName());
      }
      pathNodes.add(fromPath);
    }

    // Ignore order
    assertTrue(nodeNames.containsAll(pathNodes));
    assertTrue(pathNodes.containsAll(nodeNames));
  }

  /**
   * Checks a given list of paths against a given map of strings to strings. The map represents the
   * mapping of sourceID to targetID for all paths.
   * 
   * @param mapping SourceID to targetID mapping.
   * @param pathsToCheck List of paths to check.
   */
  private void checkPathSourcesAndTargets(final Map<String, String> mapping,
      final List<Path> pathsToCheck) {
    for (final String sourceId : mapping.keySet()) {
      final String targetId = mapping.get(sourceId);
      checkPathSourceAndTarget(sourceId, targetId, pathsToCheck);
    }
  }

  /**
   * Checks a given list of paths for one specific sourceID and targetID. If no path with the given
   * sourceID and targetID can be found, the check fails.
   * 
   * @param sourceId SourceID to search for.
   * @param targetId TargetID to search for.
   * @param pathsToCheck List of paths to search in.
   */
  private void checkPathSourceAndTarget(final String sourceId, final String targetId,
      final List<Path> pathsToCheck) {
    for (final Path p : pathsToCheck) {
      if (p.getSource().getName().equals(sourceId) && p.getTarget().getName().equals(targetId)) {
        return;
      }
    }

    fail("No matching path was found for tuple: " + sourceId + " - " + targetId);
  }

}
