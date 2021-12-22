package test.ilp;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ilp.wrapper.IncrementalIlpSolver;
import ilp.wrapper.SolverStatus;
import ilp.wrapper.Statistics;
import ilp.wrapper.config.IlpSolverConfig;
import ilp.wrapper.config.Solver;

/**
 * ILP solver setup test that ensures that all possible ILP solvers are properly
 * setup in the iflye workspace.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class IlpSolverSetupTest {

	/**
	 * Saves the currently configured ILP solver type.
	 */
	public static Solver savedSolver = null;

	@BeforeAll
	public static void saveConfig() {
		savedSolver = IlpSolverConfig.solver;
	}

	@AfterAll
	public static void restoreConfig() {
		IlpSolverConfig.solver = savedSolver;
	}

	@Test
	public void testGurobiSetup() {
		IlpSolverConfig.solver = Solver.GUROBI;
		genericIlpTest();
	}

	@Test
	public void testCplexSetup() {
		IlpSolverConfig.solver = Solver.CPLEX;
		genericIlpTest();
	}

	/**
	 * Tests a small and generic ILP problem for the ILP solver given by the
	 * {@link IlpSolverConfig}.
	 */
	private void genericIlpTest() {
		// Test solver instantiation
		IncrementalIlpSolver solver = null;
		try {
			solver = IlpSolverConfig.getIlpSolver();
		} catch (final Exception | Error ex) {
			System.out.println(ex.getMessage());
			Assert.fail("ILP solver could not be instantiated: Exception or error thrown.");
		}

		// Create a very small and simple ILP problem
		solver.addVariable("x", 1);
		solver.addVariable("y", 1);
		solver.addLessOrEqualsConstraint("c0", 5, new double[] { -1, -1 }, new String[] { "x", "y" });
		solver.addLessOrEqualsConstraint("c1", 2, new double[] { -1 }, new String[] { "x" });
		solver.addLessOrEqualsConstraint("c2", 2, new double[] { -1 }, new String[] { "y" });

		// Solve it
		final Statistics stats = solver.solve();
		assertTrue(stats.getStatus().equals(SolverStatus.OPTIMAL));
		solver.dispose();
	}

}
