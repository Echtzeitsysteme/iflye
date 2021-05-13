package algorithms.heuristics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.Before;
import org.junit.Test;
import algorithms.generic.AAlgorithmTest;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.GoogleFatTreeNetworkGenerator;
import generators.OneTierNetworkGenerator;
import generators.config.GoogleFatTreeConfig;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the TAF algorithm implementation using GoogleFatTree based substrate networks.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TafAlgorithmGoogleFatTreeTest extends AAlgorithmTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final VirtualNetwork vNet) {
    algo = new TafAlgorithm(sNet, vNet);
  }

  @Before
  public void localSetup() {
    ModelFacadeConfig.IGNORE_BW = true;
  }

  /*
   * Positive tests
   */

  @Test
  public void testOneSmallVirtualNetwork() {
    final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
    virtGen.createNetwork("virt", true);

    final GoogleFatTreeConfig subConfig = new GoogleFatTreeConfig(4);
    final GoogleFatTreeNetworkGenerator subGen = new GoogleFatTreeNetworkGenerator(subConfig);
    subGen.createNetwork("sub", false);

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    initAlgo(sNet, vNet);
    assertTrue(algo.execute());
  }

  @Test
  public void testHalfLoadSmallVirtualNetworks() {
    final GoogleFatTreeConfig subConfig = new GoogleFatTreeConfig(4);
    final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
    subConfig.setRack(subRackConfig);
    final GoogleFatTreeNetworkGenerator subGen = new GoogleFatTreeNetworkGenerator(subConfig);
    subGen.createNetwork("sub", false);

    // k = 4 -> 16 substrate servers
    for (int i = 0; i < 8; i++) {
      final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
      final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
      virtGen.createNetwork("virt" + i, true);

      final SubstrateNetwork sNet =
          (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
      final VirtualNetwork vNet =
          (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt" + i);

      initAlgo(sNet, vNet);
      assertTrue(algo.execute());
    }
  }

  @Test
  public void testFullLoadSmallVirtualNetworks() {
    final GoogleFatTreeConfig subConfig = new GoogleFatTreeConfig(4);
    final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
    subConfig.setRack(subRackConfig);
    final GoogleFatTreeNetworkGenerator subGen = new GoogleFatTreeNetworkGenerator(subConfig);
    subGen.createNetwork("sub", false);

    // k = 4 -> 16 substrate servers
    for (int i = 0; i < 16; i++) {
      final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
      final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
      virtGen.createNetwork("virt" + i, true);

      final SubstrateNetwork sNet =
          (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
      final VirtualNetwork vNet =
          (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt" + i);

      initAlgo(sNet, vNet);
      assertTrue(algo.execute());
    }
  }

  @Test
  public void testHalfLoadLargeVirtualNetwork() {
    final GoogleFatTreeConfig subConfig = new GoogleFatTreeConfig(4);
    final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
    subConfig.setRack(subRackConfig);
    final GoogleFatTreeNetworkGenerator subGen = new GoogleFatTreeNetworkGenerator(subConfig);
    subGen.createNetwork("sub", false);

    final OneTierConfig virtConfig = new OneTierConfig(8, 1, false, 2, 2, 2, 2);
    final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
    virtGen.createNetwork("virt", true);

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    initAlgo(sNet, vNet);
    assertTrue(algo.execute());
  }

  @Test
  public void testFullLoadLargeVirtualNetworkA() {
    final GoogleFatTreeConfig subConfig = new GoogleFatTreeConfig(4);
    final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
    subConfig.setRack(subRackConfig);
    final GoogleFatTreeNetworkGenerator subGen = new GoogleFatTreeNetworkGenerator(subConfig);
    subGen.createNetwork("sub", false);

    final OneTierConfig virtConfig = new OneTierConfig(16, 1, false, 2, 2, 2, 2);
    final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
    virtGen.createNetwork("virt", true);

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    initAlgo(sNet, vNet);
    assertTrue(algo.execute());
  }

  @Test
  public void testFullLoadLargeVirtualNetworkB() {
    final GoogleFatTreeConfig subConfig = new GoogleFatTreeConfig(4);
    final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 10, 10, 10, 100);
    subConfig.setRack(subRackConfig);
    final GoogleFatTreeNetworkGenerator subGen = new GoogleFatTreeNetworkGenerator(subConfig);
    subGen.createNetwork("sub", false);

    final OneTierConfig virtConfig = new OneTierConfig(80, 1, false, 2, 2, 2, 2);
    final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
    virtGen.createNetwork("virt", true);

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    initAlgo(sNet, vNet);
    assertTrue(algo.execute());
  }

  /*
   * Negative tests
   */

  @Test
  public void testFullReject() {
    final GoogleFatTreeConfig subConfig = new GoogleFatTreeConfig(4);
    final OneTierConfig subRackConfig = new OneTierConfig(-1, -1, false, 2, 2, 2, 100);
    subConfig.setRack(subRackConfig);
    final GoogleFatTreeNetworkGenerator subGen = new GoogleFatTreeNetworkGenerator(subConfig);
    subGen.createNetwork("sub", false);

    // k = 4 -> 16 substrate servers, but we want to request one more embedding
    for (int i = 0; i < 17; i++) {
      final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
      final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtConfig);
      virtGen.createNetwork("virt" + i, true);

      final SubstrateNetwork sNet =
          (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
      final VirtualNetwork vNet =
          (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt" + i);

      initAlgo(sNet, vNet);

      if (i < 16) {
        assertTrue(algo.execute());
      } else {
        assertFalse(algo.execute());
      }
    }
  }

}
