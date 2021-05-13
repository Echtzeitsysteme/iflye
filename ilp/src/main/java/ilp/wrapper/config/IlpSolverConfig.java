package ilp.wrapper.config;

import org.cardygan.ilp.api.solver.CplexSolver;
import org.cardygan.ilp.api.solver.GurobiSolver;

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
   * Returns a new instance of the configured solver.
   * 
   * @return New instance of the configured solver.
   */
  public static org.cardygan.ilp.api.solver.Solver getSolver() {
    switch (IlpSolverConfig.solver) {
      case GUROBI:
        return new GurobiSolver();
      case CPLEX:
        return new CplexSolver();
    }
    throw new UnsupportedOperationException();
  }

}
