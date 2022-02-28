package algorithms.simple;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Super simple Virtual Network Embedding algorithm. It searches for the
 * substrate server with largest residual amount of resources and checks if the
 * whole virtual network could fit onto it. If it does not, the algorithm is
 * unable to embed the request. The resources are added all together.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class SimpleVne extends AbstractAlgorithm {

	/**
	 * Initializes a new object of this simple VNE algorithm.
	 *
	 * @param sNet  Substrate network to work with.
	 * @param vNets Set of virtual networks to work with.
	 */
	public SimpleVne(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);

		if (vNets.size() != 1) {
			throw new IllegalArgumentException(
					"The simple VNE algorithm is only suited for one virtual network at a time.");
		}
	}

	@Override
	public boolean execute() {
		throw new UnsupportedOperationException(
				"SimpleVne can not exist if embedding links/switches to servers is not possible");
	}

}
