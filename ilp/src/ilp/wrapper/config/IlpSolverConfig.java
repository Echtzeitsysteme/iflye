package ilp.wrapper.config;

import org.cardygan.ilp.api.solver.CplexSolver;
import org.cardygan.ilp.api.solver.GurobiSolver;
import org.cardygan.ilp.api.solver.GurobiSolver.GurobiSolverBuilder;

public class IlpSolverConfig {

  /**
   * Private constructor ensures no instantiation of this class.
   */
  private IlpSolverConfig() {}

  /**
   * If true, enables the output of the ILP solvers.
   */
  public static final boolean ENABLE_ILP_OUTPUT = true;

  /**
   * Configuration for the ILP solver to use.
   */
  public static final Solver solver = Solver.GUROBI;

  /**
   * Timeout for the ILP solver.
   */
  public static int TIME_OUT = Integer.MAX_VALUE;

  /**
   * Random seed for the ILP solver.
   */
  public static int RANDOM_SEED = 0;

  /**
   * Optimality tolerance for the ILP implementation part of the PM algorithm. This value is the
   * default value of the Gurobi solver (1e-6) taken from
   * https://www.gurobi.com/documentation/9.1/refman/optimalitytol.html#parameter:OptimalityTol.
   */
  public static double OPT_TOL = 0.000_001;

  /**
   * Factor to scale the objective functions for the ILP solver.
   */
  public static double OBJ_SCALE = 0.001;

  /**
   * Returns a new instance of the configured solver.
   * 
   * @return New instance of the configured solver.
   */
  public static org.cardygan.ilp.api.solver.Solver getSolver() {
    switch (IlpSolverConfig.solver) {
      case GUROBI:
        final GurobiSolverBuilder builder = GurobiSolver.create();
        return builder.withLogging(ENABLE_ILP_OUTPUT).withTimeOut(TIME_OUT).withSeed(RANDOM_SEED)
            .build();
      case CPLEX:
        return new CplexSolver();
    }
    throw new UnsupportedOperationException();
  }

}
