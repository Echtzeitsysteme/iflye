package algorithms.heuristics;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.Before;
import org.junit.Test;
import algorithms.generic.AAlgorithmTwoTierTest;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the TAF algorithm implementation.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TafAlgorithmTest extends AAlgorithmTwoTierTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final VirtualNetwork vNet) {
    this.algo = new TafAlgorithm(sNet, vNet);
  }

  @Before
  public void localSetup() {
    ModelFacadeConfig.IGNORE_BW = true;
  }

  /*
   * Additional negative tests.
   */

  @Test
  public void testRejectIgnoreBandwidth() {
    ModelFacadeConfig.IGNORE_BW = false;

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    assertThrows(UnsupportedOperationException.class, () -> {
      new TafAlgorithm(sNet, vNet);
    });
  }

  @Test
  public void testRejectSubstrateServerConnections() {
    ModelFacade.getInstance().addSwitchToNetwork("sw", "sub", 0);
    ModelFacade.getInstance().addServerToNetwork("srv1", "sub", 1, 1, 1, 1);
    ModelFacade.getInstance().addServerToNetwork("srv2", "sub", 1, 1, 1, 1);
    ModelFacade.getInstance().addLinkToNetwork("ln1", "sub", 1, "srv1", "sw");
    ModelFacade.getInstance().addLinkToNetwork("ln2", "sub", 1, "srv1", "srv2");

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    assertThrows(UnsupportedOperationException.class, () -> {
      new TafAlgorithm(sNet, vNet);
    });
  }

  @Test
  public void testRejectOneVirtualServer() {
    oneTierSetupTwoServers("sub", 1);

    ModelFacade.getInstance().addSwitchToNetwork("sw", "virt", 0);
    ModelFacade.getInstance().addServerToNetwork("srv1", "virt", 1, 1, 1, 1);
    ModelFacade.getInstance().addLinkToNetwork("ln1", "virt", 1, "srv1", "sw");
    ModelFacade.getInstance().addLinkToNetwork("ln2", "virt", 1, "sw", "srv1");

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    assertThrows(UnsupportedOperationException.class, () -> {
      new TafAlgorithm(sNet, vNet);
    });
  }

  @Test
  public void testRejectMinPathLength() {
    ModelFacadeConfig.MIN_PATH_LENGTH = 3;

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    assertThrows(UnsupportedOperationException.class, () -> {
      new TafAlgorithm(sNet, vNet);
    });
  }

  @Test
  public void testRejectNoSubPaths() {
    oneTierSetupTwoServers("virt", 2);
    oneTierSetupTwoServers("sub", 2);
    // Path generation removed intentionally

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    assertThrows(UnsupportedOperationException.class, () -> {
      new TafAlgorithm(sNet, vNet);
    });
  }

}
