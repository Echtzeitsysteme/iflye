package test.algorithms.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import algorithms.AlgorithmConfig.Objective;
import algorithms.pm.VnePmMdvneAlgorithm;
import facade.ModelFacade;
import generators.OneTierNetworkGenerator;
import generators.config.OneTierConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;
import test.algorithms.generic.AAlgorithmTest;

/**
 * TODO.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class VnePmMdvneAlgorithmFullScenarioTest extends AAlgorithmTest {

  @Override
  public void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    AlgorithmConfig.obj = Objective.TOTAL_PATH_COST;
    algo = VnePmMdvneAlgorithm.prepare(sNet, vNets);
  }

  @AfterEach
  public void resetAlgo() {
    if (algo != null) {
      ((VnePmMdvneAlgorithm) algo).dispose();
    }
  }

  @Test
  public void testOneTierFull() {
    // Substrate network = one tier network
    final OneTierConfig subConfig = new OneTierConfig(2, 1, false, 4, 4, 4, 10);
    final OneTierNetworkGenerator subGen = new OneTierNetworkGenerator(subConfig);
    subGen.createNetwork("sub", false);

    // Virtual network = one tier network
    final OneTierConfig virtualConfig = new OneTierConfig(2, 1, false, 1, 1, 1, 1);
    final OneTierNetworkGenerator virtGen = new OneTierNetworkGenerator(virtualConfig);
    virtGen.createNetwork("virt", true);
    virtGen.createNetwork("virt2", true);

    SubstrateNetwork sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    VirtualNetwork vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt");
    AbstractAlgorithm algo = VnePmMdvneAlgorithm.prepare(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    sNet = (SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub");
    vNet = (VirtualNetwork) ModelFacade.getInstance().getNetworkById("virt2");
    algo = VnePmMdvneAlgorithm.prepare(sNet, Set.of(vNet));
    assertTrue(algo.execute());

    assertEquals(2, sNet.getGuests().size());
  }

}
