package statistics;

/**
 * Utility class for the statistics project.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class StatisticUtils {

  /**
   * Private constructor ensures no object instantiation.
   */
  private StatisticUtils() {}

  /**
   * Assembled one line for the output CSV files with the statistics. Takes a counter (= line
   * number), the array of doubles for the mean, and an array of double for the standard derivation
   * and combines them to an array of strings in this form:
   * 
   * {counter, mean[0], stddev[0], mean[1], stddev[1], ...}
   * 
   * @param counter Line counter.
   * @param mean Array of doubles that defines the mean values.
   * @param stddev Array of doubles that defines the standard derivation values.
   * @return Array of string as described above.
   */
  static String[] assembleCsvLine(final int counter, final double[] mean, final double[] stddev) {
    if (mean.length != stddev.length) {
      throw new IllegalArgumentException("Array length differs.");
    }

    final String[] output = new String[mean.length * 2 + 1];
    output[0] = String.valueOf(counter);

    for (int i = 0; i < mean.length * 2; i += 2) {
      output[i + 1] = String.valueOf(mean[i / 2]);
      output[i + 2] = String.valueOf(stddev[i / 2]);
    }

    return output;
  }

  /**
   * Calculates the mean for a given double array.
   * 
   * @param values Array of doubles.
   * @return Mean.
   */
  static double mean(final double[] values) {
    if (values == null) {
      throw new IllegalArgumentException("Argument was null.");
    }

    double adds = 0;
    for (int i = 0; i < values.length; i++) {
      adds += values[i];
    }

    return adds / values.length;
  }

  /**
   * Calculates the standard derivation for a given double array.
   * 
   * @param values Array of doubles.
   * @return Standard derivation.
   */
  static double stdDev(final double[] values) {
    final double mean = mean(values);
    double val = 0;

    for (int i = 0; i < values.length; i++) {
      val += Math.pow(values[i] - mean, 2) / values.length;
    }

    return Math.sqrt(val);
  }

}
