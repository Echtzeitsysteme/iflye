package algorithms;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import facade.ModelFacade;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * An abstract algorithm class that acts as a common type for embedding algorithms.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public abstract class AbstractAlgorithm {

  /**
   * ModelFacade instance.
   */
  public static ModelFacade facade = ModelFacade.getInstance();

  /**
   * The substrate network (model).
   */
  protected SubstrateNetwork sNet;

  /**
   * The virtual networks (model).
   */
  protected Set<VirtualNetwork> vNets;

  /**
   * Execution method that starts the algorithm itself.
   * 
   * @return True if embedding process was successful.
   */
  public abstract boolean execute();

  /**
   * Initializes a new abstract algorithm with a given substrate and given virtual networks.
   *
   * @param sNet Substrate network to work with.
   * @param vNet A set of virtual networks to work with.
   */
  public AbstractAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
    if (sNet == null || vNets == null) {
      throw new IllegalArgumentException("One of the provided network objects was null!");
    }

    if (vNets.size() == 0) {
      throw new IllegalArgumentException("Provided set of virtual networks was empty.");
    }

    this.sNet = sNet;
    this.vNets = new HashSet<VirtualNetwork>();
    this.vNets.addAll(vNets);
  }

  /**
   * Returns the first virtual network from this super type.
   * 
   * @return First virtual network from this super type.
   */
  protected VirtualNetwork getFirstVnet() {
    final Iterator<VirtualNetwork> it = vNets.iterator();
    return it.next();
  }

}
