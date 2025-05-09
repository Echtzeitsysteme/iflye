package algorithms.gips;

import java.util.Map;

import org.emoflon.gips.core.milp.SolverOutput;
import org.emoflon.gips.core.util.IMeasurement;

import algorithms.Algorithm;

/**
 * This interface defines the additional methods of the GIPS algorithm.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public interface GipsAlgorithm extends Algorithm {

	/**
	 * Returns the solver output of the GIPS algorithm.
	 * 
	 * @return The solver output containing the results of the GIPS algorithm.
	 */
	public abstract SolverOutput getSolverOutput();

	/**
	 * Returns the mapping of virtual nodes to substrate nodes.
	 * 
	 * @return A map where the keys are the virtual node IDs and the values are the
	 *         substrate node IDs.
	 */
	public abstract Map<String, String> getMatches();

	public abstract Map<String, IMeasurement> getMeasurements();

}
