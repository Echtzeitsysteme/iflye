package algorithms;

import algorithms.ilp.VneIlpPathAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithm;
import algorithms.pm.VnePmMdvneAlgorithmMigration;

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
    TOTAL_PATH_COST, // [1]
    TOTAL_COMMUNICATION_COST_A, // [2]
    TOTAL_COMMUNICATION_COST_B, // [3,4] (One hop = 1 cost, no node costs)
    TOTAL_COMMUNICATION_COST_C, // [5] (One hop = 1 cost, with node costs)
    TOTAL_COMMUNICATION_COST_D, // [6] (One hop = 1 cost, with node costs)
    TOTAL_TAF_COMMUNICATION_COST; // [7]

    /*
     * [1] Tomaszek S., Leblebici E., Wang L., Schürr A. (2018) Virtual Network Embedding: Reducing
     * the Search Space by Model Transformation Techniques. In: Rensink A., Sánchez Cuadrado J.
     * (eds) Theory and Practice of Model Transformation. ICMT 2018. Lecture Notes in Computer
     * Science, vol 10888. Springer, Cham
     * 
     * [2] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
     * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
     * 
     * [3] Meng, Xiaoqiao, Vasileios Pappas, and Li Zhang. "Improving the scalability of data center
     * networks with traffic-aware virtual machine placement." 2010 Proceedings IEEE INFOCOM. IEEE,
     * 2010.
     * 
     * [4] M. G. Rabbani, R. P. Esteves, M. Podlesny, G. Simon, L. Z. Granville and R. Boutaba, "On
     * tackling virtual data center embedding problem," 2013 IFIP/IEEE International Symposium on
     * Integrated Network Management (IM 2013), 2013, pp. 177-184.
     * 
     * [5] As in [3,4] but with node cost that decreases if a substrate server gets filled up.
     * 
     * [6] As in [5] but with inverted node cost.
     * 
     * [7] Zeng, D., Guo, S., Huang, H., Yu, S., and Leung, V. C.M., “Optimal VM Placement in Data
     * Centers with Architectural and Resource Constraints,” International Journal of Autonomous and
     * Adaptive Communications Systems, vol. 8, no. 4, pp. 392–406, 2015.
     */
  }

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
   * Private constructor ensures no instantiation of this class.
   */
  private AlgorithmConfig() {}

  /**
   * Objective goal for the algorithms {@link VneIlpPathAlgorithm} and {@link VnePmMdvneAlgorithm}.
   */
  public static Objective obj = Objective.TOTAL_COMMUNICATION_COST_C;

  /**
   * Embedding mechanism for the {@link VnePmMdvneAlgorithm}
   */
  public static Embedding emb = Embedding.EMOFLON_WO_UPDATE;

  /**
   * If true, the {@link VnePmMdvneAlgorithm} will create additional SOS1 constraints for each
   * virtual element to map. This may give the ILP solver a head-start to solve the problem.
   */
  public static boolean pmSosEnabled = true;

  /**
   * Number of how often the {@link VnePmMdvneAlgorithmMigration} should try to achieve a valid
   * embedding while using the migration functionality. Must at least be set to 1.
   */
  public static int pmNoMigrations = 6;

  /**
   * If true, the network rejection cost will be calculated based on the size of the virtual network
   * and its resources. If false, the cost will be static.
   */
  public static boolean netRejCostDynamic = false;

}
