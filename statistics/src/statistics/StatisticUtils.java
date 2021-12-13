package statistics;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * Utility class for the statistics project.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class StatisticUtils {

	/**
	 * Private constructor ensures no object instantiation.
	 */
	private StatisticUtils() {
	}

	/**
	 * Assembles one line for the output CSV files with the statistics. Takes a
	 * counter (= line number), the array of doubles for the mean, and an array of
	 * double for the standard derivation and combines them to an array of strings
	 * in this form:
	 *
	 * {counter, mean[0], stddev[0], mean[1], stddev[1], ...}
	 *
	 * @param counter Line counter.
	 * @param mean    Array of doubles that defines the mean values.
	 * @param stddev  Array of doubles that defines the standard derivation values.
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
	 * Assembles one line for the output CSV file with time summing.
	 *
	 * @param timeSums Mapping of time values to double array.
	 * @return String that represents the line of the output CSV file.
	 */
	static String[] assembleTimeSumCsvLine(final Map<String, Double[]> timeSums) {
		final String[] sums = new String[10];
		sums[0] = String.valueOf(roundTimesums(StatisticUtils.mean(timeSums.get("time_total"))));
		sums[1] = String.valueOf(roundTimesums(StatisticUtils.stdDev(timeSums.get("time_total"))));
		sums[2] = String.valueOf(roundTimesums(StatisticUtils.mean(timeSums.get("time_pm"))));
		sums[3] = String.valueOf(roundTimesums(StatisticUtils.stdDev(timeSums.get("time_pm"))));
		sums[4] = String.valueOf(roundTimesums(StatisticUtils.mean(timeSums.get("time_ilp"))));
		sums[5] = String.valueOf(roundTimesums(StatisticUtils.stdDev(timeSums.get("time_ilp"))));
		sums[6] = String.valueOf(roundTimesums(StatisticUtils.mean(timeSums.get("time_deploy"))));
		sums[7] = String.valueOf(roundTimesums(StatisticUtils.stdDev(timeSums.get("time_deploy"))));
		sums[8] = String.valueOf(roundTimesums(StatisticUtils.mean(timeSums.get("time_rest"))));
		sums[9] = String.valueOf(roundTimesums(StatisticUtils.stdDev(timeSums.get("time_rest"))));
		return sums;
	}

	/**
	 * Calculates the mean for a given double array.
	 *
	 * @param values Array of doubles.
	 * @return Mean.
	 */
	static double mean(final Double[] values) {
		if (values == null) {
			throw new IllegalArgumentException("Argument was null.");
		}

		double adds = 0;
		for (Double value : values) {
			adds += value;
		}

		return adds / values.length;
	}

	/**
	 * Calculates the standard derivation for a given double array.
	 *
	 * @param values Array of doubles.
	 * @return Standard derivation.
	 */
	static double stdDev(final Double[] values) {
		final double mean = mean(values);
		double val = 0;

		for (Double value : values) {
			val += Math.pow(value - mean, 2) / values.length;
		}

		return Math.sqrt(val);
	}

	/**
	 * Rounds the given double value (time sum) according to the modules
	 * configuration.
	 *
	 * @param value Input value.
	 * @return Output value (rounded if configured).
	 */
	static double roundTimesums(final double value) {
		return round(value, StatisticConfig.ROUND_TIMESUMS, StatisticConfig.ROUND_TIMESUMS_FORMAT);
	}

	/**
	 * Rounds the given double value (total time) according to the modules
	 * configuration.
	 *
	 * @param value Input value.
	 * @return Output value (rounded if configured).
	 */
	static double roundTimetotal(final double value) {
		return round(value, StatisticConfig.ROUND_TIMETOTAL_STATS, StatisticConfig.ROUND_TIMETOTAL_FORMAT);
	}

	/**
	 * Rounds the given double value (total time standard deviation) according to
	 * the modules configuration.
	 *
	 * @param value Input value.
	 * @return Output value (rounded if configured).
	 */
	static double roundTimetotalstddev(final double value) {
		return round(value, StatisticConfig.ROUND_TIMETOTAL_STATS, StatisticConfig.ROUND_TIMETOTALSTDDEV_FORMAT);
	}

	/**
	 * Actual rounding method.
	 *
	 * @param value  Input double value.
	 * @param round  If true, value gets rounded.
	 * @param format DecimalFormat that configures the decimal places.
	 * @return Rounded value
	 */
	private static double round(final double value, final boolean round, final DecimalFormat format) {
		return round //
				? Double.valueOf(format.format(value)) //
				: value;
	}

}
