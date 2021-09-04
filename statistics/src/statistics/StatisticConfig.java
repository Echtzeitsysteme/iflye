package statistics;

import java.text.DecimalFormat;

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
  public static final boolean VN_NAME_OFFSET = true;

  /**
   * If true, the values of the *_timesums.csv will be rounded.
   */
  public static final boolean ROUND_TIMESUMS = true;

  /**
   * Rounding decimal format setting.
   */
  public static final DecimalFormat ROUND_FORMAT = new DecimalFormat("0.00");

}
