package facade.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

  @Before
  public void resetModel() {
    ModelFacade.getInstance().resetAll();

    // Save old values
    oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
    oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
  }

  @After
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
