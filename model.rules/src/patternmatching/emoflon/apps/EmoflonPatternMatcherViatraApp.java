package patternmatching.emoflon.apps;

import org.eclipse.emf.common.util.URI;
import model.Root;
import patternmatching.emoflon.EmoflonPatternMatcherAppUtils;
import rules.api.RulesViatraApp;

/**
 * Wrapper class for initializing the Rules Viatra App pattern matcher.
 * 
 * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [1].
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 * 
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class EmoflonPatternMatcherViatraApp extends RulesViatraApp {

  /**
   * Constructor that initializes the model resources for a given root node.
   * 
   * @param root Root node to initialize model for.
   */
  public EmoflonPatternMatcherViatraApp(final Root root) {
    super(EmoflonPatternMatcherAppUtils.createTempDir().normalize().toString() + "/");
    EmoflonPatternMatcherAppUtils.extractFiles(workspacePath);
    if (root.eResource() == null) {
      createModel(URI.createURI("model.xmi"));
      resourceSet.getResources().get(0).getContents().add(root);
    } else {
      resourceSet = root.eResource().getResourceSet();
    }
  }

}
