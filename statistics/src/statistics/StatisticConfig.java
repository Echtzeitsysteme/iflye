package statistics;

/**
 * Configuration of the statistic module.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public interface StatisticConfig {

  /**
   * If true, the names of the virtual networks will start at 1 instead of 0 for all *_stats.csv
   * files.
   */
  public static final boolean VN_NAME_OFFSET = false;

}
