package metrics;

/**
 * Constant factors for all metrics.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public interface MetricConsts {

	/**
	 * Calculation factor from NANO to MILLI.
	 */
	public static final long NANO_TO_MILLI = 1_000_000_000;

	/**
	 * Calculation factor from BYTE to KIBIBYTE.
	 */
	public static final long KIBIBYTE = 1024L;

	/**
	 * Calculation factor from BYTE to MIBIBYTE.
	 */
	public static final long MEBIBYTE = 1024L * KIBIBYTE;

}
