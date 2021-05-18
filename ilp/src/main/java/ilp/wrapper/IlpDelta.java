package ilp.wrapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import ilp.wrapper.IncrementalIlpSolver.Constraint;
import ilp.wrapper.IncrementalIlpSolver.Variable;

public class IlpDelta {

  final Map<String, Variable> addVariables = new HashMap<>();
  final Map<String, Constraint> addEqConstraints = new HashMap<>();
  final Map<String, Constraint> addLeConstraints = new HashMap<>();

  final Map<String, Double> changeVariableWeights = new HashMap<>();
  final Map<String, int[]> changeVariableBounds = new HashMap<>();
  final Map<String, Map<String, Double>> changeConstraitVariableWeights = new HashMap<>();
  final Map<String, Double> changeConstraintRight = new HashMap<>();

  final List<String> removeVariables = new LinkedList<>();
  final List<String> removeConstraints = new LinkedList<>();

  public void addEqualsConstraint(final String name, final int right) throws IlpSolverException {
    addEqConstraints.put(name, new Constraint(name, right));
  }

  public void addEqualsConstraint(final String name, final int right, final int[] weights,
      final String[] vars) throws IlpSolverException {
    addEqConstraints.put(name, new Constraint(name, right).addVars(vars, weights));
  }

  public void addEqualsConstraints(final Constraint[] constraints) throws Exception {
    addEqConstraints.putAll(Arrays.stream(constraints)
        .collect(Collectors.toMap(Constraint::getName, Function.identity())));
  }

  public void addLessOrEqualsConstraint(final String name, final int right)
      throws IlpSolverException {
    addLeConstraints.put(name, new Constraint(name, right));
  }

  public void addLessOrEqualsConstraint(final String name, final int right, final int[] weights,
      final String[] vars) throws IlpSolverException {
    addLeConstraints.put(name, new Constraint(name, right).addVars(vars, weights));
  }

  public void addLessOrEqualsConstraints(final Constraint[] constraints) throws Exception {
    addLeConstraints.putAll(Arrays.stream(constraints)
        .collect(Collectors.toMap(Constraint::getName, Function.identity())));
  }

  public void addVariable(final String name, final double solutionWeight)
      throws IlpSolverException {
    addVariables.put(name, new Variable(name, solutionWeight));
  }

  public void addVariables(final Variable[] variables) throws Exception {
    addVariables.putAll(
        Arrays.stream(variables).collect(Collectors.toMap(Variable::getName, Function.identity())));
  }

  public void apply(final IncrementalIlpSolver solver) throws IlpSolverException {
    if (!addVariables.isEmpty()) {
      solver.addVariables(addVariables.values().toArray(new Variable[addVariables.size()]));
    }
    if (!addEqConstraints.isEmpty()) {
      solver.addEqualsConstraints(
          addEqConstraints.values().toArray(new Constraint[addEqConstraints.size()]));
    }
    if (!addLeConstraints.isEmpty()) {
      solver.addLessOrEqualsConstraints(
          addLeConstraints.values().toArray(new Constraint[addLeConstraints.size()]));
    }

    if (!changeVariableWeights.isEmpty()) {
      solver.setVariableWeights(changeVariableWeights);
    }
    if (!changeVariableBounds.isEmpty()) {
      solver.setVariableBounds(changeVariableBounds);
    }
    if (!changeConstraitVariableWeights.isEmpty()) {
      solver.setVariableWeightsForConstraints(changeConstraitVariableWeights);
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

  public void changeVariableBounds(final String name, final int lower, final int upper)
      throws IlpSolverException {
    changeVariableBounds.put(name, new int[] {lower, upper});
  }

  public void changeVariableWeight(final String name, final double solutionWeight)
      throws IlpSolverException {
    if (addVariables.containsKey(name)) {
      addVariables.get(name).setWeight(solutionWeight);
    } else {
      changeVariableWeights.put(name, solutionWeight);
    }
  }

  public void removeConstraint(final String name) throws IlpSolverException {
    removeConstraints.add(name);
  }

  public void removeVariable(final String name) throws IlpSolverException {
    removeVariables.add(name);
  }

  public void setConstraintRight(final String name, final double newRight) {
    if (addEqConstraints.containsKey(name)) {
      addEqConstraints.get(name).setRight(newRight);
    } else if (addLeConstraints.containsKey(name)) {
      addLeConstraints.get(name).setRight(newRight);
    } else {
      changeConstraintRight.put(name, newRight);
    }
  }

  public void setVariableWeightForConstraint(final String name, final double weight,
      final String var) throws IlpSolverException {
    if (addEqConstraints.containsKey(name)) {
      addEqConstraints.get(name).addVar(var, weight);
    } else if (addLeConstraints.containsKey(name)) {
      addLeConstraints.get(name).addVar(var, weight);
    } else {
      changeConstraitVariableWeights.computeIfAbsent(name, k -> new HashMap<>()).put(var, weight);
    }
  }

}
