package ilp.solver.examples;

import ilog.concert.IloException;
import ilog.concert.IloMPModeler;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

/**
 * Simple example to solve an optimization problem with the CPLEX solver [1].
 * The example is taken from IBM's documentation [2,3]. This example exists to
 * check the correct installation, licensing and configuration of the CPLEX
 * solver.
 *
 * [1] https://www.ibm.com/products/ilog-cplex-optimization-studio
 *
 * [2] https://www.ibm.com/docs/en/icos/20.1.0?topic=java-example-lpex1java
 *
 * [3]
 * https://www.tu-chemnitz.de/mathematik/discrete/manuals/cplex/doc/getstart/html/Java_Start_113.html
 *
 * @author IBM Corporation
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class CplexExample {

	/**
	 * Main method to start the example. String arguments will be ignored.
	 *
	 * @param args Ignored string arguments.
	 */
	public static void main(final String[] args) {
		try {
			// Create the modeler/solver object
			final IloCplex cplex = new IloCplex();

			final IloNumVar[][] var = new IloNumVar[1][];
			final IloRange[][] rng = new IloRange[1][];

			// The created ranges and variables are returned as element 0 of arrays
			// var and rng.
			populateByRow(cplex, var, rng);

			// write model to file
			cplex.exportModel("lpex1.lp");

			// solve the model and display the solution if one was found
			if (cplex.solve()) {
				final double[] x = cplex.getValues(var[0]);
				final double[] dj = cplex.getReducedCosts(var[0]);
				final double[] pi = cplex.getDuals(rng[0]);
				final double[] slack = cplex.getSlacks(rng[0]);

				System.out.println("Solution status = " + cplex.getStatus());
				System.out.println("Solution value  = " + cplex.getObjValue());

				final int ncols = cplex.getNcols();
				for (int j = 0; j < ncols; ++j) {
					System.out.println("Column: " + j + " Value = " + x[j] + " Reduced cost = " + dj[j]);
				}

				final int nrows = cplex.getNrows();
				for (int i = 0; i < nrows; ++i) {
					System.out.println("Row   : " + i + " Slack = " + slack[i] + " Pi = " + pi[i]);
				}
			}
			cplex.end();
		} catch (final IloException e) {
			System.err.println("Concert exception '" + e + "' caught");
		}
	}

	// The following method populates the problem with data for the following
	// linear program:
	//
	// Maximize
	// x1 + 2 x2 + 3 x3
	// Subject To
	// - x1 + x2 + x3 <= 20
	// x1 - 3 x2 + x3 <= 30
	// Bounds
	// 0 <= x1 <= 40
	// End
	//
	// using the IloMPModeler API

	static void populateByRow(final IloMPModeler model, final IloNumVar[][] var, final IloRange[][] rng)
			throws IloException {
		final double[] lb = { 0.0, 0.0, 0.0 };
		final double[] ub = { 40.0, Double.MAX_VALUE, Double.MAX_VALUE };
		final IloNumVar[] x = model.numVarArray(3, lb, ub);
		var[0] = x;

		final double[] objvals = { 1.0, 2.0, 3.0 };
		model.addMaximize(model.scalProd(x, objvals));

		rng[0] = new IloRange[2];
		rng[0][0] = model.addLe(model.sum(model.prod(-1.0, x[0]), model.prod(1.0, x[1]), model.prod(1.0, x[2])), 20.0);
		rng[0][1] = model.addLe(model.sum(model.prod(1.0, x[0]), model.prod(-3.0, x[1]), model.prod(1.0, x[2])), 30.0);
	}

}
