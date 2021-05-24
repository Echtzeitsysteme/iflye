package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.GoogleFatTreeNetworkGenerator;
import generators.config.GoogleFatTreeConfig;
import model.Node;
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
  public void testGoogleFatTreeAggrPlanePathLength1() {
    setExactPathLength(1);
    final List<Path> allPaths = createPlaneNetworkAndGetPaths();

    assertFalse(allPaths.isEmpty());
    assertEquals(8, allPaths.size());
  }

  @Test
  public void testGoogleFatTreeAggrPlanePathLength2() {
    setExactPathLength(2);
    ModelFacadeConfig.YEN_K = 3;
    final List<Path> allPaths = createPlaneNetworkAndGetPaths();

    assertFalse(allPaths.isEmpty());
    assertEquals(5 * 4, allPaths.size());
  }

  @Test
  public void testGoogleFatTreeAggrPlanePathLength3() {
    setExactPathLength(3);
    ModelFacadeConfig.YEN_K = 5;
    final List<Path> allPaths = createPlaneNetworkAndGetPaths();

    assertFalse(allPaths.isEmpty());
    assertEquals(2 * 4 * 2, allPaths.size());
  }

  @Test
  public void testGoogleFatTreeAggrPlanePathLength4() {
    setExactPathLength(4);
    ModelFacadeConfig.YEN_K = 5;
    final List<Path> allPaths = createPlaneNetworkAndGetPaths();

    assertFalse(allPaths.isEmpty());
    assertEquals(4 * 6 + 2 * 4, allPaths.size());
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
    ModelFacadeConfig.YEN_K = 10;
    setExactPathLength(3);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    // final List<String> paths = new ArrayList<String>();
    //
    // for (int i = 0; i < allPaths.size(); i++) {
    // // System.out.println(String.format("%03d", i + 1) + " " +
    // // allPaths.get(i).getSource().getName()
    // // + " -> " + allPaths.get(i).getTarget().getName());
    //
    // String nodes = "";
    // for (final Node n : allPaths.get(i).getNodes()) {
    // nodes += n.getName() + "-";
    // }
    //
    // paths.add(allPaths.get(i).getSource().getName() + " -> "
    // + allPaths.get(i).getTarget().getName() + ": " + nodes);
    // }
    //
    // Collections.sort(paths);
    // for (int i = 0; i < paths.size(); i++) {
    //
    // System.out.println(String.format("%03d", i + 1) + " " + paths.get(i));
    // }
    final List<String> pNames = new LinkedList<String>();

    for (final Path p : allPaths) {
      pNames.add(p.getName());
    }

    Collections.sort(pNames);
    pNames.forEach(System.out::println);


    assertFalse(allPaths.isEmpty());

    // Check total number of paths
    // assertEquals(calcNumberOfPathsRef(4, 3), allPaths.size());
    int ref = 0;
    ref += 4 * 16; // Csw_n * Srv_n
    ref += 8 * 2 * 2; // Esw_n * Srv_n_near * 2 (possibilities)
    ref += (16 * 4 + 16 * 2); // Srv_n * Csw_n + Srv_n * Esw_n_near
    assertEquals(ref, allPaths.size());
  }

  @Test
  public void testK4GoogleFatTreePathLength4() {
    ModelFacadeConfig.YEN_K = 3;
    setExactPathLength(4);
    final List<Path> allPaths = createNetworkAndGetPaths(4);

    final List<String> paths = new ArrayList<String>();

    for (int i = 0; i < allPaths.size(); i++) {
      // System.out.println(String.format("%03d", i + 1) + " " +
      // allPaths.get(i).getSource().getName()
      // + " -> " + allPaths.get(i).getTarget().getName());

      String nodes = "";
      for (final Node n : allPaths.get(i).getNodes()) {
        nodes += n.getName() + "-";
      }

      paths.add(allPaths.get(i).getSource().getName() + " -> "
          + allPaths.get(i).getTarget().getName() + ": " + nodes);
    }

    Collections.sort(paths);
    for (int i = 0; i < paths.size(); i++) {

      System.out.println(String.format("%03d", i + 1) + " " + paths.get(i));
    }


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

  private List<Path> createPlaneNetworkAndGetPaths() {
    ModelFacade.getInstance().addNetworkToRoot("net", false);
    ModelFacade.getInstance().addSwitchToNetwork("csw1", "net", 0);
    ModelFacade.getInstance().addSwitchToNetwork("csw2", "net", 0);
    ModelFacade.getInstance().addSwitchToNetwork("rsw1", "net", 1);
    ModelFacade.getInstance().addSwitchToNetwork("rsw2", "net", 1);

    ModelFacade.getInstance().addServerToNetwork("srv1", "net", 0, 0, 0, 2);
    ModelFacade.getInstance().addServerToNetwork("srv2", "net", 0, 0, 0, 2);
    ModelFacade.getInstance().addServerToNetwork("srv3", "net", 0, 0, 0, 2);
    ModelFacade.getInstance().addServerToNetwork("srv4", "net", 0, 0, 0, 2);

    ModelFacade.getInstance().addLinkToNetwork("ln1", "net", 0, "srv1", "rsw1");
    ModelFacade.getInstance().addLinkToNetwork("ln2", "net", 0, "srv2", "rsw1");
    // ModelFacade.getInstance().addLinkToNetwork("ln3", "net", 0, "srv3", "rsw1");
    // ModelFacade.getInstance().addLinkToNetwork("ln4", "net", 0, "srv4", "rsw1");

    // ModelFacade.getInstance().addLinkToNetwork("ln5", "net", 0, "srv1", "rsw2");
    // ModelFacade.getInstance().addLinkToNetwork("ln6", "net", 0, "srv2", "rsw2");
    ModelFacade.getInstance().addLinkToNetwork("ln7", "net", 0, "srv3", "rsw2");
    ModelFacade.getInstance().addLinkToNetwork("ln8", "net", 0, "srv4", "rsw2");

    ModelFacade.getInstance().addLinkToNetwork("ln9", "net", 0, "rsw1", "srv1");
    ModelFacade.getInstance().addLinkToNetwork("ln10", "net", 0, "rsw1", "srv2");
    // ModelFacade.getInstance().addLinkToNetwork("ln11", "net", 0, "rsw1", "srv3");
    // ModelFacade.getInstance().addLinkToNetwork("ln12", "net", 0, "rsw1", "srv4");

    // ModelFacade.getInstance().addLinkToNetwork("ln13", "net", 0, "rsw2", "srv1");
    // ModelFacade.getInstance().addLinkToNetwork("ln14", "net", 0, "rsw2", "srv2");
    ModelFacade.getInstance().addLinkToNetwork("ln15", "net", 0, "rsw2", "srv3");
    ModelFacade.getInstance().addLinkToNetwork("ln16", "net", 0, "rsw2", "srv4");

    ModelFacade.getInstance().addLinkToNetwork("ln17", "net", 0, "rsw1", "csw1");
    ModelFacade.getInstance().addLinkToNetwork("ln18", "net", 0, "rsw2", "csw1");
    ModelFacade.getInstance().addLinkToNetwork("ln19", "net", 0, "csw1", "rsw1");
    ModelFacade.getInstance().addLinkToNetwork("ln20", "net", 0, "csw1", "rsw2");

    ModelFacade.getInstance().addLinkToNetwork("ln21", "net", 0, "rsw1", "csw2");
    ModelFacade.getInstance().addLinkToNetwork("ln22", "net", 0, "rsw2", "csw2");
    ModelFacade.getInstance().addLinkToNetwork("ln23", "net", 0, "csw2", "rsw1");
    ModelFacade.getInstance().addLinkToNetwork("ln24", "net", 0, "csw2", "rsw2");

    ModelFacade.getInstance().createAllPathsForNetwork("net");

    return ModelFacade.getInstance().getAllPathsOfNetwork("net");
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
        counter = numberOfServers * 6;
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
