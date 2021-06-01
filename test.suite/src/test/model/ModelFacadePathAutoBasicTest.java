package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.GoogleFatTreeNetworkGenerator;
import generators.config.GoogleFatTreeConfig;
import model.Path;
import model.Switch;

/**
 * Test class for the ModelFacade that tests all basic path related creations which rely on
 * automatic determination of the maximum path length.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadePathAutoBasicTest {

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
   * Old auto value.
   */
  private boolean oldAutoOption;

  @BeforeEach
  public void resetModel() {
    ModelFacade.getInstance().resetAll();

    // Save old values
    oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
    oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
    oldAutoOption = ModelFacadeConfig.MAX_PATH_LENGTH_AUTO;
    ModelFacadeConfig.MAX_PATH_LENGTH_AUTO = true;
  }

  @AfterEach
  public void restoreConfig() {
    ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
    ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
    ModelFacadeConfig.MAX_PATH_LENGTH_AUTO = oldAutoOption;
  }

  @Test
  public void testNoPathsAfterNetworkCreation() {
    ModelFacadePathBasicTest.oneTierSetupTwoServers();
    assertTrue(ModelFacade.getInstance().getAllPathsOfNetwork("net").isEmpty());
  }

  @Test
  public void testOneTierPathCreationTwoServers() {
    ModelFacadePathBasicTest.oneTierSetupTwoServers();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 2;
    // ^maximum path length should be overwritten to 1

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(1, ModelFacadeConfig.MAX_PATH_LENGTH);

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(4, allPaths.size());

    // Check individual source and targets
    final Set<Tuple<String, String>> mapping = new HashSet<Tuple<String, String>>();
    mapping.add(new Tuple<String, String>("srv1", "sw"));
    mapping.add(new Tuple<String, String>("sw", "srv1"));
    mapping.add(new Tuple<String, String>("srv2", "sw"));
    mapping.add(new Tuple<String, String>("sw", "srv2"));

    ModelFacadePathBasicTest.checkPathSourcesAndTargets(mapping, allPaths);
  }

  @Test
  public void testOneTierPathCreationFourServers() {
    ModelFacadePathBasicTest.oneTierSetupFourServers();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    // ^maximum path length should be overwritten to 1

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(1, ModelFacadeConfig.MAX_PATH_LENGTH);

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(8, allPaths.size());

    // Check individual source and targets
    final Set<Tuple<String, String>> mapping = new HashSet<Tuple<String, String>>();
    mapping.add(new Tuple<String, String>("srv1", "sw"));
    mapping.add(new Tuple<String, String>("sw", "srv1"));
    mapping.add(new Tuple<String, String>("srv2", "sw"));
    mapping.add(new Tuple<String, String>("sw", "srv2"));
    mapping.add(new Tuple<String, String>("srv3", "sw"));
    mapping.add(new Tuple<String, String>("sw", "srv3"));
    mapping.add(new Tuple<String, String>("srv4", "sw"));
    mapping.add(new Tuple<String, String>("sw", "srv4"));

    ModelFacadePathBasicTest.checkPathSourcesAndTargets(mapping, allPaths);
  }

  @Test
  public void testTwoTierPathCreationFourServers() {
    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    // ^maximum path length should be overwritten to 2

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(2, ModelFacadeConfig.MAX_PATH_LENGTH);

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(36, allPaths.size());

    // Check individual source and targets
    final Set<Tuple<String, String>> mapping = new HashSet<Tuple<String, String>>();
    mapping.add(new Tuple<String, String>("srv1", "rsw1"));
    mapping.add(new Tuple<String, String>("srv1", "csw1"));
    mapping.add(new Tuple<String, String>("srv1", "rsw2"));
    mapping.add(new Tuple<String, String>("srv1", "srv2"));
    mapping.add(new Tuple<String, String>("srv1", "srv3"));
    mapping.add(new Tuple<String, String>("srv1", "srv4"));

    mapping.add(new Tuple<String, String>("srv2", "srv1"));
    mapping.add(new Tuple<String, String>("srv2", "srv3"));
    mapping.add(new Tuple<String, String>("srv2", "srv4"));
    mapping.add(new Tuple<String, String>("srv2", "rsw1"));
    mapping.add(new Tuple<String, String>("srv2", "rsw2"));
    mapping.add(new Tuple<String, String>("srv2", "csw1"));

    mapping.add(new Tuple<String, String>("srv3", "srv1"));
    mapping.add(new Tuple<String, String>("srv3", "srv2"));
    mapping.add(new Tuple<String, String>("srv3", "srv4"));
    mapping.add(new Tuple<String, String>("srv3", "rsw1"));
    mapping.add(new Tuple<String, String>("srv3", "rsw2"));
    mapping.add(new Tuple<String, String>("srv3", "csw1"));

    mapping.add(new Tuple<String, String>("srv4", "srv1"));
    mapping.add(new Tuple<String, String>("srv4", "srv2"));
    mapping.add(new Tuple<String, String>("srv4", "srv3"));
    mapping.add(new Tuple<String, String>("srv4", "rsw1"));
    mapping.add(new Tuple<String, String>("srv4", "rsw2"));
    mapping.add(new Tuple<String, String>("srv4", "csw1"));

    mapping.add(new Tuple<String, String>("rsw1", "srv1"));
    mapping.add(new Tuple<String, String>("rsw1", "srv2"));
    mapping.add(new Tuple<String, String>("rsw1", "srv3"));
    mapping.add(new Tuple<String, String>("rsw1", "srv4"));

    mapping.add(new Tuple<String, String>("rsw2", "srv1"));
    mapping.add(new Tuple<String, String>("rsw2", "srv2"));
    mapping.add(new Tuple<String, String>("rsw2", "srv3"));
    mapping.add(new Tuple<String, String>("rsw2", "srv4"));

    mapping.add(new Tuple<String, String>("csw1", "srv1"));
    mapping.add(new Tuple<String, String>("csw1", "srv2"));
    mapping.add(new Tuple<String, String>("csw1", "srv3"));
    mapping.add(new Tuple<String, String>("csw1", "srv4"));

    ModelFacadePathBasicTest.checkPathSourcesAndTargets(mapping, allPaths);
  }

  @Test
  public void testTwoTierPathCreationFourServersTwoCoreSwitches() {
    ModelFacadePathBasicTest.twoTierSetupFourServersTwoCoreSwitches();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    // ^maximum path length should be overwritten to 2

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(2, ModelFacadeConfig.MAX_PATH_LENGTH);

    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    assertEquals(44, allPaths.size());

    // Check individual source and targets
    final Set<Tuple<String, String>> mapping = new HashSet<Tuple<String, String>>();

    mapping.add(new Tuple<String, String>("srv1", "rsw1"));
    mapping.add(new Tuple<String, String>("srv1", "csw1"));
    mapping.add(new Tuple<String, String>("srv1", "csw2"));
    mapping.add(new Tuple<String, String>("srv1", "rsw2"));
    mapping.add(new Tuple<String, String>("srv1", "srv2"));
    mapping.add(new Tuple<String, String>("srv1", "srv3"));
    mapping.add(new Tuple<String, String>("srv1", "srv4"));

    mapping.add(new Tuple<String, String>("srv2", "srv1"));
    mapping.add(new Tuple<String, String>("srv2", "srv3"));
    mapping.add(new Tuple<String, String>("srv2", "srv4"));
    mapping.add(new Tuple<String, String>("srv2", "rsw1"));
    mapping.add(new Tuple<String, String>("srv2", "rsw2"));
    mapping.add(new Tuple<String, String>("srv2", "csw1"));
    mapping.add(new Tuple<String, String>("srv2", "csw2"));

    mapping.add(new Tuple<String, String>("srv3", "srv1"));
    mapping.add(new Tuple<String, String>("srv3", "srv2"));
    mapping.add(new Tuple<String, String>("srv3", "srv4"));
    mapping.add(new Tuple<String, String>("srv3", "rsw1"));
    mapping.add(new Tuple<String, String>("srv3", "rsw2"));
    mapping.add(new Tuple<String, String>("srv3", "csw1"));
    mapping.add(new Tuple<String, String>("srv3", "csw2"));

    mapping.add(new Tuple<String, String>("srv4", "srv1"));
    mapping.add(new Tuple<String, String>("srv4", "srv2"));
    mapping.add(new Tuple<String, String>("srv4", "srv3"));
    mapping.add(new Tuple<String, String>("srv4", "rsw1"));
    mapping.add(new Tuple<String, String>("srv4", "rsw2"));
    mapping.add(new Tuple<String, String>("srv4", "csw1"));
    mapping.add(new Tuple<String, String>("srv4", "csw2"));

    mapping.add(new Tuple<String, String>("rsw1", "srv1"));
    mapping.add(new Tuple<String, String>("rsw1", "srv2"));
    mapping.add(new Tuple<String, String>("rsw1", "srv3"));
    mapping.add(new Tuple<String, String>("rsw1", "srv4"));

    mapping.add(new Tuple<String, String>("rsw2", "srv1"));
    mapping.add(new Tuple<String, String>("rsw2", "srv2"));
    mapping.add(new Tuple<String, String>("rsw2", "srv3"));
    mapping.add(new Tuple<String, String>("rsw2", "srv4"));

    mapping.add(new Tuple<String, String>("csw1", "srv1"));
    mapping.add(new Tuple<String, String>("csw1", "srv2"));
    mapping.add(new Tuple<String, String>("csw1", "srv3"));
    mapping.add(new Tuple<String, String>("csw1", "srv4"));

    mapping.add(new Tuple<String, String>("csw2", "srv1"));
    mapping.add(new Tuple<String, String>("csw2", "srv2"));
    mapping.add(new Tuple<String, String>("csw2", "srv3"));
    mapping.add(new Tuple<String, String>("csw2", "srv4"));

    ModelFacadePathBasicTest.checkPathSourcesAndTargets(mapping, allPaths);
  }

  @Test
  public void testOneTierNumberOfHopsPerPath() {
    ModelFacadePathBasicTest.oneTierSetupTwoServers();

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

    for (final Path p : allPaths) {
      if (p.getSource() instanceof Switch || p.getTarget() instanceof Switch) {
        assertEquals(1, p.getHops());
      } else {
        assertEquals(2, p.getHops());
      }
    }
  }

  @Test
  public void testTwoTierNumberOfHopsPerPath() {
    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    // ^maximum path length should be overwritten to 2

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(2, ModelFacadeConfig.MAX_PATH_LENGTH);

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
    assertEquals(20, counterTwoHops);
    assertEquals(0, counterThreeHops);
    assertEquals(0, counterFourHops);
  }

  @Test
  public void testOneTierContainedLinksNames() {
    ModelFacadePathBasicTest.oneTierSetupTwoServers();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    // ^maximum path length should be overwritten to 1

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(1, ModelFacadeConfig.MAX_PATH_LENGTH);
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

    List<Set<String>> linkNames = new LinkedList<Set<String>>();
    linkNames.add(Set.of("ln1"));
    linkNames.add(Set.of("ln2"));
    linkNames.add(Set.of("ln3"));
    linkNames.add(Set.of("ln4"));

    ModelFacadePathBasicTest.checkPathLinkNames(linkNames, allPaths);
  }

  @Test
  public void testOneTierContainedNodesNames() {
    ModelFacadePathBasicTest.oneTierSetupTwoServers();
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    // ^maximum path length should be overwritten to 1

    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(1, ModelFacadeConfig.MAX_PATH_LENGTH);
    final List<Path> allPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");

    // The reference nodes only have to be added one time
    List<Set<String>> nodeNames = new LinkedList<Set<String>>();

    for (int i = 0; i <= 1; i++) {
      nodeNames.add(Set.of("srv1", "sw"));
      nodeNames.add(Set.of("srv2", "sw"));
    }

    ModelFacadePathBasicTest.checkPathNodeNames(nodeNames, allPaths);
  }

  @Test
  public void testNoPathsLowerLimit() {
    // Setup for this test
    ModelFacadeConfig.MIN_PATH_LENGTH = 10;
    ModelFacadePathBasicTest.oneTierSetupFourServers();
    ModelFacade.getInstance().createAllPathsForNetwork("net");

    final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertTrue(generatedPaths.isEmpty());
  }

  @Test
  public void testPathsUpperLimit() {
    // Setup for this test
    ModelFacadeConfig.MAX_PATH_LENGTH = 0;
    // ^maximum path length should be overwritten to 1

    ModelFacadePathBasicTest.oneTierSetupFourServers();
    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(1, ModelFacadeConfig.MAX_PATH_LENGTH);

    final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(generatedPaths.isEmpty());
  }

  @Test
  public void testOnlyPathsWithTwoHops() {
    // Setup for this test
    ModelFacadeConfig.MIN_PATH_LENGTH = 2;
    ModelFacadeConfig.MAX_PATH_LENGTH = 2;
    // ^maximum path length should be kept at 2

    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(2, ModelFacadeConfig.MAX_PATH_LENGTH);

    final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(generatedPaths.isEmpty());

    for (final Path p : generatedPaths) {
      assertEquals(2, p.getHops());
      assertEquals(2, p.getLinks().size());
      assertEquals(3, p.getNodes().size());
    }
  }

  @Test
  public void testOnlyPathsWithThreeHops() {
    // Setup for this test
    ModelFacadeConfig.MIN_PATH_LENGTH = 3;
    ModelFacadeConfig.MAX_PATH_LENGTH = 3;
    // ^maximum path length should be overwritten to 2

    ModelFacadePathBasicTest.twoTierSetupFourServers();
    ModelFacade.getInstance().createAllPathsForNetwork("net");
    assertEquals(2, ModelFacadeConfig.MAX_PATH_LENGTH);

    final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertTrue(generatedPaths.isEmpty());
  }

  @Test
  public void testGoogleFatTree() {
    // Setup for this test
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 1;
    // ^maximum path length should be overwritten to 3

    final GoogleFatTreeConfig subConf = new GoogleFatTreeConfig(4);
    final GoogleFatTreeNetworkGenerator gen = new GoogleFatTreeNetworkGenerator(subConf);
    gen.createNetwork("net", false);

    assertEquals(3, ModelFacadeConfig.MAX_PATH_LENGTH);

    final List<Path> generatedPaths = ModelFacade.getInstance().getAllPathsOfNetwork("net");
    assertFalse(generatedPaths.isEmpty());

    // Check that every path has <= 3 hops
    for (final Path p : generatedPaths) {
      assertTrue(p.getHops() <= 3);
    }
  }

  /*
   * Negative tests
   */

  @Test
  public void testRejectServerDifferentDepth() {
    // Setup for this test
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 1;
    ModelFacade.getInstance().addNetworkToRoot("net", false);
    ModelFacade.getInstance().addSwitchToNetwork("sw", "net", 0);
    ModelFacade.getInstance().addServerToNetwork("1", "net", 1, 1, 1, 1);
    ModelFacade.getInstance().addServerToNetwork("2", "net", 1, 1, 1, 2);
    ModelFacade.getInstance().addLinkToNetwork("l1", "net", 1, "sw", "1");
    ModelFacade.getInstance().addLinkToNetwork("l2", "net", 1, "1", "sw");
    ModelFacade.getInstance().addLinkToNetwork("l3", "net", 1, "sw", "2");
    ModelFacade.getInstance().addLinkToNetwork("l4", "net", 1, "2", "sw");

    assertThrows(UnsupportedOperationException.class, () -> {
      ModelFacade.getInstance().createAllPathsForNetwork("net");
    });
  }

}
