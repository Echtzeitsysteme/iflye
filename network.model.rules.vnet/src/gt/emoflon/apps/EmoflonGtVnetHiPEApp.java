package gt.emoflon.apps;

import org.eclipse.emf.common.util.URI;

import gt.emoflon.EmoflonGtVnetAppUtils;
import model.Root;
import network.model.rules.vnet.api.VnetHiPEApp;

/**
 * Wrapper class for initializing the Vnet rules HiPe App pattern matcher.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class EmoflonGtVnetHiPEApp extends VnetHiPEApp {

	/**
	 * Constructor that initializes the model resources for a given root node.
	 *
	 * @param root Root node to initialize model for.
	 */
	public EmoflonGtVnetHiPEApp(final Root root) {
		EmoflonGtVnetAppUtils.extractFiles(workspacePath);
		if (root.eResource() == null) {
			createModel(URI.createURI("model.xmi"));
			resourceSet.getResources().get(0).getContents().add(root);
		} else {
			resourceSet = root.eResource().getResourceSet();
		}
	}

}
