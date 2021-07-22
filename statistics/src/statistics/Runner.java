package statistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import scenario.util.CsvUtil;

/**
 * Runner class to combine multiple CSV files containing measurements to one statistic CSV file
 * containing the mean and the standard derivation.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class Runner {

  /**
   * Private constructor ensures no object instantiation.
   */
  private Runner() {}

  /**
   * Main method to start the runner. Argument most contain the base name of the experiment to load
   * files from, e.g., 'pm_fat-tree-4-pods_l3_k2'.
   * 
   * @param args
   */
  public static void main(final String[] args) {
    if (args == null || args.length < 1) {
      throw new IllegalArgumentException("Please specify the experiment name to load files from.");
    }

    final String expName = args[0];
    final List<List<Double[]>> data = new LinkedList<List<Double[]>>();

    // TODO: Find a way to dynamically determine upper limit of i (e.g., by using 'find')
    for (int i = 1; i <= 3; i++) {
      final List<Double[]> line = CsvUtil.loadCsvFile(expName + "_run" + i + ".csv");
      data.add(line);
    }

    final String outputName = expName + "_stats.csv";

    // Currently, the number of metrics is hardcoded against CsvUtil.java
    double[] outputMean = new double[17];
    double[] outputStdDev = new double[17];

    // Iterate over all lines of the files
    for (int v = 0; v < data.get(0).size(); v++) {
      // Iterate over all metrics
      for (int i = 0; i < 17; i++) {
        final Double[] values = new Double[data.size()];

        // Iterate over the data sets (= files)
        for (int f = 0; f < data.size(); f++) {
          values[f] = data.get(f).get(v)[i];
        }

        // Calculate values
        outputMean[i] = StatisticUtils.mean(values);
        outputStdDev[i] = StatisticUtils.stdDev(values);
      }

      // Write line to statistic CSV file
      final String[] line = StatisticUtils.assembleCsvLine(v, outputMean, outputStdDev);
      CsvUtil.appendCsvStatsLine(outputName, line);
    }

    //
    // Time summing
    //

    final Map<String, Double[]> timeSums = new HashMap<String, Double[]>();
    timeSums.put("time_total", new Double[3]);
    timeSums.put("time_pm", new Double[3]);
    timeSums.put("time_ilp", new Double[3]);
    timeSums.put("time_deploy", new Double[3]);
    timeSums.put("time_rest", new Double[3]);

    for (final String key : timeSums.keySet()) {
      for (int i = 0; i < timeSums.get(key).length; i++) {
        timeSums.get(key)[i] = 0.0;
      }
    }

    for (int i = 0; i < 3; i++) {
      for (int line = 0; line < data.get(i).size(); line++) {
        timeSums.get("time_pm")[i] += data.get(i).get(line)[0];
        timeSums.get("time_ilp")[i] += data.get(i).get(line)[1];
        timeSums.get("time_deploy")[i] += data.get(i).get(line)[2];
        timeSums.get("time_rest")[i] += data.get(i).get(line)[3];
        timeSums.get("time_total")[i] += //
            data.get(i).get(line)[0] //
                + data.get(i).get(line)[1] //
                + data.get(i).get(line)[2] //
                + data.get(i).get(line)[3];
      }
    }

    final String[] sums = new String[10];
    sums[0] = String.valueOf(StatisticUtils.mean(timeSums.get("time_total")));
    sums[1] = String.valueOf(StatisticUtils.stdDev(timeSums.get("time_total")));
    sums[2] = String.valueOf(StatisticUtils.mean(timeSums.get("time_pm")));
    sums[3] = String.valueOf(StatisticUtils.stdDev(timeSums.get("time_pm")));
    sums[4] = String.valueOf(StatisticUtils.mean(timeSums.get("time_ilp")));
    sums[5] = String.valueOf(StatisticUtils.stdDev(timeSums.get("time_ilp")));
    sums[6] = String.valueOf(StatisticUtils.mean(timeSums.get("time_deploy")));
    sums[7] = String.valueOf(StatisticUtils.stdDev(timeSums.get("time_deploy")));
    sums[8] = String.valueOf(StatisticUtils.mean(timeSums.get("time_rest")));
    sums[9] = String.valueOf(StatisticUtils.stdDev(timeSums.get("time_rest")));

    CsvUtil.createCsvTimeSumFile(expName + "_timesums.csv", sums);

    System.out.println("=> Finished statistics file: " + outputName);
  }

}
