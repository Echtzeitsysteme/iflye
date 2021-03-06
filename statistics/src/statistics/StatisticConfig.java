package statistics;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Configuration of the statistic module.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class StatisticConfig {

	/**
	 * Private constructor ensures no instantiation.
	 */
	private StatisticConfig() {
	}

	/**
	 * If true, the names of the virtual networks will start at 1 instead of 0 for
	 * all *_stats.csv files.
	 */
	public static final boolean VN_NAME_OFFSET = true;

	/**
	 * If true, the values of the *_timesums.csv will be rounded.
	 */
	public static final boolean ROUND_TIMESUMS = true;

	/**
	 * Rounding decimal format setting for the time sums.
	 */
	public static final DecimalFormat ROUND_TIMESUMS_FORMAT = ((DecimalFormat) NumberFormat
			.getNumberInstance(new Locale("en", "US")));

	/**
	 * If true, the time-based total values (time_total, time_total_stddev) of the
	 * *_stats.csv will be rounded.
	 */
	public static final boolean ROUND_TIMETOTAL_STATS = true;

	/**
	 * Rounding decimal format setting for the time_total column in *_stats.csv.
	 */
	public static final DecimalFormat ROUND_TIMETOTAL_FORMAT = new DecimalFormat("0.000");

	/**
	 * Rounding decimal format setting for the time_total_stddev column in
	 * *_stats.csv.
	 */
	public static final DecimalFormat ROUND_TIMETOTALSTDDEV_FORMAT = new DecimalFormat("0.0000");

	/*
	 * Apply pattern to time sums format
	 */
	static {
		ROUND_TIMESUMS_FORMAT.applyPattern("0.00");
	}

}
