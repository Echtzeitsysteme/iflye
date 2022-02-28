package algorithms.heuristics;

import java.util.Set;

import algorithms.AbstractAlgorithm;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * Implementation of the TAF algorithm of the paper [1]. Please note:
 * <ul>
 * <li>The link bandwidths are not taken into account.</li>
 * <li>The mapping of a virtual link to a substrate link or path is not
 * specified by the TAF algorithm.</li>
 * </ul>
 *
 * Parts of this implementation are heavily inspired, taken or adapted from the
 * idyve project [2].
 *
 * [1] Zeng, D., Guo, S., Huang, H., Yu, S., and Leung, V. C.M., “Optimal VM
 * Placement in Data Centers with Architectural and Resource Constraints,”
 * International Journal of Autonomous and Adaptive Communications Systems, vol.
 * 8, no. 4, pp. 392–406, 2015.
 *
 * [2] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in
 * Rechenzentren, http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI
 * 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class TafAlgorithm extends AbstractAlgorithm {

	public TafAlgorithm(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets) {
		super(sNet, vNets);
	}

	@Override
	public boolean execute() {
		throw new UnsupportedOperationException("TAF algorithm without paths does not make any sense.");
	}

}
