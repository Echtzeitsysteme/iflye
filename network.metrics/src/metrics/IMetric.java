package metrics;

/**
 * Interface for metrics.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public interface IMetric {

	/**
	 * Returns the value of a specific metric.
	 *
	 * @return Value of the specific metric.
	 */
	public double getValue();

}
