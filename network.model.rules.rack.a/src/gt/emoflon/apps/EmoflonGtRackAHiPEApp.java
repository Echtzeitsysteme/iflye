package gt.emoflon.apps;

import model.Root;
import network.model.rules.rack.api.RackHiPEApp;

/**
 * Wrapper class for initializing the rack A rules HiPe App pattern matcher.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class EmoflonGtRackAHiPEApp extends RackHiPEApp {

  /**
   * Constructor that initializes the model resources for a given root node.
   * 
   * @param root Root node to initialize model for.
   */
  public EmoflonGtRackAHiPEApp(final Root root) {
    resourceSet = root.eResource().getResourceSet();
  }

}
