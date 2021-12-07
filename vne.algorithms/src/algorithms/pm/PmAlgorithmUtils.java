package algorithms.pm;

import java.util.Set;
import java.util.function.Consumer;

import facade.ModelFacade;
import model.Link;
import model.Node;
import model.SubstrateNetwork;
import model.SubstratePath;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Utility class for all VNE PM MdVNE algorithms.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class PmAlgorithmUtils {

	/**
	 * Private constructor ensures no instantiation of this class.
	 */
	private PmAlgorithmUtils() {
	}

	/**
	 * Method that removed the embedding for all given networks if there exists one.
	 * Moreover, this method "repairs" the possible floating state if the virtual
	 * network itself is not embedded, but its elements are.
	 * 
	 * @param sNet  Substrate network.
	 * @param vNets Set of virtual networks to remove embeddings for.
	 */
	public static void unembedAll(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		// Iterate over all given virtual networks
		for (final VirtualNetwork vNet : vNets) {
			// If virtual network has no host, but one of the nodes is embedded -> Embed the
			// whole virtual
			// network object again (otherwise the removal of the virtual network fails)
			if (vNet.getHost() == null) {
				final Node n = vNet.getNodes().get(0);

				if (n instanceof VirtualSwitch) {
					if (((VirtualSwitch) n).getHost() != null) {
						ModelFacade.getInstance().embedNetworkToNetwork(sNet.getName(), vNet.getName());
					}
				} else if (n instanceof VirtualServer) {
					if (((VirtualServer) n).getHost() != null) {
						ModelFacade.getInstance().embedNetworkToNetwork(sNet.getName(), vNet.getName());
					}
				}
			}

			// Remove embedding of whole virtual network with all of its elements
			if (vNet.getHost() != null) {
				ModelFacade.getInstance().removeNetworkEmbedding(vNet.getName());
			}
		}
	}

	public static void forEachLink(final SubstratePath sPath, final Consumer<? super Link> operation) {
		sPath.getLinks().stream().forEach(operation);
	}

}
