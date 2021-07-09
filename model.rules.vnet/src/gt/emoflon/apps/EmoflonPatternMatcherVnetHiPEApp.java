package gt.emoflon.apps;

import model.Root;
import model.rules.vnet.api.VnetHiPEApp;

/**
 * Wrapper class for initializing the Vnet rules HiPe App pattern matcher.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class EmoflonPatternMatcherVnetHiPEApp extends VnetHiPEApp {

  /**
   * Constructor that initializes the model resources for a given root node.
   * 
   * @param root Root node to initialize model for.
   */
  public EmoflonPatternMatcherVnetHiPEApp(final Root root) {
    resourceSet = root.eResource().getResourceSet();
  }

}
