package algorithms.generic;

import org.junit.After;
import org.junit.Before;
import algorithms.AbstractAlgorithm;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Abstract test class for the algorithm implementations.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public abstract class AAlgorithmTest {

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

    // Network setup
    ModelFacade.getInstance().addNetworkToRoot("sub", false);
    ModelFacade.getInstance().addNetworkToRoot("virt", true);

    // Normal model setup
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
  }

  @After
  public void restoreConfig() {
    ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
    ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
  }

  /**
   * Initializes the algorithm to test.
   */
  public abstract void initAlgo(final SubstrateNetwork sNet, final VirtualNetwork vNet);

}
