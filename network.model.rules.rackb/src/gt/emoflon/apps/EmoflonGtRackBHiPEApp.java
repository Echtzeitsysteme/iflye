package gt.emoflon.apps;

import org.eclipse.emf.common.util.URI;

import gt.emoflon.EmoflonGtRackBAppUtils;
import model.Root;
import network.model.rules.rackb.api.RackbHiPEApp;

/**
 * Wrapper class for initializing the rack B rules HiPe App pattern matcher.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class EmoflonGtRackBHiPEApp extends RackbHiPEApp {

	/**
	 * Constructor that initializes the model resources for a given root node.
	 *
	 * @param root Root node to initialize model for.
	 */
	public EmoflonGtRackBHiPEApp(final Root root) {
		EmoflonGtRackBAppUtils.extractFiles(workspacePath);
		if (root.eResource() == null) {
			createModel(URI.createURI("model.xmi"));
			resourceSet.getResources().get(0).getContents().add(root);
		} else {
			resourceSet = root.eResource().getResourceSet();
		}
	}

}
