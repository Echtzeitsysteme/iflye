package statistics;

import java.util.LinkedList;
import java.util.List;
import scenario.util.CsvUtil;

public class Runner {

  public static void main(final String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException("Please specify the experiment name to load files from.");
    }

    final String expName = args[0];
    final List<List<Double[]>> data = new LinkedList<List<Double[]>>();

    for (int i = 1; i <= 3; i++) {
      final List<Double[]> line = CsvUtil.loadCsvFile(expName + "_run" + i + ".csv");
      data.add(line);
    }

    final String outputName = expName + "_stats.csv";

    double[] outputMean = new double[17];
    double[] outputStdDev = new double[17];

    for (int v = 0; v < data.get(0).size(); v++) {
      for (int i = 0; i < 17; i++) {
        final double[] values = new double[3];

        values[0] = data.get(0).get(v)[i];
        values[1] = data.get(1).get(v)[i];
        values[2] = data.get(2).get(v)[i];

        outputMean[i] = mean(values);
        outputStdDev[i] = stdDev(values);
      }

      final String[] line = assembleCsvLine(v, outputMean, outputStdDev);
      CsvUtil.appendCsvStatsLine(outputName, line);
    }

    System.out.println("=> Finished statistics file: " + outputName);
  }

  private static String[] assembleCsvLine(final int counter, final double[] mean,
      final double[] stddev) {
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

  private static double mean(final double[] values) {
    double adds = 0;
    for (int i = 0; i < values.length; i++) {
      adds += values[i];
    }

    return adds / values.length;
  }

  private static double stdDev(final double[] values) {
    final double mean = mean(values);
    double val = 0;

    for (int i = 0; i < values.length; i++) {
      val += Math.pow(values[i] - mean, 2) / values.length;
    }

    return Math.sqrt(val);
  }

}
