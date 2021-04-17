package facade;

import java.util.Collection;
import java.util.stream.Collectors;

import model.ModelFactory;
import model.Network;
import model.Node;
import model.Root;
import model.Server;

public class ModelFacade {

	static final Root root = ModelFactory.eINSTANCE.createRoot();
	
	public Collection<Network> getAllNetworks() {
		return root.getNetworks();
	}
	
	public Collection<Node> getAllServersOfNetwork(final String networkId) {
		for (Network actNet : root.getNetworks()) {
			if (actNet.getName().equals(networkId)) {
				return actNet.getNodes().stream()
						.filter(n -> n instanceof Server)
						.collect(Collectors.toList());
			}
		}
		
		return null;
	}
}
