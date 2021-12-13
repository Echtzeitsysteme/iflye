package ilp.solver.tests;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import ilp.solver.examples.GurobiExample;

/**
 * Tests the availability of the Gurobi ILP solver.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class GurobiIlpTest {

	/**
	 * This test is adapted from the Gurobi solver example {@link GurobiExample}.
	 */
	@Test
	public void testGurobiModelExecution() {
		try {
			// Create empty environment, set options, and start
			final GRBEnv env = new GRBEnv(true);
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

			// Dispose of model and environment
			model.dispose();
			env.dispose();
		} catch (final GRBException e) {
			fail("GRBException caught. Maybe your Gurobi installation is not properly set up.");
		}
	}

}
