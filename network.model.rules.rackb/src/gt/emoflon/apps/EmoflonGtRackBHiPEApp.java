package gt.emoflon.apps;

import model.Root;
import network.model.rules.rackb.api.RackbHiPEApp;

/**
 * Wrapper class for initializing the rack B rules HiPe App pattern matcher.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class EmoflonGtRackBHiPEApp extends RackbHiPEApp {

  /**
   * Constructor that initializes the model resources for a given root node.
   * 
   * @param root Root node to initialize model for.
   */
  public EmoflonGtRackBHiPEApp(final Root root) {
    resourceSet = root.eResource().getResourceSet();
  }

}
