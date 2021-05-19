package generators;

import facade.ModelFacade;

/**
 * A super type interface that acts as a common type for network generators.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public interface INetworkGenerator {

  /**
   * ModelFacade instance.
   */
  public static ModelFacade facade = ModelFacade.getInstance();

  /**
   * Creates the network with specified configuration and network ID. If isVirtual is true, the
   * generated network must be of type VirtualNetwork.
   * 
   * @param networkId Network ID for the network to generate.
   * @param isVirtual True if generated network must be virtual.
   */
  public void createNetwork(final String networkId, boolean isVirtual);

}
