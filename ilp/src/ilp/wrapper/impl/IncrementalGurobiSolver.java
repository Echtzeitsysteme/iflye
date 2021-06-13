package ilp.wrapper.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntParam;
import gurobi.GRB.StringAttr;
import gurobi.GRBCallback;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import ilp.wrapper.IlpSolverException;
import ilp.wrapper.IncrementalIlpSolver;
import ilp.wrapper.SolverStatus;
import ilp.wrapper.Statistics;
import ilp.wrapper.config.IlpSolverConfig;

/**
 * Implementation of the {@link IncrementalIlpSolver} interface for the Gurobi solver.
 * 
 * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [1].
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 * 
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class IncrementalGurobiSolver implements IncrementalIlpSolver {

  /**
   * Gurobi environment (for configuration etc.).
   */
  private final GRBEnv env;

  /**
   * Gurobi model.
   */
  private GRBModel model;

  // TODO: Change HashMap to UnifiedMap (Eclipse collections).

  /**
   * All variables.
   */
  private final Map<String, GRBVar> variables = new HashMap<>();

  /**
   * All constraints.
   */
  private final Map<String, GRBConstr> constraints = new HashMap<>();

  /**
   * Mapping from variable to constraints.
   */
  private final Map<GRBVar, Set<GRBConstr>> varConstraints = new HashMap<>();

  /**
   * Mapping from constraint to variables.
   */
  private final Map<GRBConstr, Set<GRBVar>> constraintVars = new HashMap<>();

  // TODO: Find a better name for this variable
  private final long[] vals = {-1, -1, -1};

  /**
   * Constructor that initializes a new Gurobi solver object for a given time limit and random seed.
   * 
   * @param timelimit Time limit to set for the solver.
   * @param randomSeed Random seed to set for the solver.
   */
  public IncrementalGurobiSolver(final int timelimit, final int randomSeed) {
    try {
      env = new GRBEnv("Gurobi_ILP.log");
      env.set(DoubleParam.TimeLimit, timelimit);
      env.set(IntParam.Seed, randomSeed);
      if (!IlpSolverConfig.ENABLE_ILP_OUTPUT) {
        env.set(IntParam.OutputFlag, 0);
      }
      if (IlpSolverConfig.IMPROVE_PARAMS) {
        env.set(IntParam.DegenMoves, 1);
        env.set(DoubleParam.Heuristics, 0.001);
        env.set(IntParam.CutPasses, 1);
      }
      model = new GRBModel(env);
      model.set(DoubleParam.TimeLimit, timelimit);
      model.set(IntParam.Seed, randomSeed);
      model.setCallback(new GRBCallback() {
        @Override
        protected void callback() {
          if (where == GRB.CB_PRESOLVE) {
            try {
              synchronized (vals) {
                vals[0] = getIntInfo(GRB.CB_PRE_COLDEL);
                vals[1] = getIntInfo(GRB.CB_PRE_ROWDEL);
                vals[2] = System.nanoTime();
              }
            } catch (final GRBException e) {
              throw new IlpSolverException(e);
            }
          }
        }
      });
    } catch (final GRBException e) {
      e.printStackTrace();
      throw new IlpSolverException(e);
    }
  }

  /**
   * Adds a constraint to the solver.
   * 
   * @param name Name of the constraint.
   * @param right Value of the right side.
   * @param weights Variable weights.
   * @param vars Variables.
   * @param chr Senses.
   * @throws IlpSolverException If the solver encounters a problem.
   */
  private void addConstraint(final String name, final double right, final double[] weights,
      final String[] vars, final char chr) throws IlpSolverException {
    try {
      final GRBLinExpr grbLinExpr = new GRBLinExpr();
      final GRBVar[] grbVars = Arrays.stream(vars).map(variables::get).toArray(s -> new GRBVar[s]);
      grbLinExpr.addTerms(weights, grbVars);
      final GRBConstr addConstr = model.addConstr(grbLinExpr, chr, right, name);
      for (final GRBVar grbVar : grbVars) {
        // TODO: Change HashSet to UnifiedSet (Eclipse collection).
        varConstraints.computeIfAbsent(grbVar, k -> new HashSet<>()).add(addConstr);
        constraintVars.computeIfAbsent(addConstr, k -> new HashSet<>()).add(grbVar);
      }
      constraints.put(name, addConstr);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  /**
   * Adds predefined constraints to the solver.
   * 
   * @param constrs Array of predefined constraints.
   * @param chr Senses.
   * @throws IlpSolverException If the solver encounters a problem.
   */
  private void addConstraints(final Constraint[] constrs, final char chr)
      throws IlpSolverException {
    final GRBLinExpr[] grbLinExprs = new GRBLinExpr[constrs.length];
    try {
      for (int j = 0; j < constrs.length; j++) {
        final Constraint c = constrs[j];
        final GRBLinExpr expr = new GRBLinExpr();
        expr.addTerms(c.getWeights().stream().mapToDouble(i -> i).toArray(),
            c.getVarnames().stream().map(variables::get).toArray(GRBVar[]::new));

        grbLinExprs[j] = expr;
      }
      final char[] senses = new char[constrs.length];
      Arrays.fill(senses, chr);
      final double[] rhs = Arrays.stream(constrs).mapToDouble(Constraint::getRight).toArray();
      final String[] names = Arrays.stream(constrs).map(Constraint::getName).toArray(String[]::new);

      final GRBConstr[] addConstrs = model.addConstrs(grbLinExprs, senses, rhs, names);
      for (int i = 0; i < addConstrs.length; i++) {
        final GRBConstr constr = addConstrs[i];
        constraints.put(constrs[i].getName(), constr);
        for (final String var : constrs[i].getVarnames()) {
          final GRBVar key = getVariable(var);
          if (!varConstraints.containsKey(key)) {
            // TODO: Change HashSet to UnifiedSet (Eclipse collection).
            varConstraints.putIfAbsent(key, new HashSet<>());
          }
          varConstraints.get(key).add(constr);
          if (!constraintVars.containsKey(constr)) {
            // TODO: Change HashSet to UnifiedSet (Eclipse collection).
            constraintVars.putIfAbsent(constr, new HashSet<>());
          }
          constraintVars.get(constr).add(key);
          // varConstraints.computeIfAbsent(variables.get(var),
          // v -> varConstraints.computeIfAbsent(v, k -> new HashSet<>())).add(constr);
        }
      }
    } catch (final GRBException | NullPointerException e) {
      throw new IlpSolverException(e);
    }
  }

  /**
   * Returns the Gurobi variable for a given name.
   * 
   * @param name Name to get the variable for.
   * @return Gurobi variable for name.
   */
  private GRBVar getVariable(final String name) {
    if (variables.containsKey(name)) {
      return variables.get(name);
    } else {
      throw new IlpSolverException("Variable with the name=" + name + " does not exist.");
    }
  }

  @Override
  public void addSosConstraint(final SosConstraint constraint) {
    // All weights has to be 1
    final double[] weights = new double[constraint.getVars().size()];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = 1;
    }

    // Get all actual variable objects
    final GRBVar[] vars = new GRBVar[constraint.getVars().size()];
    for (int i = 0; i < vars.length; i++) {
      vars[i] = getVariable(constraint.getVars().get(i).getName());
    }

    try {
      model.addSOS(vars, weights, GRB.SOS_TYPE1);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void addSosConstraints(final SosConstraint[] constraints) {
    for (final SosConstraint c : constraints) {
      addSosConstraint(c);
    }
  }

  @Override
  public void addEqualsConstraint(final String name, final double right) throws IlpSolverException {
    addConstraint(name, right, new double[0], new String[0], GRB.EQUAL);
  }

  @Override
  public void addEqualsConstraint(final String name, final double right, final double[] weights,
      final String[] vars) throws IlpSolverException {
    addConstraint(name, right, weights, vars, GRB.EQUAL);
  }

  @Override
  public void addEqualsConstraints(final Constraint[] constraints) throws IlpSolverException {
    addConstraints(constraints, GRB.EQUAL);
  }

  @Override
  public void addLessOrEqualsConstraint(final String name, final double right)
      throws IlpSolverException {
    addConstraint(name, right, new double[0], new String[0], GRB.LESS_EQUAL);
  }

  @Override
  public void addLessOrEqualsConstraint(final String name, final double right,
      final double[] weights, final String[] vars) throws IlpSolverException {
    addConstraint(name, right, weights, vars, GRB.LESS_EQUAL);
  }

  @Override
  public void addLessOrEqualsConstraints(final Constraint[] constraints) throws IlpSolverException {
    addConstraints(constraints, GRB.LESS_EQUAL);
  }

  @Override
  public void addToVariableWeight(final String name, final double change)
      throws IlpSolverException {
    try {
      getVariable(name).set(DoubleAttr.Obj, getVariable(name).get(DoubleAttr.Obj) + change);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void addVariable(final String name, final double solutionWeight)
      throws IlpSolverException {
    try {
      final GRBVar addVar = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, name);
      addVar.set(DoubleAttr.Obj, solutionWeight);
      variables.put(name, addVar);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void addVariables(final Variable[] variables) throws IlpSolverException {
    final double[] lbs = new double[variables.length];
    final double[] ubs = new double[variables.length];
    final char[] types = new char[variables.length];
    Arrays.fill(lbs, 0);
    Arrays.fill(ubs, 1);
    Arrays.fill(types, GRB.BINARY);
    try {
      final GRBVar[] addVars = model.addVars(lbs, ubs,
          Arrays.stream(variables).mapToDouble(Variable::getWeight).toArray(), types,
          Arrays.stream(variables).map(Variable::getName).toArray(String[]::new));
      for (int i = 0; i < addVars.length; i++) {
        this.variables.put(variables[i].getName(), addVars[i]);
      }
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void changeVariableBounds(final String name, final int lower, final int upper)
      throws IlpSolverException {
    try {
      getVariable(name).set(DoubleAttr.LB, lower);
      getVariable(name).set(DoubleAttr.UB, upper);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void changeVariableWeight(final String name, final double solutionWeight)
      throws IlpSolverException {
    try {
      getVariable(name).set(DoubleAttr.Obj, solutionWeight);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void dispose() throws IlpSolverException {
    model.dispose();
    try {
      env.dispose();
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public int getConstraintCount() {
    return constraints.size();
  }

  @Override
  public Map<String, Boolean> getMappings() throws IlpSolverException {
    return variables.values().stream().collect(Collectors.toMap(v -> {
      try {
        return v.get(StringAttr.VarName);
      } catch (final GRBException e) {
        throw new IlpSolverException(e);
      }
    }, v -> {
      try {
        return v.get(DoubleAttr.X) > 0.5;
      } catch (final GRBException e) {
        throw new IlpSolverException(e);
      }
    }));
  }

  @Override
  public double getObjectiveValue() throws IlpSolverException {
    try {
      return model.get(GRB.DoubleAttr.ObjVal);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
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
      final GRBVar grbVar = getVariable(name);
      return grbVar.get(DoubleAttr.X) > 0.5;
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void loadModel(final String path) throws IlpSolverException {
    try {
      model = new GRBModel(env, path);
      model.setCallback(new GRBCallback() {
        @Override
        protected void callback() {
          if (where == GRB.CB_PRESOLVE) {
            try {
              synchronized (vals) {
                vals[0] = getIntInfo(GRB.CB_PRE_COLDEL);
                vals[1] = getIntInfo(GRB.CB_PRE_ROWDEL);
                vals[2] = System.nanoTime();
              }
            } catch (final GRBException e) {
              throw new IlpSolverException(e);
            }
          }
        }
      });
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void removeConstraint(final String name) throws IlpSolverException {
    try {
      final GRBConstr removeConstr = constraints.remove(name);
      model.remove(removeConstr);
      if (constraintVars.containsKey(removeConstr)) {
        final Set<GRBVar> vars = constraintVars.remove(removeConstr);
        for (final GRBVar grbVar : vars) {
          varConstraints.get(grbVar).remove(removeConstr);
        }
      }
      // varConstraints.values().forEach(l -> l.remove(remove));
    } catch (final Exception e) {
      throw new IlpSolverException("Gurobi constraint for " + name + " does not exist. \n" + e);
    }
  }

  @Override
  public void removeConstraints(final List<String> removeConstraints) {
    removeConstraints.forEach(this::removeConstraint);
  }

  @Override
  public void removeVariable(final String name) throws IlpSolverException {
    final GRBVar grbVar = variables.remove(name);
    if (grbVar == null) {
      throw new IllegalArgumentException("Gurobi Var to " + name + " does not exist.");
    }
    final Set<GRBConstr> constraints = varConstraints.remove(grbVar);
    if (constraints != null && !constraints.isEmpty()) {
      for (final GRBConstr grbConstr : constraints) {
        try {
          model.chgCoeff(grbConstr, grbVar, 0);
          constraintVars.get(grbConstr).remove(grbVar);
        } catch (final GRBException e) {
          throw new IlpSolverException(e);
        }
      }
    }
    variables.remove(name);
    try {
      model.remove(grbVar);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void removeVariables(final List<String> removeVariables) throws IlpSolverException {
    for (final String removeVar : removeVariables) {
      removeVariable(removeVar);
    }
  }

  @Override
  public void save(final String file) throws IlpSolverException {
    try {
      model.write(file);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void setConstraintRight(final String name, final double newRight)
      throws IlpSolverException {
    try {
      model.set(DoubleAttr.RHS, new GRBConstr[] {constraints.get(name)}, new double[] {newRight});
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void setConstraintRights(final Map<String, Double> changeConstraintRight) {
    final double[] weights = new double[changeConstraintRight.size()];
    final GRBConstr[] constrs = new GRBConstr[changeConstraintRight.size()];
    int i = 0;
    for (final Entry<String, Double> entry : changeConstraintRight.entrySet()) {
      constrs[i] = constraints.get(entry.getKey());
      weights[i] = entry.getValue();
      i++;
    }
    try {
      model.set(DoubleAttr.RHS, constrs, weights);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void setSeed(final int seed) {
    try {
      model.set(IntParam.Seed, seed);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void setTimeout(final int parameterValue) {
    try {
      env.set(DoubleParam.TimeLimit, parameterValue);
      model.set(DoubleParam.TimeLimit, parameterValue);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void setVariableBounds(final Map<String, int[]> changeVariableBounds)
      throws IlpSolverException {
    for (final Entry<String, int[]> entry : changeVariableBounds.entrySet()) {
      changeVariableBounds(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
    }
  }

  @Override
  public void setVariableWeightForConstraint(final String name, final double weight,
      final String var) throws IlpSolverException {
    try {
      model.chgCoeff(constraints.get(name), getVariable(var), weight);
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public void setVariableWeights(final Map<String, Double> changeVariableWeights)
      throws IlpSolverException {
    for (final Entry<String, Double> entry : changeVariableWeights.entrySet()) {
      changeVariableWeight(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void setVariableWeightsForConstraints(
      final Map<String, Map<String, Double>> changeConstraitVariableWeights) {
    final List<GRBConstr> constrs = new LinkedList<>();
    final List<GRBVar> vars = new LinkedList<>();
    final List<Double> weights = new LinkedList<>();
    for (final Entry<String, Map<String, Double>> entry : changeConstraitVariableWeights
        .entrySet()) {
      for (final Entry<String, Double> entry2 : entry.getValue().entrySet()) {
        constrs.add(constraints.get(entry.getKey()));
        vars.add(variables.get(entry2.getKey()));
        weights.add(entry2.getValue());
      }
    }

    try {
      model.chgCoeffs(constrs.toArray(new GRBConstr[constrs.size()]),
          vars.toArray(new GRBVar[vars.size()]), weights.stream().mapToDouble(d -> d).toArray());
    } catch (final GRBException | NullPointerException e) {
      throw new IlpSolverException(e);
    }
  }

  @Override
  public Statistics solve() throws IlpSolverException {
    try {
      synchronized (vals) {
        for (int i = 0; i < vals.length; i++) {
          vals[i] = -1;
        }
      }
      // final long beforeUpdate = System.nanoTime();
      model.update();
      // GenericMethodHelper.printToConsole("Start ILP Solving.");
      // model.tune();
      final long start = System.nanoTime();
      model.optimize();
      // final long endOptimize = System.nanoTime();
      // TempDataStore.getInstance()
      // .setIlpModelUpdateRuntime((start - beforeUpdate) / Consts.NANO_TO_MILLI);
      // TempDataStore.getInstance()
      // .setIlpModelOptimizeRuntime((endOptimize - start) / Consts.NANO_TO_MILLI);
      synchronized (vals) {
        SolverStatus status;
        if (model.get(GRB.IntAttr.Status) == GRB.UNBOUNDED) {
          status = SolverStatus.UNBOUNDED;
        } else if (model.get(GRB.IntAttr.Status) == GRB.INF_OR_UNBD) {
          status = SolverStatus.INF_OR_UNBD;
        } else if (model.get(GRB.IntAttr.Status) == GRB.INFEASIBLE) {
          status = SolverStatus.INFEASIBLE;
        } else if (model.get(GRB.IntAttr.Status) == GRB.OPTIMAL) {
          status = SolverStatus.OPTIMAL;
        } else if (model.get(GRB.IntAttr.Status) == GRB.TIME_LIMIT) {
          // System.err.println("Warning: time limit (" + model.get(GRB.DoubleParam.TimeLimit)
          // + "s) reached! " + model.get(GRB.IntAttr.SolCount) + " solutions were found so far.");
          // GenericMethodHelper.printToConsole("Warning: time limit reached! "
          // + model.get(GRB.IntAttr.SolCount) + " solutions were found so far.");
          status = SolverStatus.TIME_OUT;
        } else {
          throw new RuntimeException("Unknown solver status.");
        }
        // GenericMethodHelper.printToConsole("Finished ILP Solving.");

        return new Statistics(status, System.nanoTime() - start, (vals[2] - start), (int) vals[0],
            (int) vals[1]);
      }
    } catch (final GRBException e) {
      throw new IlpSolverException(e);
    }
  }

}
