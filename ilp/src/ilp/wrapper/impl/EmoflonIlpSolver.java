package ilp.wrapper.impl;

import java.util.List;
import java.util.Map;

import org.emoflon.ilp.Problem;
import org.emoflon.ilp.RealVariable;
import org.emoflon.ilp.SOS1Constraint;
import org.emoflon.ilp.Solver;
import org.emoflon.ilp.SolverConfig;
import org.emoflon.ilp.SolverConfig.SolverType;
import org.emoflon.ilp.SolverHelper;

import ilp.wrapper.IlpSolverException;
import ilp.wrapper.IncrementalIlpSolver;
import ilp.wrapper.Statistics;

public class EmoflonIlpSolver implements IncrementalIlpSolver {

	private final Problem problem;
	private final SolverConfig config;

	public EmoflonIlpSolver() {
		problem = new Problem();
		config = new SolverConfig(SolverType.GUROBI, true, 3600, true, 0, false, -1, true, 0, 1, true, true, false,
				null);
	}

	private RealVariable v2v(final Variable v) {
		final RealVariable rv = new RealVariable(v.getName());
		return rv;
	}

	@Override
	public void addSosConstraint(final SosConstraint constraint) {
		// TODO: Set name
		final SOS1Constraint cnstr = new SOS1Constraint();
		for (final Variable v : constraint.getVars()) {
			cnstr.addVariable(v2v(v), 1);
		}
		problem.add(cnstr);
	}

	@Override
	public void addSosConstraints(SosConstraint[] constraints) {
		for (int i = 0; i < constraints.length; i++) {
			addSosConstraint(constraints[i]);
		}
	}

	@Override
	public void addEqualsConstraint(String name, double right) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addEqualsConstraint(String name, double right, double[] weights, String[] vars)
			throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addEqualsConstraints(Constraint[] constraints) throws IlpSolverException {
		// TODO Auto-generated method stub
		for (int i = 0; i < constraints.length; i++) {
			final Constraint c = constraints[i];
			c.getRight();
			c.getVarnames();
			c.getWeights();
		}
	}

	@Override
	public void addLessOrEqualsConstraint(String name, double right) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLessOrEqualsConstraint(String name, double right, double[] weights, String[] vars)
			throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLessOrEqualsConstraints(Constraint[] constraints) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addToVariableWeight(String name, double change) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addVariable(String name, double solutionWeight) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addVariables(Variable[] variables) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeVariableBounds(String name, int lower, int upper) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeVariableWeight(String name, double solutionWeight) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() throws IlpSolverException {
		// TODO Auto-generated method stub
	
	}

	@Override
	public int getConstraintCount() {
		return problem.getConstraintCount();
	}

	@Override
	public Map<String, Boolean> getMappings() throws IlpSolverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getObjectiveValue() throws IlpSolverException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getVariableCount() {
		return problem.getVariableCount();
	}

	@Override
	public boolean hasVariable(String name) throws IlpSolverException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSelected(String name) throws IlpSolverException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadModel(String path) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeConstraint(String name) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeConstraints(List<String> removeConstraints) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeVariable(String name) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeVariables(List<String> removeVariables) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void save(String file) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConstraintRight(String name, double newRight) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConstraintRights(Map<String, Double> changeConstraintRight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSeed(int seed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTimeout(int parameterValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVariableBounds(Map<String, int[]> changeVariableBounds) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVariableWeightForConstraint(String name, double weight, String var) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVariableWeights(Map<String, Double> changeVariableWeights) throws IlpSolverException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVariableWeightsForConstraints(Map<String, Map<String, Double>> changeConstraitVariableWeights) {
		// TODO Auto-generated method stub

	}

	@Override
	public Statistics solve() throws IlpSolverException {
		final Solver solver = (new SolverHelper(config).getSolver());
		solver.buildILPProblem(problem);
		solver.solve();
		solver.updateValuesFromSolution();
		
		// TODO
		return null;
	}

}
