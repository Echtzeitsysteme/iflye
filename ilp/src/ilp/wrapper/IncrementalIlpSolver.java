package ilp.wrapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Interface that designs a super type for an incremental ILP solver implementation.
 * 
 * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [1].
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 * 
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public interface IncrementalIlpSolver {
  /**
   * ILP constraint implementation.
   * 
   * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [1].
   * 
   * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
   * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
   * 
   * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
   * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
   */
  public static class Constraint {
    private final String name;
    private double right;
    private final List<String> varnames;
    private final List<Double> weights;

    public Constraint(final String name, final int right) {
      this.name = name;
      this.right = right;
      varnames = new LinkedList<>();
      weights = new LinkedList<>();
    }

    public Constraint addVar(final String varname, final double weight) {
      varnames.add(varname);
      weights.add(weight);
      return this;
    }

    public Constraint addVars(final String[] vars, final int[] weights) {
      for (int i = 0; i < vars.length; i++) {
        addVar(vars[i], weights[i]);
      }
      return this;
    }

    public String getName() {
      return name;
    }

    public double getRight() {
      return right;
    }

    public List<String> getVarnames() {
      return varnames;
    }

    public List<Double> getWeights() {
      return weights;
    }

    public void setRight(final double newRight) {
      right = newRight;
    }

    @Override
    public String toString() {
      return "Constraint [name=" + name + ", right=" + right + ", varnames=" + varnames
          + ", weights=" + weights + "]";
    }

  }

  /**
   * ILP variable implementation.
   * 
   * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
   * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
   */
  public static class Variable {
    private final String name;
    private double weight;

    public Variable(final String name, final double solutionWeight) {
      super();
      this.name = name;
      weight = solutionWeight;
    }

    public String getName() {
      return name;
    }

    public double getWeight() {
      return weight;
    }

    public void setWeight(final double solutionWeight) {
      weight = solutionWeight;
    }

  }

  void addEqualsConstraint(final String name, final double right) throws IlpSolverException;

  void addEqualsConstraint(final String name, final double right, final double[] weights,
      String[] vars) throws IlpSolverException;

  void addEqualsConstraints(final Constraint[] constraints) throws IlpSolverException;

  void addLessOrEqualsConstraint(final String name, final double right) throws IlpSolverException;

  void addLessOrEqualsConstraint(final String name, final double right, double[] weights,
      String[] vars) throws IlpSolverException;

  void addLessOrEqualsConstraints(final Constraint[] constraints) throws IlpSolverException;

  void addToVariableWeight(final String name, final double change) throws IlpSolverException;

  void addVariable(final String name, final double solutionWeight) throws IlpSolverException;

  void addVariables(final Variable[] variables) throws IlpSolverException;

  void changeVariableBounds(final String name, final int lower, final int upper)
      throws IlpSolverException;

  void changeVariableWeight(final String name, final double solutionWeight)
      throws IlpSolverException;

  void dispose() throws IlpSolverException;

  int getConstraintCount();

  Map<String, Boolean> getMappings() throws IlpSolverException;

  default Map<String, Boolean> getMappings(final List<String> variables) throws IlpSolverException {
    return variables.stream().collect(Collectors.toMap(Function.identity(), this::isSelected));
  }

  double getObjectiveValue() throws IlpSolverException;

  int getVariableCount();

  boolean hasVariable(final String name) throws IlpSolverException;

  boolean isSelected(final String name) throws IlpSolverException;

  void loadModel(final String path) throws IlpSolverException;

  default void lockVariables(final Predicate<Entry<String, Boolean>> predicate)
      throws IlpSolverException {
    getMappings().entrySet().stream().filter(predicate)
        .forEach(e -> changeVariableBounds(e.getKey(), e.getValue() ? 1 : 0, e.getValue() ? 1 : 0));
  }

  void removeConstraint(final String name) throws IlpSolverException;

  void removeConstraints(final List<String> removeConstraints);

  void removeVariable(final String name) throws IlpSolverException;

  void removeVariables(final List<String> removeVariables) throws IlpSolverException;

  void save(final String file) throws IlpSolverException;

  void setConstraintRight(final String name, double newRight) throws IlpSolverException;

  void setConstraintRights(final Map<String, Double> changeConstraintRight);

  void setSeed(final int seed);

  void setTimeout(final int parameterValue);

  void setVariableBounds(final Map<String, int[]> changeVariableBounds) throws IlpSolverException;

  void setVariableWeightForConstraint(final String name, final double weight, String var)
      throws IlpSolverException;

  void setVariableWeights(final Map<String, Double> changeVariableWeights)
      throws IlpSolverException;

  void setVariableWeightsForConstraints(
      final Map<String, Map<String, Double>> changeConstraitVariableWeights);

  Statistics solve() throws IlpSolverException;

}
