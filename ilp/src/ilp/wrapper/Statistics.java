package ilp.wrapper;

public class Statistics {
	// TODO!

	private final SolverStatus status;
	private final long duration;
	private final long presolveTime;
	private final int colsRemovedByPresolve;
	private final int rowsRemovedByPresolve;

	public Statistics(final SolverStatus status, final long duration, final long presolveTime,
			final int colsRemovedByPresolve, final int rowsRemovedByPresolve) {
		this.presolveTime = presolveTime;
		this.status = status;
		this.duration = duration;
		this.colsRemovedByPresolve = colsRemovedByPresolve;
		this.rowsRemovedByPresolve = rowsRemovedByPresolve;
	}

	public SolverStatus getStatus() {
		return status;
	}

	public long getDuration() {
		return duration;
	}

	public long getPresolveTime() {
		return presolveTime;
	}

	public int getColsRemovedByPresolve() {
		return colsRemovedByPresolve;
	}

	public int getRowsRemovedByPresolve() {
		return rowsRemovedByPresolve;
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
