package algorithms;

import algorithms.ilp.VneIlpPathAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;

/**
 * Configuration of the algorithms {@link VneIlpPathAlgorithm} and {@link VnePmMdvneAlgorithm}.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class AlgorithmConfig {

  /**
   * Objective that defines which cost function should be used by the algorithms
   * {@link VneIlpPathAlgorithm} and {@link VnePmMdvneAlgorithm}.
   * 
   * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
   */
  public enum Objective {
    TOTAL_PATH_COST, TOTAL_COMMUNICATION_COST_A, TOTAL_COMMUNICATION_COST_B;
  }

  /**
   * Private constructor ensures no instantiation of this class.
   */
  private AlgorithmConfig() {}

  /**
   * Objective goal.
   */
  public static Objective obj = Objective.TOTAL_PATH_COST;

}
