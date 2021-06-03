package ilp.wrapper.config;

import org.cardygan.ilp.api.solver.CplexSolver;
import org.cardygan.ilp.api.solver.GurobiSolver;
import org.cardygan.ilp.api.solver.GurobiSolver.GurobiSolverBuilder;

public interface IlpSolverConfig {

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
  public static final int TIME_OUT = Integer.MAX_VALUE;

  /**
   * Random seed for the ILP solver.
   */
  public static final int RANDOM_SEED = 0;

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
