package algorithms;

import java.util.Set;

import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * This interface defines the basic structure and interaction of an algorithm.
 *
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public interface Algorithm {

	/**
	 * Execution method that starts the algorithm itself.
	 *
	 * @return True if embedding process was successful.
	 */
	public abstract boolean execute();

	/**
	 * Prepare the algorithm for execution
	 * 
	 * @param sNet  Substrate network to work with.
	 * @param vNets A set of virtual networks to work with.
	 */
	public void prepare(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNets);

	/**
	 * Dispose and terminate used resources.
	 */
	public abstract void dispose();

}
