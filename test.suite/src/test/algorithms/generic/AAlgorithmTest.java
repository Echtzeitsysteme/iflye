package test.algorithms.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import algorithms.AbstractAlgorithm;
import algorithms.AlgorithmConfig;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.Link;
import model.Node;
import model.SubstrateNetwork;
import model.SubstratePath;
import model.SubstrateServer;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Abstract test class for the algorithm implementations.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public abstract class AAlgorithmTest {

  /**
   * ModelFacade instance.
   */
  protected ModelFacade facade = ModelFacade.getInstance();

  /*
   * Variables to save the ModelFacade's configuration of path limits to.
   */

  /**
   * Algorithm to test.
   */
  protected AbstractAlgorithm algo;

  /**
   * Old lower limit value.
   */
  protected int oldLowerLimit;

  /**
   * Old upper limit value.
   */
  protected int oldUpperLimit;

  /**
   * Old bandwidth ignore value.
   */
  protected boolean oldIgnoreBw;

  /**
   * Old dynamic network rejection switch value.
   */
  protected boolean oldNetRejCost;

  @BeforeEach
  public void resetModel() {
    facade.resetAll();

    // Save old values
    oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
    oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
    oldIgnoreBw = ModelFacadeConfig.IGNORE_BW;
    oldNetRejCost = AlgorithmConfig.netRejCostDynamic;

    // Network setup
    facade.addNetworkToRoot("sub", false);
    facade.addNetworkToRoot("virt", true);

    // Normal model setup
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
  }

  @AfterEach
  public void restoreConfig() {
    facade.validateModel();
    ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
    ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
    ModelFacadeConfig.IGNORE_BW = oldIgnoreBw;
    AlgorithmConfig.netRejCostDynamic = oldNetRejCost;
  }

  /**
   * Initializes the algorithm to test.
   * 
   * @param sNet Substrate network.
   * @param vNets Set of virtual networks.
   */
  public abstract void initAlgo(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets);

  /**
   * Checks if all given virtual networks (and each of their elements) are embedded on the given
   * substrate network.
   * 
   * @param sNet Substrate network to check.
   * @param vNets Set of virtual networks to check.
   */
  protected void checkAllElementsEmbeddedOnSubstrateNetwork(final SubstrateNetwork sNet,
      final Set<VirtualNetwork> vNets) {
    final Iterator<VirtualNetwork> it = vNets.iterator();
    while (it.hasNext()) {
      final VirtualNetwork vNet = it.next();
      assertEquals(sNet, vNet.getHost());

      for (final Node n : facade.getAllServersOfNetwork(vNet.getName())) {
        assertEquals(sNet, ((VirtualServer) n).getHost().getNetwork());
      }

      for (final Node n : facade.getAllSwitchesOfNetwork(vNet.getName())) {
        assertEquals(sNet, ((VirtualSwitch) n).getHost().getNetwork());
      }

      for (final Link l : facade.getAllLinksOfNetwork(vNet.getName())) {
        final VirtualLink vl = (VirtualLink) l;
        if (vl.getHost() instanceof SubstratePath) {
          assertEquals(sNet, ((SubstratePath) vl.getHost()).getNetwork());
        } else if (vl.getHost() instanceof SubstrateServer) {
          assertEquals(sNet, ((SubstrateServer) vl.getHost()).getNetwork());
        } else {
          fail();
        }
      }
    }
  }

}
