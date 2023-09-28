package ilp.wrapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoflon.ilp.BinaryVariable;
import org.emoflon.ilp.LinearConstraint;
import org.emoflon.ilp.LinearFunction;
import org.emoflon.ilp.LinearTerm;
import org.emoflon.ilp.Operator;
import org.emoflon.ilp.Problem;
import org.emoflon.ilp.RealVariable;
import org.emoflon.ilp.SOS1Constraint;
import org.emoflon.ilp.Solver;
import org.emoflon.ilp.SolverConfig;
import org.emoflon.ilp.SolverConfig.SolverType;
import org.emoflon.ilp.SolverHelper;
import org.emoflon.ilp.SolverOutput;
import org.emoflon.ilp.SolverStatus;
import org.emoflon.ilp.Term;

import ilp.wrapper.IlpSolverException;
import ilp.wrapper.IncrementalIlpSolver;
import ilp.wrapper.Statistics;

public class EmoflonIlpSolver implements IncrementalIlpSolver {

	private final Problem problem;
	private final SolverConfig config;
	private SolverOutput output = null;

	private HashMap<String, org.emoflon.ilp.BinaryVariable> vars = new HashMap<>();
	private HashMap<org.emoflon.ilp.BinaryVariable, Double> varWeightToObj = new HashMap<>();

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
		// TODO: Name
		final LinearConstraint cnstr = new LinearConstraint(Operator.EQUAL, right);
		problem.add(cnstr);
	}

	@Override
	public void addEqualsConstraint(String name, double right, double[] weights, String[] vars)
			throws IlpSolverException {
		// TODO: Name

		final List<Term> terms = new ArrayList<Term>();

		for (int i = 0; i < vars.length; i++) {
			final double w = weights[i];
			final String varName = vars[i];

			final org.emoflon.ilp.Variable<Integer> var = new BinaryVariable(varName);
			terms.add(new LinearTerm(var, w));
		}

		final LinearConstraint cnstr = new LinearConstraint(terms, Operator.EQUAL, right);
		problem.add(cnstr);
	}

	@Override
	public void addEqualsConstraints(Constraint[] constraints) throws IlpSolverException {
		for (int i = 0; i < constraints.length; i++) {
			final Constraint c = constraints[i];
			addEqualsConstraint(c.getName(), c.getRight());
		}
	}

	@Override
	public void addLessOrEqualsConstraint(String name, double right) throws IlpSolverException {
		// TODO : Name
		final LinearConstraint cnstr = new LinearConstraint(Operator.LESS_OR_EQUAL, right);
		problem.add(cnstr);
	}

	@Override
	public void addLessOrEqualsConstraint(String name, double right, double[] weights, String[] vars)
			throws IlpSolverException {
		// TODO: Name

		final List<Term> terms = new ArrayList<Term>();

		for (int i = 0; i < vars.length; i++) {
			final double w = weights[i];
			final String varName = vars[i];

			final org.emoflon.ilp.Variable<Integer> var = new BinaryVariable(varName);
			terms.add(new LinearTerm(var, w));
		}

		final LinearConstraint cnstr = new LinearConstraint(terms, Operator.LESS_OR_EQUAL, right);
		problem.add(cnstr);
	}

	@Override
	public void addLessOrEqualsConstraints(Constraint[] constraints) throws IlpSolverException {
		for (int i = 0; i < constraints.length; i++) {
			final Constraint c = constraints[i];
			addLessOrEqualsConstraint(c.getName(), c.getRight());
		}
	}

	@Override
	public void addToVariableWeight(String name, double change) throws IlpSolverException {
		// TODO

	}

	@Override
	public void addVariable(String name, double solutionWeight) throws IlpSolverException {
		final BinaryVariable var = new BinaryVariable(name);
		if (!vars.containsKey(name)) {
			vars.put(name, var);
			varWeightToObj.put(var, solutionWeight);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void addVariables(Variable[] variables) throws IlpSolverException {
		for (final Variable v : variables) {
			addVariable(v.getName(), v.getWeight());
		}
	}

	@Override
	public void changeVariableBounds(String name, int lower, int upper) throws IlpSolverException {
		// TODO

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
		final Map<String, Boolean> mappings = new HashMap<String, Boolean>();
		for (final org.emoflon.ilp.Variable<?> v : problem.getVariables().values()) {
			mappings.put(v.getName(), v.getValue().doubleValue() > 0.5);
		}
		return mappings;
	}

	@Override
	public double getObjectiveValue() throws IlpSolverException {
		return output.getObjVal();
	}

	@Override
	public int getVariableCount() {
		return problem.getVariableCount();
	}

	@Override
	public boolean hasVariable(String name) throws IlpSolverException {
		return vars.containsKey(name);
	}

	@Override
	public boolean isSelected(String name) throws IlpSolverException {
		return ((Number) problem.getVariables().get(name).getValue()).doubleValue() > 0.5;
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
		for (final String name : changeVariableWeights.keySet()) {
			final BinaryVariable var = vars.get(name);
			varWeightToObj.remove(var);
			varWeightToObj.put(var, changeVariableWeights.get(name));
		}

	}

	@Override
	public void setVariableWeightsForConstraints(Map<String, Map<String, Double>> changeConstraitVariableWeights) {
		// TODO

	}

	@Override
	public Statistics solve() throws IlpSolverException {
		final LinearFunction obj = new LinearFunction();
		for (final BinaryVariable var : varWeightToObj.keySet()) {
			obj.addTerm(new LinearTerm(var, varWeightToObj.get(var)));
		}
		problem.setObjective(obj);

		final Solver solver = (new SolverHelper(config).getSolver());
		solver.buildILPProblem(problem);
		output = solver.solve();
		solver.updateValuesFromSolution();

		// TODO
		Statistics stats = null;

		if (output.getStatus() == SolverStatus.OPTIMAL) {
			stats = new Statistics(ilp.wrapper.SolverStatus.OPTIMAL, -1);
		}

		return stats;
	}

}
