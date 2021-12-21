package ilp.wrapper;

public class Statistics {
	// TODO!

	private final SolverStatus status;
	private final long duration;

	public Statistics(final SolverStatus status, final long duration) {
		this.status = status;
		this.duration = duration;
	}

	public SolverStatus getStatus() {
		return status;
	}

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
