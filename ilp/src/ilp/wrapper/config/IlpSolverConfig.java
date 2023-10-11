package ilp.wrapper.config;

/**
 * General configuration class for all ILP solvers.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class IlpSolverConfig {

	/**
	 * Private constructor ensures no instantiation of this class.
	 */
	private IlpSolverConfig() {
	}

	/**
	 * If true, enables the output of the ILP solvers.
	 */
	public static final boolean ENABLE_ILP_OUTPUT = true;
	
	/**
	 * Configuration for the ILP solver to use.
	 */
	public static SolverType solver = SolverType.GUROBI;

	/**
	 * Timeout for the ILP solver.
	 */
	public static int TIME_OUT = Integer.MAX_VALUE;

	/**
	 * Random seed for the ILP solver.
	 */
	public static int RANDOM_SEED = 0;

	/**
	 * If true, this enables presolve for all ILP solvers.
	 */
	public static boolean ENABLE_PRESOLVE = true;

	/**
	 * Optimality tolerance for the ILP implementation part of the PM algorithm.
	 * This value is the default value of the Gurobi solver (1e-6) taken from
	 * https://www.gurobi.com/documentation/9.1/refman/optimalitytol.html#parameter:OptimalityTol.
	 */
	public static double OPT_TOL = 0.000_001;

	/**
	 * Factor to scale the objective functions for the ILP solver.
	 */
	public static double OBJ_SCALE = 1;

	/**
	 * If true, the objective function will introduce a logarithm.
	 */
	public static boolean OBJ_LOG = false;

	/**
	 * Transforms the input value of the former objective function according to the
	 * settings for the ILP solver.
	 *
	 * @param input Input value of the former objective function.
	 * @return Transformed value according to the set parameters.
	 */
	public static double transformObj(final double input) {
		if (OBJ_LOG) {
			return Math.log10(input + 1) * OBJ_SCALE;
		} else {
			return input * OBJ_SCALE;
		}
	}

}
