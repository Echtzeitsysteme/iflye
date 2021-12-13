package statistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import scenario.util.CsvUtil;

/**
 * Runner class to combine multiple CSV files containing measurements to one
 * statistic CSV file containing the mean and the standard derivation.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class Runner {

	// TODO: Find a way to dynamically determine upper limit (e.g., by using 'find')
	/**
	 * Number of experiments (= runs).
	 */
	private static final int NUM_OF_EXPS = 3;

	/**
	 * Private constructor ensures no object instantiation.
	 */
	private Runner() {
	}

	/**
	 * Main method to start the runner. Argument must contain the base name of the
	 * experiment to load metric files from, e.g., 'pm_fat-tree-4-pods_l3_k2'.
	 *
	 * @param args Arguments to parse, i.e., args[0] must hold the experiment's name
	 *             to load.
	 */
	public static void main(final String[] args) {
		if (args == null || args.length < 1) {
			throw new IllegalArgumentException("Please specify the experiment name to load files from.");
		}

		final String expName = args[0];

		//
		// Statistics CSV file
		//

		final List<List<Double[]>> data = new LinkedList<>();

		for (int i = 1; i <= NUM_OF_EXPS; i++) {
			final List<Double[]> line = CsvUtil.loadCsvFile(expName + "_run" + i + ".csv");
			data.add(line);
		}

		final String outputName = expName + "_stats.csv";

		// Currently, the number of metrics is hard-coded against CsvUtil.java
		double[] outputMean = new double[18];
		double[] outputStdDev = new double[18];

		// Iterate over all lines of the files
		for (int v = 0; v < data.get(0).size(); v++) {
			// Iterate over all metrics
			for (int i = 0; i < 18; i++) {
				final Double[] values = new Double[data.size()];

				// Iterate over the data sets (= files)
				for (int f = 0; f < data.size(); f++) {
					values[f] = data.get(f).get(v)[i];
				}

				// Calculate values
				outputMean[i] = StatisticUtils.mean(values);
				outputStdDev[i] = StatisticUtils.stdDev(values);

				// If the metric is the last one -> Check if rounding of time_total and
				// time_total_stddev is
				// necessary
				if (i == 17) {
					outputMean[i] = StatisticUtils.roundTimetotal(outputMean[i]);
					outputStdDev[i] = StatisticUtils.roundTimetotalstddev(outputStdDev[i]);
				}
			}

			// Write line to statistic CSV file
			final String[] line = StatisticUtils.assembleCsvLine(StatisticConfig.VN_NAME_OFFSET ? v + 1 : v, outputMean,
					outputStdDev);
			CsvUtil.appendCsvStatsLine(outputName, line);
		}

		//
		// Time summing CSV file
		//

		final Map<String, Double[]> timeSums = new HashMap<>();
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

		for (int i = 0; i < NUM_OF_EXPS; i++) {
			for (Double[] element : data.get(i)) {
				timeSums.get("time_pm")[i] += element[0];
				timeSums.get("time_ilp")[i] += element[1];
				timeSums.get("time_deploy")[i] += element[2];
				timeSums.get("time_rest")[i] += element[3];
				timeSums.get("time_total")[i] += //
						element[0] //
								+ element[1] //
								+ element[2] //
								+ element[3];
			}
		}

		final String[] line = StatisticUtils.assembleTimeSumCsvLine(timeSums);
		CsvUtil.createCsvTimeSumFile(expName + "_timesums.csv", line);

		System.out.println("=> Finished statistics file: " + outputName);
	}

}
