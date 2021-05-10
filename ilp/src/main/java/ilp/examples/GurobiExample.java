package ilp.examples;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

/**
 * Simple example to solve an optimization problem with the Gurobi solver [1]. The example is taken
 * from Gurobi's quickstart guide [2]. This example exists to check the correct installation,
 * licensing and configuration of the Gurobi solver.
 * 
 * [1] https://www.gurobi.com/
 * 
 * [2] https://www.gurobi.com/documentation/9.1/quickstart_mac/java_example_mip1_java.html
 * 
 * @author Gurobi Optimization, LLC
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class GurobiExample {

  /**
   * Main method to start the example. String arguments will be ignored.
   * 
   * @param args Ignored string arguments.
   */
  public static void main(final String[] args) {
    try {
      // Create empty environment, set options, and start
      final GRBEnv env = new GRBEnv(true);
      env.set("logFile", "mip1.log");
      env.start();

      // Create empty model
      final GRBModel model = new GRBModel(env);

      // Create variables
      final GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x");
      final GRBVar y = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y");
      final GRBVar z = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z");

      // Set objective: maximize x + y + 2 z
      GRBLinExpr expr = new GRBLinExpr();
      expr.addTerm(1.0, x);
      expr.addTerm(1.0, y);
      expr.addTerm(2.0, z);
      model.setObjective(expr, GRB.MAXIMIZE);

      // Add constraint: x + 2 y + 3 z <= 4
      expr = new GRBLinExpr();
      expr.addTerm(1.0, x);
      expr.addTerm(2.0, y);
      expr.addTerm(3.0, z);
      model.addConstr(expr, GRB.LESS_EQUAL, 4.0, "c0");

      // Add constraint: x + y >= 1
      expr = new GRBLinExpr();
      expr.addTerm(1.0, x);
      expr.addTerm(1.0, y);
      model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, "c1");

      // Optimize model
      model.optimize();

      System.out.println(x.get(GRB.StringAttr.VarName) + " " + x.get(GRB.DoubleAttr.X));
      System.out.println(y.get(GRB.StringAttr.VarName) + " " + y.get(GRB.DoubleAttr.X));
      System.out.println(z.get(GRB.StringAttr.VarName) + " " + z.get(GRB.DoubleAttr.X));

      System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

      // Dispose of model and environment
      model.dispose();
      env.dispose();

    } catch (final GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
    }
  }

}
