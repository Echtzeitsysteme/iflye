package test.ilp;

import static org.junit.Assert.assertTrue;

import org.emoflon.ilp.IntegerVariable;
import org.emoflon.ilp.LinearConstraint;
import org.emoflon.ilp.ObjectiveType;
import org.emoflon.ilp.Operator;
import org.emoflon.ilp.Problem;
import org.emoflon.ilp.Solver;
import org.emoflon.ilp.SolverConfig;
import org.emoflon.ilp.SolverHelper;
import org.emoflon.ilp.SolverOutput;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ilp.wrapper.config.IlpSolverConfig;
import ilp.wrapper.config.SolverType;

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
	public static SolverType savedSolver = null;

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
		IlpSolverConfig.solver = SolverType.GUROBI;
		genericIlpTest();
	}

	@Test
	public void testCplexSetup() {
		IlpSolverConfig.solver = SolverType.CPLEX;
		genericIlpTest();
	}

	@Test
	public void testGlpkSetup() {
		IlpSolverConfig.solver = SolverType.GLPK;
		genericIlpTest();
	}

	/**
	 * Tests a small and generic ILP problem for the ILP solver given by the
	 * {@link IlpSolverConfig}.
	 */
	private void genericIlpTest() {
		// Test solver instantiation
		final Problem ilp = new Problem();
		final IntegerVariable x = new IntegerVariable("x");
		final IntegerVariable y = new IntegerVariable("y");
		final LinearConstraint c0 = new LinearConstraint(Operator.LESS_OR_EQUAL, 5);
		c0.addTerm(x, -1);
		c0.addTerm(y, -1);
		final LinearConstraint c1 = new LinearConstraint(Operator.LESS_OR_EQUAL, 2);
		c1.addTerm(x, 2);
		final LinearConstraint c2 = new LinearConstraint(Operator.LESS_OR_EQUAL, 2);
		c2.addTerm(y, 2);

		ilp.add(c0);
		ilp.add(c1);
		ilp.add(c2);

		ilp.setObjective();
		ilp.setType(ObjectiveType.MIN);

		final SolverConfig config = new SolverConfig();
		switch (savedSolver) {
		case GUROBI:
			config.setSolver(org.emoflon.ilp.SolverConfig.SolverType.GUROBI);
			break;
		case CPLEX:
			config.setSolver(org.emoflon.ilp.SolverConfig.SolverType.CPLEX);
			break;
		case GLPK:
			config.setSolver(org.emoflon.ilp.SolverConfig.SolverType.GLPK);
			break;
		}

		final Solver solver = (new SolverHelper(config)).getSolver();
		solver.buildILPProblem(ilp);
		final SolverOutput out = solver.solve();
		assertTrue(out.getSolCount() > 0);
		solver.terminate();
	}

}
