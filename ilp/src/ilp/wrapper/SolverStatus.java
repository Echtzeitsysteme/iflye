package ilp.wrapper;

/**
 * Enumeration that defines all possible solver states.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public enum SolverStatus {
	UNBOUNDED, INF_OR_UNBD, INFEASIBLE, OPTIMAL, TIME_OUT;
}
