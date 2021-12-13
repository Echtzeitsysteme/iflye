package metrics;

import facade.ModelFacade;

/**
 * Interface for metrics.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public interface IMetric {

	/**
	 * ModelFacade to get model properties from.
	 */
	public final ModelFacade facade = ModelFacade.getInstance();

	/**
	 * Returns the value of a specific metric.
	 *
	 * @return Value of the specific metric.
	 */
	public double getValue();

}
