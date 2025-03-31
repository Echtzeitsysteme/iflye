package ilp.wrapper.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilp.wrapper.IlpSolverException;
import ilp.wrapper.IncrementalIlpSolver;
import ilp.wrapper.SolverStatus;
import ilp.wrapper.Statistics;
import ilp.wrapper.config.IlpSolverConfig;

/**
 * Implementation of the {@link IncrementalIlpSolver} interface for the IBM
 * CPLEX solver.
 *
 * Parts of this implementation are heavily inspired, taken or adapted from the
 * idyve project [1].
 *
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in
 * Rechenzentren, http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI
 * 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class IncrementalCplexSolver implements IncrementalIlpSolver {

	/**
	 * CPLEX object (solver and model).
	 */
	private IloCplex cplex;

	/**
	 * Objective object.
	 */
	private IloObjective obj;

	/**
	 * Mappings of strings (variable names) to CPLEX variables.
	 */
	private final Map<String, IloIntVar> variables = new HashMap<>();

	/**
	 * Mappings of strings (constraint names) to CPLEXC ranges.
	 */
	private final Map<String, IloRange> constraints = new HashMap<>();

	/**
	 * Mappings of strings (objective coefficients) to doubles.
	 */
	private final Map<String, Double> objectiveCoefficients = new HashMap<>();

	/**
	 * Variable for the final objective value.
	 */
	private double objectiveValue = -1;

	/**
	 * Creates a new object of this incremental CPLEX solver with the given
	 * parameters.
	 *
	 * @param timelimit  Time limit for the solver.
	 * @param randomSeed Random seed for the solver.
	 */
	public IncrementalCplexSolver(final int timelimit, final int randomSeed) {
		try {
			cplex = new IloCplex();
			cplex.setParam(IloCplex.Param.TimeLimit, timelimit);
			cplex.setParam(IloCplex.Param.RandomSeed, randomSeed);
			cplex.setParam(IloCplex.Param.Preprocessing.Presolve, IlpSolverConfig.ENABLE_PRESOLVE);

			if (!IlpSolverConfig.ENABLE_ILP_OUTPUT) {
				cplex.setOut(null);
			}

			obj = cplex.addMinimize(cplex.linearNumExpr());
		} catch (final IloException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void addSosConstraint(final SosConstraint constraint) {
		// SOS1 constraints with equal weights are not supported by CPLEX.
		//
		// "Members of an SOS should be given unique weights that in turn define the
		// order of the variables in the set. (These unique weights are also called
		// reference row values.) Each of those ways of declaring SOS members allows you
		// to specify weights."
		//
		// Source: https://www.ibm.com/docs/en/icos/22.1.2?topic=sos-declaring-members
		//
		System.out.println("=> WARNING: SOS1 constraints are currently not supported by the CPLEX implementation!");
		return;
	}

	@Override
	public void addSosConstraints(final SosConstraint[] constraints) {
		for (final SosConstraint c : constraints) {
			addSosConstraint(c);
		}
	}

	@Override
	public void addEqualsConstraint(final String name, final double right) throws IlpSolverException {
		try {
			final IloLinearNumExpr linearNumExpr = cplex.linearNumExpr();
			constraints.put(name, cplex.addEq(right, linearNumExpr, name));
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void addEqualsConstraint(final String name, final double right, final double[] weights, String[] vars)
			throws IlpSolverException {
		try {
			final IloLinearNumExpr linearNumExpr = cplex.linearNumExpr();
			for (int i = 0; i < weights.length; i++) {
				linearNumExpr.addTerm(variables.get(vars[i]), weights[i]);
			}
			constraints.put(name, cplex.addEq(right, linearNumExpr, name));
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void addEqualsConstraints(final Constraint[] constraints) throws IlpSolverException {
		for (final Constraint constraint : constraints) {
			addEqualsConstraint(constraint.getName(), constraint.getRight(),
					constraint.getWeights().stream().mapToDouble(w -> w).toArray(),
					constraint.getVarnames().toArray(new String[constraint.getVarnames().size()]));
		}
	}

	@Override
	public void addLessOrEqualsConstraint(final String name, final double right) throws IlpSolverException {
		try {
			final IloLinearNumExpr linearNumExpr = cplex.linearNumExpr();
			constraints.put(name, cplex.addGe(right, linearNumExpr, name));
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void addLessOrEqualsConstraint(final String name, final double right, final double[] weights, String[] vars)
			throws IlpSolverException {
		try {
			final IloLinearNumExpr linearNumExpr = cplex.linearNumExpr();
			for (int i = 0; i < weights.length; i++) {
				linearNumExpr.addTerm(variables.get(vars[i]), weights[i]);
			}
			constraints.put(name, cplex.addGe(right, linearNumExpr, name));
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void addLessOrEqualsConstraints(final Constraint[] constraints) throws IlpSolverException {
		for (final Constraint constraint : constraints) {
			addLessOrEqualsConstraint(constraint.getName(), constraint.getRight(),
					constraint.getWeights().stream().mapToDouble(i -> i).toArray(),
					constraint.getVarnames().toArray(new String[constraint.getVarnames().size()]));
		}
	}

	@Override
	public void addToVariableWeight(final String name, final double change) throws IlpSolverException {
		try {
			final double newCoef = objectiveCoefficients.get(name) + change;
			cplex.setLinearCoef(obj, newCoef, variables.get(name));
			objectiveCoefficients.put(name, newCoef);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void addVariable(final String name, final double solutionWeight) throws IlpSolverException {
		try {
			final IloIntVar boolVar = cplex.boolVar(name);
			variables.put(name, boolVar);
			final IloLinearNumExpr linearNumExpr = cplex.linearNumExpr();
			linearNumExpr.addTerm(boolVar, solutionWeight);
			cplex.addToExpr(obj, linearNumExpr);
			objectiveCoefficients.put(name, solutionWeight);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void addVariables(final Variable[] variables) throws IlpSolverException {
		try {
			final IloIntVar[] addVars = cplex.boolVarArray(variables.length,
					Arrays.stream(variables).map(Variable::getName).toArray(String[]::new));
			final IloLinearNumExpr linearNumExpr = cplex.linearNumExpr();
			for (int i = 0; i < addVars.length; i++) {
				final IloIntVar var = addVars[i];
				this.variables.put(var.getName(), var);
				linearNumExpr.addTerm(var, variables[i].getWeight());
				objectiveCoefficients.put(var.getName(), variables[i].getWeight());
			}
			cplex.addToExpr(obj, linearNumExpr);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void changeVariableBounds(final String name, final int lower, final int upper) throws IlpSolverException {
		try {
			variables.get(name).setLB(lower);
			variables.get(name).setUB(upper);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void changeVariableWeight(final String name, final double solutionWeight) throws IlpSolverException {
		try {
			cplex.setLinearCoef(obj, solutionWeight, variables.get(name));
			objectiveCoefficients.put(name, solutionWeight);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void dispose() throws IlpSolverException {
		try {
			cplex.clearModel();
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public int getConstraintCount() {
		return constraints.size();
	}

	@Override
	public Map<String, Boolean> getMappings() throws IlpSolverException {
		return variables.values().stream().collect(Collectors.toMap(IloNumVar::getName, v -> {
			try {
				return cplex.getValue(v) > 0.5;
			} catch (final IloException e) {
				throw new IlpSolverException(e);
			}
		}));
	}

	@Override
	public double getObjectiveValue() throws IlpSolverException {
		return objectiveValue;
	}

	@Override
	public int getVariableCount() {
		return variables.size();
	}

	@Override
	public boolean hasVariable(final String name) throws IlpSolverException {
		return variables.containsKey(name);
	}

	@Override
	public boolean isSelected(final String name) throws IlpSolverException {
		try {
			return cplex.getValue(variables.get(name)) > 0.5;
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void loadModel(final String path) throws IlpSolverException {
		try {
			cplex.importModel(path);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
		obj = cplex.getObjective();
	}

	@Override
	public void removeConstraint(final String name) throws IlpSolverException {
		try {
			cplex.remove(constraints.remove(name));
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void removeConstraints(final List<String> removeConstraints) {
		try {
			cplex.remove(removeConstraints.stream().map(constraints::remove).toArray(IloNumVar[]::new));
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void removeVariable(final String name) throws IlpSolverException {
		try {
			cplex.delete(variables.remove(name));
			objectiveCoefficients.remove(name);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void removeVariables(final List<String> removeVariables) throws IlpSolverException {
		try {
			cplex.delete(removeVariables.stream().map(variables::remove).toArray(IloNumVar[]::new));
			removeVariables.forEach(objectiveCoefficients::remove);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void save(final String file) throws IlpSolverException {
		try {
			cplex.exportModel(file);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void setConstraintRight(final String name, final double newRight) throws IlpSolverException {
		try {
			constraints.get(name).setBounds(Double.NEGATIVE_INFINITY, newRight);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void setConstraintRights(final Map<String, Double> changeConstraintRight) {
		for (final Entry<String, Double> entry : changeConstraintRight.entrySet()) {
			setConstraintRight(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void setSeed(final int seed) {
		try {
			cplex.setParam(IloCplex.Param.RandomSeed, seed);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void setTimeout(final int parameterValue) {
		try {
			cplex.setParam(IloCplex.Param.TimeLimit, parameterValue);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void setVariableBounds(final Map<String, int[]> changeVariableBounds) throws IlpSolverException {
		for (final Entry<String, int[]> entry : changeVariableBounds.entrySet()) {
			changeVariableBounds(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
		}
	}

	@Override
	public void setVariableWeightForConstraint(final String name, final double weight, final String var)
			throws IlpSolverException {
		try {
			cplex.setLinearCoef(constraints.get(name), weight, variables.get(var));
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

	@Override
	public void setVariableWeights(final Map<String, Double> changeVariableWeights) throws IlpSolverException {
		setVariableWeightsForConstraint(changeVariableWeights, (w, v) -> {
			try {
				cplex.setLinearCoefs(obj, w, v);
			} catch (final IloException e) {
				throw new IlpSolverException(e);
			}
		}, true);
	}

	/**
	 * Sets the variable weights for a constraint.
	 *
	 * @param changeVariableWeights Map of string -> double for the new weights.
	 * @param changer               Changes the values inside the CPLEX model.
	 * @param objective             True if relevant for the objective.
	 */
	private void setVariableWeightsForConstraint(final Map<String, Double> changeVariableWeights,
			final BiConsumer<double[], IloIntVar[]> changer, final boolean objective) {
		final double[] weights = new double[changeVariableWeights.size()];
		final IloIntVar[] vars = new IloIntVar[changeVariableWeights.size()];
		int i = 0;
		for (final Entry<String, Double> entry : changeVariableWeights.entrySet()) {
			vars[i] = variables.get(entry.getKey());
			weights[i] = entry.getValue();
			if (objective) {
				objectiveCoefficients.put(entry.getKey(), entry.getValue());
			}
			i++;
		}
		changer.accept(weights, vars);
	}

	@Override
	public void setVariableWeightsForConstraints(
			final Map<String, Map<String, Double>> changeConstraitVariableWeights) {
		for (final Entry<String, Map<String, Double>> entry : changeConstraitVariableWeights.entrySet()) {
			final IloRange expr = constraints.get(entry.getKey());
			setVariableWeightsForConstraint(entry.getValue(), (w, v) -> {
				try {
					cplex.setLinearCoefs(expr, w, v);
				} catch (final IloException e) {
					throw new IlpSolverException(e);
				}
			}, true);
		}
	}

	@Override
	public Statistics solve() throws IlpSolverException {
		try {
			final long start = System.nanoTime();
			final boolean solve = cplex.solve();
			if (solve) {
				objectiveValue = cplex.getObjValue();
			} else {
				objectiveValue = -1;
			}

			SolverStatus status;
			if (cplex.getStatus() == IloCplex.Status.Unbounded) {
				status = SolverStatus.UNBOUNDED;
			} else if (cplex.getStatus() == IloCplex.Status.InfeasibleOrUnbounded) {
				status = SolverStatus.INF_OR_UNBD;
			} else if (cplex.getStatus() == IloCplex.Status.Infeasible) {
				status = SolverStatus.INFEASIBLE;
			} else if (cplex.getStatus() == IloCplex.Status.Optimal) {
				status = SolverStatus.OPTIMAL;
			} else if (cplex.getStatus() == IloCplex.Status.Unknown) {
				status = SolverStatus.TIME_OUT;
			} else {
				throw new RuntimeException("Unknown solver status.");
			}
			return new Statistics(status, System.nanoTime() - start);
		} catch (final IloException e) {
			throw new IlpSolverException(e);
		}
	}

}
