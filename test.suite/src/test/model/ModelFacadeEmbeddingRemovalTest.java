package test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import algorithms.AbstractAlgorithm;
import algorithms.ilp.VneIlpPathAlgorithm;
import facade.ModelFacade;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Test class for the ModelFacade that tests some embedding removal tasks.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadeEmbeddingRemovalTest {

  @BeforeEach
  public void resetModel() {
    ModelFacade.getInstance().resetAll();

    // Network setup
    ModelFacade.getInstance().addNetworkToRoot("sub", false);
    ModelFacade.getInstance().addNetworkToRoot("virt", true);

    final OneTierConfig subConfig = new OneTierConfig(4, 1, false, 1, 1, 1, 10);
    OneTierNetworkGenerator gen = new OneTierNetworkGenerator(subConfig);
    gen.createNetwork("sub", false);

    final OneTierConfig virtConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    gen = new OneTierNetworkGenerator(virtConfig);
    gen.createNetwork("virt", true);
  }

  @Test
  public void testEmbedNetworkRemoval() {
    // No guests before embedding anything
    assertTrue(
        ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    final AbstractAlgorithm algo = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    assertFalse(
        ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());
    ModelFacade.getInstance().unembedVirtualNetwork(vNet);
    assertTrue(
        ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());
    ModelFacade.getInstance().validateModel();
  }

  @Test
  public void testEmbedServerRemoval() {
    // No guests before embedding anything
    assertTrue(
        ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub")).getGuests().isEmpty());

    final SubstrateNetwork sNet =
        (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    final VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");

    AbstractAlgorithm algo = new VneIlpPathAlgorithm(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    ModelFacade.getInstance().removeSubstrateServerFromNetwork("sub_srv_1");

    // We have to remove the embedding of the virtual network for the check to pass
    ModelFacade.getInstance().unembedVirtualNetwork(vNet);
    ModelFacade.getInstance().validateModel();
  }

}

