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
   * Migration configuration that should be used by the {@link VnePmMdvneAlgorithm}.
   * 
   * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [1].
   * 
   * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
   * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
   *
   * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
   * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
   */
  public enum Migration {
    /**
     * Once a virtual element was embedded, it can never be migrated to a different substrate
     * element. If the mapping is not possible anymore (substrate element was removed), all future
     * embeddings will fail.
     */
    NEVER,
    /**
     * Once a virtual element was embedded, it can only be migrated to a different substrate
     * element, if the mapping is not possible anymore (substrate element was removed). If the
     * embedding is invalid for another reason, the embedding will fail.
     */
    MAPPING_REMOVED,
    /**
     * Virtual elements can be migrated during all future embeddings, but migrations are discouraged
     * by incurring a penalty to the objective value.
     */
    ALWAYS_PENALTY,
    /**
     * Virtual elements can be migrated during all future embeddings without any penalty.
     */
    ALWAYS_FREE;
  }

  /**
   * Private constructor ensures no instantiation of this class.
   */
  private AlgorithmConfig() {}

  /**
   * Objective goal.
   */
  public static Objective obj = Objective.TOTAL_PATH_COST;

  /**
   * Migration setting.
   */
  public static Migration mig = Migration.NEVER;

}
