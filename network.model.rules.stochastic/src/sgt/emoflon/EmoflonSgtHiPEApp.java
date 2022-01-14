package sgt.emoflon;

import org.eclipse.emf.common.util.URI;

import model.Root;
import network.model.rules.stochastic.api.StochasticHiPEApp;

public class EmoflonSgtHiPEApp extends StochasticHiPEApp {

	public EmoflonSgtHiPEApp(final Root root) {
		super(EmoflonSgtAppUtils.createTempDir().normalize().toString() + "/");
		EmoflonSgtAppUtils.extractFiles(workspacePath);
		if (root.eResource() == null) {
			createModel(URI.createURI("model.xmi"));
			resourceSet.getResources().get(0).getContents().add(root);
		} else {
			resourceSet = root.eResource().getResourceSet();
		}
	}
}
