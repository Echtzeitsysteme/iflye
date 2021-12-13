package gt.emoflon.apps;

import org.eclipse.emf.common.util.URI;

import gt.emoflon.EmoflonGtAppUtils;
import model.Root;
import network.model.rules.api.RulesHiPEApp;

/**
 * Wrapper class for initializing the Rules HiPe App pattern matcher.
 *
 * Parts of this implementation are heavily inspired, taken or adapted from the
 * idyve project [1].
 *
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in
 * Rechenzentren, http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI
 * 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class EmoflonGtHiPEApp extends RulesHiPEApp {

	/**
	 * Constructor that initializes the model resources for a given root node.
	 *
	 * @param root Root node to initialize model for.
	 */
	public EmoflonGtHiPEApp(final Root root) {
		super(EmoflonGtAppUtils.createTempDir().normalize().toString() + "/");
		EmoflonGtAppUtils.extractFiles(workspacePath);
		if (root.eResource() == null) {
			createModel(URI.createURI("model.xmi"));
			resourceSet.getResources().get(0).getContents().add(root);
		} else {
			resourceSet = root.eResource().getResourceSet();
		}
	}

}
