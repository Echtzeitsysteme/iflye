package metrics;

import org.junit.Before;
import facade.ModelFacade;

/**
 * Abstract metric test class.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public abstract class AMetricTest {

  /**
   * ModelFacade object to work with.
   */
  final ModelFacade facade = ModelFacade.getInstance();

  @Before
  public void resetModel() {
    ModelFacade.getInstance().resetAll();
  }

  /*
   * Utility methods
   */

  /**
   * Creates the substrate network to test on.
   */
  protected void createSubstrateNetwork() {
    facade.addNetworkToRoot("sub", false);
    facade.addServerToNetwork("ssrv1", "sub", 0, 0, 0, 0);
    facade.addServerToNetwork("ssrv2", "sub", 0, 0, 0, 0);
    facade.addSwitchToNetwork("ssw", "sub", 0);
    facade.addLinkToNetwork("sln1", "sub", 0, "ssw", "ssrv1");
    facade.addLinkToNetwork("sln2", "sub", 0, "ssw", "ssrv2");
    facade.addLinkToNetwork("sln3", "sub", 0, "ssrv1", "ssw");
    facade.addLinkToNetwork("sln4", "sub", 0, "ssrv2", "ssw");
  }

  /**
   * Creates the virtual network to test on.
   */
  protected void createVirtualNetwork() {
    facade.addNetworkToRoot("virt", true);
    facade.addSwitchToNetwork("vsw", "virt", 0);
    facade.addServerToNetwork("vsrv1", "virt", 0, 0, 0, 0);
    facade.addServerToNetwork("vsrv2", "virt", 0, 0, 0, 0);
    facade.addLinkToNetwork("vln1", "virt", 0, "vsw", "vsrv1");
    facade.addLinkToNetwork("vln2", "virt", 0, "vsw", "vsrv2");
    facade.addLinkToNetwork("vln3", "virt", 0, "vsrv1", "vsw");
    facade.addLinkToNetwork("vln4", "virt", 0, "vsrv2", "vsw");
  }

}
