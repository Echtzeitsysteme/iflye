package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.Link;
import model.Node;
import model.Path;

/**
 * Test class for the ModelFacade that tests two tier path related creations.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadePathTwoTierDetailTest {

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
  public void testTwoTierLinksAgainstNodes() {
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacade.getInstance().createAllPathsForNetwork("net");

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    for (final Path p : allPaths) {
      checkPathLinksAgainstNodes(p);
    }
  }

  @Test
  public void testTwoTierLinksDetailLengthOne() {
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 1;
    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacade.getInstance().createAllPathsForNetwork("net");

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Test if every path is present
    final Set<List<String>> references = new HashSet<List<String>>();
    references.add(Arrays.asList(new String[] {"srv1", "rsw1"}));
    references.add(Arrays.asList(new String[] {"rsw1", "srv1"}));
    references.add(Arrays.asList(new String[] {"srv1", "rsw2"}));
    references.add(Arrays.asList(new String[] {"rsw2", "srv1"}));

    references.add(Arrays.asList(new String[] {"srv2", "rsw1"}));
    references.add(Arrays.asList(new String[] {"rsw1", "srv2"}));
    references.add(Arrays.asList(new String[] {"srv2", "rsw2"}));
    references.add(Arrays.asList(new String[] {"rsw2", "srv2"}));

    references.add(Arrays.asList(new String[] {"srv3", "rsw1"}));
    references.add(Arrays.asList(new String[] {"rsw1", "srv3"}));
    references.add(Arrays.asList(new String[] {"srv3", "rsw2"}));
    references.add(Arrays.asList(new String[] {"rsw2", "srv3"}));

    references.add(Arrays.asList(new String[] {"srv4", "rsw1"}));
    references.add(Arrays.asList(new String[] {"rsw1", "srv4"}));
    references.add(Arrays.asList(new String[] {"srv4", "rsw2"}));
    references.add(Arrays.asList(new String[] {"rsw2", "srv4"}));

    checkPathNodesAgainstRef(allPaths, references);
  }

  @Test
  public void testTwoTierLinksDetailLengthTwo() {
    ModelFacadeConfig.MIN_PATH_LENGTH = 2;
    ModelFacadeConfig.MAX_PATH_LENGTH = 2;
    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacade.getInstance().createAllPathsForNetwork("net");

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Test if every path is present
    final Set<List<String>> references = new HashSet<List<String>>();
    references.add(Arrays.asList(new String[] {"srv1", "rsw1", "csw1"}));
    references.add(Arrays.asList(new String[] {"csw1", "rsw1", "srv1"}));

    references.add(Arrays.asList(new String[] {"srv2", "rsw1", "csw1"}));
    references.add(Arrays.asList(new String[] {"csw1", "rsw1", "srv2"}));

    references.add(Arrays.asList(new String[] {"srv3", "rsw1", "csw1"}));
    references.add(Arrays.asList(new String[] {"csw1", "rsw1", "srv3"}));

    references.add(Arrays.asList(new String[] {"srv4", "rsw1", "csw1"}));
    references.add(Arrays.asList(new String[] {"csw1", "rsw1", "srv4"}));

    references.add(Arrays.asList(new String[] {"srv1", "rsw1", "srv2"}));
    references.add(Arrays.asList(new String[] {"srv2", "rsw1", "srv1"}));

    references.add(Arrays.asList(new String[] {"srv1", "rsw1", "srv3"}));
    references.add(Arrays.asList(new String[] {"srv3", "rsw1", "srv1"}));
    references.add(Arrays.asList(new String[] {"srv1", "rsw1", "srv4"}));
    references.add(Arrays.asList(new String[] {"srv4", "rsw1", "srv1"}));

    references.add(Arrays.asList(new String[] {"srv2", "rsw1", "srv3"}));
    references.add(Arrays.asList(new String[] {"srv3", "rsw1", "srv2"}));
    references.add(Arrays.asList(new String[] {"srv2", "rsw1", "srv4"}));
    references.add(Arrays.asList(new String[] {"srv4", "rsw1", "srv2"}));

    references.add(Arrays.asList(new String[] {"srv3", "rsw1", "srv4"}));
    references.add(Arrays.asList(new String[] {"srv4", "rsw1", "srv3"}));

    checkPathNodesAgainstRef(allPaths, references);
  }

  /**
   * Checks nodes of paths against a given reference set.
   * 
   * @param paths List of paths to check.
   * @param references Set of lists of node names to check against.
   */
  private void checkPathNodesAgainstRef(final List<Path> paths,
      final Set<List<String>> references) {
    assertEquals(references.size(), paths.size());

    final Set<List<String>> referencesCopy = new HashSet<List<String>>(references);

    for (final Path p : paths) {
      List<String> pNodes = new LinkedList<String>();
      for (final Node n : p.getNodes()) {
        pNodes.add(n.getName());
      }

      assertTrue(referencesCopy.contains(pNodes));
      referencesCopy.remove(pNodes);
    }

    assertEquals(0, referencesCopy.size());
  }

  /**
   * Ensures that the ordering of nodes matches the ordering of links (their source and target
   * nodes) for a given path.
   * 
   * @param p Path to check link and nodes against each other.
   */
  private void checkPathLinksAgainstNodes(final Path p) {
    final List<Node> nodes = p.getNodes();
    final List<Link> links = p.getLinks();

    assertEquals(links.size() + 1, nodes.size());

    for (int i = 0; i < links.size(); i++) {
      assertEquals(nodes.get(i), links.get(i).getSource());
      assertEquals(nodes.get(i + 1), links.get(i).getTarget());
    }
  }

}
