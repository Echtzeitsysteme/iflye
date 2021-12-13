package gt.emoflon.apps;

import org.eclipse.emf.common.util.URI;

import gt.emoflon.EmoflonGtRackAAppUtils;
import model.Root;
import network.model.rules.racka.api.RackaHiPEApp;

/**
 * Wrapper class for initializing the rack A rules HiPe App pattern matcher.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class EmoflonGtRackAHiPEApp extends RackaHiPEApp {

	/**
	 * Constructor that initializes the model resources for a given root node.
	 *
	 * @param root Root node to initialize model for.
	 */
	public EmoflonGtRackAHiPEApp(final Root root) {
		EmoflonGtRackAAppUtils.extractFiles(workspacePath);
		if (root.eResource() == null) {
			createModel(URI.createURI("model.xmi"));
			resourceSet.getResources().get(0).getContents().add(root);
		} else {
			resourceSet = root.eResource().getResourceSet();
		}
	}

}
