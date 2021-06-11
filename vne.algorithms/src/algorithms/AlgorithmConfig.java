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
   * Objective goal for the algorithms {@link VneIlpPathAlgorithm} and {@link VnePmMdvneAlgorithm}.
   */
  public static Objective obj = Objective.TOTAL_PATH_COST;

  /**
   * Embedding enumeration that defines the embedding functionality of the
   * {@link VnePmMdvneAlgorithm}. Either it uses the calculates matches from emoflon or it uses
   * methods implemented in the model facade.
   * 
   * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
   */
  public enum Embedding {
    EMOFLON, // Emoflon PM as it is
    EMOFLON_WO_UPDATE, // Emoflon PM without the update functionality
    MANUAL; // Use ModelFacade methods
  }

  /**
   * Embedding mechanism for the {@link VnePmMdvneAlgorithm}
   */
  public static Embedding emb = Embedding.EMOFLON_WO_UPDATE;

  /**
   * If true, the {@link VnePmMdvneAlgorithm} will create additional SOS1 constraints for each
   * virtual element to map. This may give the ILP solver a head-start to solve the problem.
   */
  public static boolean pmSosEnabled = true;

}
