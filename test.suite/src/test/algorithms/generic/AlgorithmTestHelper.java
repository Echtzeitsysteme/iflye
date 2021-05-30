package test.algorithms.generic;

import facade.ModelFacade;

/**
 * Generic algorithm test helper implementation for the testing of all algorithm implementations.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class AlgorithmTestHelper {

  /**
   * ModelFacade instance.
   */
  private static ModelFacade facade = ModelFacade.getInstance();

  /*
   * Utility methods.
   */

  /**
   * Creates a one tier network with two servers and one switch.
   * 
   * @param networkId Network id.
   * @param slotsPerServer Number of CPU, memory and storage resources.
   */
  public static void oneTierSetupTwoServers(final String networkId, final int slotsPerServer) {
    facade.addSwitchToNetwork(networkId + "_sw", networkId, 0);
    facade.addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer, slotsPerServer,
        slotsPerServer, 1);
    facade.addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer, slotsPerServer,
        slotsPerServer, 1);
    facade.addLinkToNetwork(networkId + "_ln1", networkId, 1, networkId + "_srv1",
        networkId + "_sw");
    facade.addLinkToNetwork(networkId + "_ln2", networkId, 1, networkId + "_srv2",
        networkId + "_sw");
    facade.addLinkToNetwork(networkId + "_ln3", networkId, 1, networkId + "_sw",
        networkId + "_srv1");
    facade.addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_sw",
        networkId + "_srv2");
  }

  /**
   * Creates a one tier network with three servers and one switch.
   * 
   * @param networkId Network id.
   * @param slotsPerServer Number of CPU, memory and storage resources.
   */
  public static void oneTierSetupThreeServers(final String networkId, final int slotsPerServer) {
    facade.addSwitchToNetwork(networkId + "_sw", networkId, 0);
    facade.addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer, slotsPerServer,
        slotsPerServer, 1);
    facade.addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer, slotsPerServer,
        slotsPerServer, 1);
    facade.addServerToNetwork(networkId + "_srv3", networkId, slotsPerServer, slotsPerServer,
        slotsPerServer, 1);
    facade.addLinkToNetwork(networkId + "_ln1", networkId, 1, networkId + "_srv1",
        networkId + "_sw");
    facade.addLinkToNetwork(networkId + "_ln2", networkId, 1, networkId + "_srv2",
        networkId + "_sw");
    facade.addLinkToNetwork(networkId + "_ln3", networkId, 1, networkId + "_srv3",
        networkId + "_sw");
    facade.addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_sw",
        networkId + "_srv1");
    facade.addLinkToNetwork(networkId + "_ln5", networkId, 1, networkId + "_sw",
        networkId + "_srv2");
    facade.addLinkToNetwork(networkId + "_ln6", networkId, 1, networkId + "_sw",
        networkId + "_srv3");
  }

  /**
   * Creates a two tier network with four servers total, two rack switches, and one core switch.
   * 
   * @param networkId Network id.
   * @param slotsPerServer Number of CPU, memory and storage resources.
   */
  public static void twoTierSetupFourServers(final String networkId, final int slotsPerServer) {
    facade.addSwitchToNetwork(networkId + "_csw1", networkId, 0);
    facade.addSwitchToNetwork(networkId + "_rsw1", networkId, 1);
    facade.addSwitchToNetwork(networkId + "_rsw2", networkId, 1);

    facade.addServerToNetwork(networkId + "_srv1", networkId, slotsPerServer, slotsPerServer,
        slotsPerServer, 2);
    facade.addServerToNetwork(networkId + "_srv2", networkId, slotsPerServer, slotsPerServer,
        slotsPerServer, 2);
    facade.addServerToNetwork(networkId + "_srv3", networkId, slotsPerServer, slotsPerServer,
        slotsPerServer, 2);
    facade.addServerToNetwork(networkId + "_srv4", networkId, slotsPerServer, slotsPerServer,
        slotsPerServer, 2);

    facade.addLinkToNetwork(networkId + "_ln1", networkId, 1, networkId + "_srv1",
        networkId + "_rsw1");
    facade.addLinkToNetwork(networkId + "_ln2", networkId, 1, networkId + "_srv2",
        networkId + "_rsw1");
    facade.addLinkToNetwork(networkId + "_ln3", networkId, 1, networkId + "_rsw1",
        networkId + "_srv1");
    facade.addLinkToNetwork(networkId + "_ln4", networkId, 1, networkId + "_rsw1",
        networkId + "_srv2");
    facade.addLinkToNetwork(networkId + "_ln5", networkId, 1, networkId + "_srv3",
        networkId + "_rsw2");
    facade.addLinkToNetwork(networkId + "_ln6", networkId, 1, networkId + "_srv4",
        networkId + "_rsw2");
    facade.addLinkToNetwork(networkId + "_ln7", networkId, 1, networkId + "_rsw2",
        networkId + "_srv3");
    facade.addLinkToNetwork(networkId + "_ln8", networkId, 1, networkId + "_rsw2",
        networkId + "_srv4");

    facade.addLinkToNetwork(networkId + "_ln9", networkId, 10, networkId + "_rsw1",
        networkId + "_csw1");
    facade.addLinkToNetwork(networkId + "_ln10", networkId, 10, networkId + "_rsw2",
        networkId + "_csw1");
    facade.addLinkToNetwork(networkId + "_ln11", networkId, 10, networkId + "_csw1",
        networkId + "_rsw1");
    facade.addLinkToNetwork(networkId + "_ln12", networkId, 10, networkId + "_csw1",
        networkId + "_rsw2");
  }

}
