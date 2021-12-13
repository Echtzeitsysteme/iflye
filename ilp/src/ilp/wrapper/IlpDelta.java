package ilp.wrapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import ilp.wrapper.IncrementalIlpSolver.Constraint;
import ilp.wrapper.IncrementalIlpSolver.SosConstraint;
import ilp.wrapper.IncrementalIlpSolver.Variable;

/**
 * Data object that holds new, changed or removed variables and constraints.
 *
 * Parts of this implementation are heavily inspired, taken or adapted from the
 * idyve project [1].
 *
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in
 * Rechenzentren, http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI
 * 10.12921/TUPRINTS– 00017362, 2020.
 *
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class IlpDelta {

	/*
	 * Added variables and constraints.
	 */
	final SortedMap<String, Variable> addVariables = new TreeMap<>();
	final SortedMap<String, Constraint> addEqConstraints = new TreeMap<>();
	final SortedMap<String, Constraint> addLeConstraints = new TreeMap<>();
	final SortedMap<String, SosConstraint> addSosConstraints = new TreeMap<>();

	/*
	 * Changed variable and constraint parameters.
	 */
	final SortedMap<String, Double> changeVariableWeights = new TreeMap<>();
	final SortedMap<String, int[]> changeVariableBounds = new TreeMap<>();
	final SortedMap<String, Map<String, Double>> changeConstraintVariableWeights = new TreeMap<>();
	final SortedMap<String, Double> changeConstraintRight = new TreeMap<>();

	/*
	 * Removed variables and constraints.
	 */
	final List<String> removeVariables = new LinkedList<>();
	final List<String> removeConstraints = new LinkedList<>();

	/**
	 * Adds an SOS1 constraint with given name. Uses all variables corresponding to
	 * the given variable IDs for the constraint.
	 *
	 * @param name   Name of the new constraint.
	 * @param varIds List of variable IDs for the variables to use in the new
	 *               constraint.
	 */
	public void addSosConstraint(final String name, final List<String> varIds) {
		final List<Variable> vars = new LinkedList<>();
		varIds.forEach(id -> vars.add(addVariables.get(id)));
		addSosConstraints.put(name, new SosConstraint(name, vars));
	}

	/**
	 * Adds a simple equals constraint.
	 *
	 * @param name  Name of the new constraint.
	 * @param right Value of the right side.
	 */
	public void addEqualsConstraint(final String name, final int right) {
		addEqConstraints.put(name, new Constraint(name, right));
	}

	/**
	 * Adds an equals constraint.
	 *
	 * @param name    Name of the new constraint.
	 * @param right   Value of the right side.
	 * @param weights Array of integers defining the weights for each variable.
	 * @param vars    Array of strings defining the variable names.
	 */
	public void addEqualsConstraint(final String name, final int right, final int[] weights, final String[] vars) {
		addEqConstraints.put(name, new Constraint(name, right).addVars(vars, weights));
	}

	/**
	 * Adds an array of already defined equal constraints.
	 *
	 * @param constraints Array of constraints.
	 */
	public void addEqualsConstraints(final Constraint[] constraints) {
		addEqConstraints
				.putAll(Arrays.stream(constraints).collect(Collectors.toMap(Constraint::getName, Function.identity())));
	}

	/**
	 * Adds a simple less or equals constraint.
	 *
	 * @param name  Name of the new constraint.
	 * @param right Value of the right side.
	 */
	public void addLessOrEqualsConstraint(final String name, final int right) {
		addLeConstraints.put(name, new Constraint(name, right));
	}

	/**
	 * Adds a less or equals constraint.
	 *
	 * @param name    Name of the new constraint.
	 * @param right   Value of the right side.
	 * @param weights Array of integers defining the weights for each variable.
	 * @param vars    Array of strings defining the variable names.
	 */
	public void addLessOrEqualsConstraint(final String name, final int right, final int[] weights,
			final String[] vars) {
		addLeConstraints.put(name, new Constraint(name, right).addVars(vars, weights));
	}

	/**
	 * Adds an array of already defined less or equals constraints.
	 *
	 * @param constraints Array of constraints.
	 */
	public void addLessOrEqualsConstraints(final Constraint[] constraints) {
		addLeConstraints
				.putAll(Arrays.stream(constraints).collect(Collectors.toMap(Constraint::getName, Function.identity())));
	}

	/**
	 * Adds a variable with a given name and solution weight.
	 *
	 * @param name           Name of the variable to add.
	 * @param solutionWeight Weight of the variable in solution.
	 */
	public void addVariable(final String name, final double solutionWeight) {
		addVariables.put(name, new Variable(name, solutionWeight));
	}

	/**
	 * Adds a predefined array of variables.
	 *
	 * @param variables Predefined array of variables.
	 */
	public void addVariables(final Variable[] variables) {
		addVariables.putAll(Arrays.stream(variables).collect(Collectors.toMap(Variable::getName, Function.identity())));
	}

	/**
	 * Returns true if there is a variable with matching name.
	 *
	 * @param name Variable name to match.
	 * @return True if there is a variable with matching name.
	 */
	public boolean hasAddVariable(final String name) {
		return addVariables.containsKey(name);
	}

	/**
	 * Applies the collections of constraints and variables to the given incremental
	 * ILP solver.
	 *
	 * @param solver Incremental ILP solver to add constraints and variables to.
	 * @throws IlpSolverException Throws an IlpSolverException of there is a problem
	 *                            with the solver.
	 */
	public void apply(final IncrementalIlpSolver solver) throws IlpSolverException {
		if (!addVariables.isEmpty()) {
			solver.addVariables(addVariables.values().toArray(new Variable[addVariables.size()]));
		}
		if (!addEqConstraints.isEmpty()) {
			solver.addEqualsConstraints(addEqConstraints.values().toArray(new Constraint[addEqConstraints.size()]));
		}
		if (!addLeConstraints.isEmpty()) {
			solver.addLessOrEqualsConstraints(
					addLeConstraints.values().toArray(new Constraint[addLeConstraints.size()]));
		}
		if (!addSosConstraints.isEmpty()) {
			solver.addSosConstraints(addSosConstraints.values().toArray(new SosConstraint[addSosConstraints.size()]));
		}

		if (!changeVariableWeights.isEmpty()) {
			solver.setVariableWeights(changeVariableWeights);
		}
		if (!changeVariableBounds.isEmpty()) {
			solver.setVariableBounds(changeVariableBounds);
		}
		if (!changeConstraintVariableWeights.isEmpty()) {
			solver.setVariableWeightsForConstraints(changeConstraintVariableWeights);
		}
		if (!changeConstraintRight.isEmpty()) {
			solver.setConstraintRights(changeConstraintRight);
		}

		if (!removeVariables.isEmpty()) {
			solver.removeVariables(removeVariables);
		}
		if (!removeConstraints.isEmpty()) {
			solver.removeConstraints(removeConstraints);
		}
	}

	/**
	 * Changes variable bounds.
	 *
	 * @param name  Name of the variable.
	 * @param lower Lower bound.
	 * @param upper Upper bound.
	 */
	public void changeVariableBounds(final String name, final int lower, final int upper) {
		changeVariableBounds.put(name, new int[] { lower, upper });
	}

	/**
	 * Changes variable weight.
	 *
	 * @param name           Name of the variable.
	 * @param solutionWeight New solution weight.
	 */
	public void changeVariableWeight(final String name, final double solutionWeight) {
		if (addVariables.containsKey(name)) {
			addVariables.get(name).setWeight(solutionWeight);
		} else {
			changeVariableWeights.put(name, solutionWeight);
		}
	}

	/**
	 * Removes a given constraint.
	 *
	 * @param name Name of the constraint to remove.
	 */
	public void removeConstraint(final String name) {
		removeConstraints.add(name);
	}

	/**
	 * Removed a given variable.
	 *
	 * @param name Name of the variable to remove.
	 */
	public void removeVariable(final String name) {
		removeVariables.add(name);
	}

	/**
	 * Sets the right side of a given constraint to a given value.
	 *
	 * @param name     Name of the constraint.
	 * @param newRight New value of the right side.
	 */
	public void setConstraintRight(final String name, final double newRight) {
		if (addEqConstraints.containsKey(name)) {
			addEqConstraints.get(name).setRight(newRight);
		} else if (addLeConstraints.containsKey(name)) {
			addLeConstraints.get(name).setRight(newRight);
		} else {
			changeConstraintRight.put(name, newRight);
		}
	}

	/**
	 * Sets the weight for a given constraint's variable to a given value.
	 *
	 * @param name   Name of the constraint.
	 * @param weight Value of the variable's weight.
	 * @param var    Name of the variable.
	 */
	public void setVariableWeightForConstraint(final String name, final double weight, final String var) {
		if (addEqConstraints.containsKey(name)) {
			addEqConstraints.get(name).addVar(var, weight);
		} else if (addLeConstraints.containsKey(name)) {
			addLeConstraints.get(name).addVar(var, weight);
		} else {
			changeConstraintVariableWeights.computeIfAbsent(name, k -> new HashMap<>()).put(var, weight);
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();

		for (final String key : addVariables.keySet()) {
			builder.append(key + addVariables.get(key));
			builder.append(System.lineSeparator());
		}

		for (final String key : addEqConstraints.keySet()) {
			builder.append(key + addEqConstraints.get(key));
			builder.append(System.lineSeparator());
		}

		for (final String key : addLeConstraints.keySet()) {
			builder.append(key + addLeConstraints.get(key));
			builder.append(System.lineSeparator());
		}

		return builder.toString();
	}

}
