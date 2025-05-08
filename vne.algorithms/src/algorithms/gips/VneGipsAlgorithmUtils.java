package algorithms.gips;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import facade.ModelFacade;
import model.Network;
import model.VirtualNetwork;

public class VneGipsAlgorithmUtils {

	/**
	 * Checks if the given set of virtual networks exactly match the non-embedded
	 * virtual networks within the model.
	 * 
	 * @param vNets Set of virtual networks to check for.
	 */
	protected static void checkGivenVnets(final ModelFacade modelFacade, final Set<VirtualNetwork> vNets) {
		if (vNets == null) {
			throw new IllegalArgumentException("Virtual network set was null.");
		}

		final Collection<Network> modelNets = modelFacade.getAllNetworks();
		final Set<VirtualNetwork> modelVnets = new HashSet<VirtualNetwork>();
		for (final Network n : modelNets) {
			if (n instanceof VirtualNetwork vnet) {
				if (vnet.getHost() == null && vnet.getHostServer() == null) {
					modelVnets.add(vnet);
				}
			}
		}

		if (vNets.size() != modelVnets.size()) {
			throw new IllegalStateException(
					"Number of given virtual networks (" + vNets.size() + ") does not match the number of "
							+ "non-embedded virtual networks (" + modelVnets.size() + ") existing in the model.");
		}

		if (!vNets.containsAll(modelVnets) || !modelVnets.containsAll(vNets)) {
			throw new IllegalStateException("Given set of virtual network does not match the set of non-embedded"
					+ " virtual networks existing in the model.");
		}
	}

}
