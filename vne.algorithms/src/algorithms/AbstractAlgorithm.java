package algorithms;

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
  protected final SubstrateNetwork sNet;

  /**
   * The virtual network (model).
   */
  protected final VirtualNetwork vNet;

  /**
   * Execution method that starts the algorithm itself.
   * 
   * @return True if embedding process was successful.
   */
  public abstract boolean execute();

  /**
   * Initializes a new abstract algorithm with a given substrate and a given virtual network.
   * 
   * @param sNet Substrate network to work with.
   * @param vNet Virtual network to work with.
   */
  public AbstractAlgorithm(final SubstrateNetwork sNet, final VirtualNetwork vNet) {
    if (sNet == null || vNet == null) {
      throw new IllegalArgumentException("One of the provided network objects was null!");
    }

    this.sNet = sNet;
    this.vNet = vNet;
  }

}
