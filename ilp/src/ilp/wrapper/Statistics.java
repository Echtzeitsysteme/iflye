package ilp.wrapper;

/**
 * Statistics class for the ILP solver implementations.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class Statistics {

	/**
	 * Status of the solver after running, e.g., OPTIMAL.
	 */
	private final SolverStatus status;

	/**
	 * Runtime of the solver in nanoseconds.
	 */
	private final long duration;

	/**
	 * Creates a new statistics object with given parameters.
	 * 
	 * @param status   Solver status.
	 * @param duration Runtime in nanoseconds.
	 */
	public Statistics(final SolverStatus status, final long duration) {
		this.status = status;
		this.duration = duration;
	}

	/**
	 * Returns the status of the solver.
	 * 
	 * @return Status of the solver.
	 */
	public SolverStatus getStatus() {
		return status;
	}

	/**
	 * Returns the runtime of the solver in nanoseconds.
	 * 
	 * @return Runtime of the solver in nanoseconds.
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Returns true if the statistics object was feasible.
	 *
	 * @return True if statistics object was feasible.
	 */
	public boolean isFeasible() {
		if (status == ilp.wrapper.SolverStatus.INF_OR_UNBD || status == ilp.wrapper.SolverStatus.INFEASIBLE) {
			return false;
		}
		return true;
	}

}
