package test.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.ilp.VneIlpPathAlgorithm;
import facade.ModelFacade;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.SubstratePath;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Test class for the ModelFacade that checks the model validation functionality.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadeValidationTest {

  /**
   * Old objective value.
   */
  private Objective oldObjective;

  @BeforeEach
  public void resetModel() {
    oldObjective = AlgorithmConfig.obj;
    AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
    ModelFacade.getInstance().resetAll();
  }

  @AfterEach
  public void restoreConig() {
    AlgorithmConfig.obj = oldObjective;
  }

  @Test
  public void testValidateSubstrateNetworkOnly() {
    createSubstrateNetwork();
    ModelFacade.getInstance().validateModel();
  }

  @Test
  public void testValidateVirtualNetworkOnly() {
    createVirtualNetwork();
    ModelFacade.getInstance().validateModel();
  }

  @Test
  public void testValidateNoEmbeddedNetwork() {
    createSubstrateNetwork();
    createVirtualNetwork();

    ModelFacade.getInstance().validateModel();
  }

  @Test
  public void testValidateEmbeddedNetwork() {
    createSubstrateNetwork();
    createVirtualNetwork();
    embedVirtToSub();

    ModelFacade.getInstance().validateModel();
  }

  @Test
  public void testManipulatedSubstrateServerCpu() {
    createSubstrateNetwork();

    final SubstrateServer ssrv = (SubstrateServer) ModelFacade.getInstance().getRoot().getNetworks()
        .get(0).getNodes().get(1);
    ssrv.setResidualStorage(0);

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  @Test
  public void testManipulatedSubstrateServerMemory() {
    createSubstrateNetwork();

    final SubstrateServer ssrv = (SubstrateServer) ModelFacade.getInstance().getRoot().getNetworks()
        .get(0).getNodes().get(1);
    ssrv.setResidualMemory(0);

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  @Test
  public void testManipulatedSubstrateServerStorage() {
    createSubstrateNetwork();

    final SubstrateServer ssrv = (SubstrateServer) ModelFacade.getInstance().getRoot().getNetworks()
        .get(0).getNodes().get(1);
    ssrv.setResidualStorage(0);

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  @Test
  public void testManipulatesSubstratePath() {
    createSubstrateNetwork();

    final SubstratePath sp =
        ((SubstrateNetwork) ModelFacade.getInstance().getRoot().getNetworks().get(0)).getPaths()
            .get(0);
    sp.setResidualBandwidth(0);

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  @Test
  public void testRemovedGuestServer() {
    createSubstrateNetwork();
    createVirtualNetwork();
    embedVirtToSub();

    final SubstrateServer ssrv = (SubstrateServer) ModelFacade.getInstance().getRoot().getNetworks()
        .get(0).getNodes().get(1);
    ssrv.getGuestServers().clear();

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  @Test
  public void testRemovedGuestSwitch() {
    createSubstrateNetwork();
    createVirtualNetwork();
    embedVirtToSub();

    final SubstrateSwitch ssw = (SubstrateSwitch) ModelFacade.getInstance().getRoot().getNetworks()
        .get(0).getNodes().get(2);
    ssw.getGuestSwitches().clear();

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  @Test
  public void testRemovedGuestLink() {
    createSubstrateNetwork();
    createVirtualNetwork();
    embedVirtToSub();

    ModelFacade.getInstance().getRoot();

    for (int i = 0; i < 6; i++) {
      final SubstratePath sp =
          ((SubstrateNetwork) ModelFacade.getInstance().getRoot().getNetworks().get(0)).getPaths()
              .get(i);
      sp.getGuestLinks().clear();
    }

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  @Test
  public void testRemovedHostServer() {
    createSubstrateNetwork();
    createVirtualNetwork();
    embedVirtToSub();

    final VirtualServer vsrv =
        (VirtualServer) ModelFacade.getInstance().getRoot().getNetworks().get(1).getNodes().get(1);
    vsrv.setHost(null);

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  @Test
  public void testRemovedHostSwitch() {
    createSubstrateNetwork();
    createVirtualNetwork();
    embedVirtToSub();

    final VirtualSwitch vsw =
        (VirtualSwitch) ModelFacade.getInstance().getRoot().getNetworks().get(1).getNodes().get(2);
    vsw.setHost(null);

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  @Test
  public void testRemovedHostLink() {
    createSubstrateNetwork();
    createVirtualNetwork();
    embedVirtToSub();

    for (int i = 0; i < 3; i++) {
      final VirtualLink vl =
          (VirtualLink) ModelFacade.getInstance().getRoot().getNetworks().get(1).getLinks().get(i);
      vl.setHost(null);
    }

    assertThrows(InternalError.class, () -> {
      ModelFacade.getInstance().validateModel();
    });
  }

  /*
   * Utility methods.
   */

  private void createSubstrateNetwork() {
    final OneTierConfig subConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(subConfig);
    gen.createNetwork("sub", false);
  }

  private void createVirtualNetwork() {
    final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator gen = new OneTierNetworkGenerator(virtConfig);
    gen.createNetwork("virt", true);
  }

  private void embedVirtToSub() {
    final AbstractAlgorithm algo =
        new VneIlpPathAlgorithm((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub"),
            Set.of((VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt")));
    assertTrue(algo.execute());
  }

}
