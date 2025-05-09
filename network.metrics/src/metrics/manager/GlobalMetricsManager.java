package metrics.manager;

import metrics.MetricConfig;
import metrics.memory.MemoryDetailedMetric;
import metrics.memory.MemoryPidMetric;
import metrics.time.RuntimeDetailedMetric;

/**
 * Global metrics manager. This class can be used to check in metrics and get
 * them back later on.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class GlobalMetricsManager {

	/**
	 * Global time measurement array.
	 */
	private static final double[] globalTimeMeasurement = new double[5];

	/**
	 * Global runtime detailed metric.
	 */
	static RuntimeDetailedMetric rt;

	/**
	 * Global memory detailed metric.
	 */
	static MemoryDetailedMetric mm;

	/**
	 * Private constructor ensures no instantiation of this class.
	 */
	private GlobalMetricsManager() {
	}

	public static MetricsManager accessor() {
		return MetricsManager.getInstance();
	}

	/**
	 * Starts the complete runtime measurement.
	 */
	@Deprecated
	public static void startRuntime() {
		if (accessor() != null) {
			throw new UnsupportedOperationException(
					"Starting the runtime using GlobalMetricsManager is not supported anymore! Please use MetricsManager#observe instead.");
		}

		if (rt != null) {
			throw new UnsupportedOperationException("RuntimeDetailedMetric was already created!");
		}

		rt = new RuntimeDetailedMetric();
	}

	/**
	 * Returns the runtime metric.
	 *
	 * @return Runtime metric.
	 */
	@Deprecated
	public static RuntimeDetailedMetric getRuntime() {
		if (accessor() != null) {
			throw new UnsupportedOperationException(
					"Timing using GlobalMetricsManager is not supported anymore! Please use MetricsManager#observe instead.");
		}

		return rt;
	}

	/**
	 * Sets a start point for the ILP time measurement.
	 */
	@Deprecated
	public static void startIlpTime() {
		if (rt != null) {
			rt.startIlpTime();

			if (accessor() != null) {
				accessor().start("ilp", Context.IlpStepContext::new);
			}
		}
	}

	/**
	 * Sets an end point for the ILP time measurement and increments the global ILP
	 * time value.
	 */
	@Deprecated
	public static void endIlpTime() {
		if (rt != null) {
			rt.endIlpTime();

			if (accessor() != null) {
				accessor().stop();
			}
		}
	}

	/**
	 * Sets a start point for the PM time measurement.
	 */
	@Deprecated
	public static void startPmTime() {
		if (rt != null) {
			rt.startPmTime();

			if (accessor() != null) {
				accessor().start("pm", Context.PmStepContext::new);
			}
		}
	}

	/**
	 * Sets an end point for the PM time measurement and increments the global PM
	 * time value.
	 */
	@Deprecated
	public static void endPmTime() {
		if (rt != null) {
			rt.endPmTime();

			if (accessor() != null) {
				accessor().stop();
			}
		}
	}

	/**
	 * Sets a start point for the deploy time measurement.
	 */
	@Deprecated
	public static void startDeployTime() {
		if (rt != null) {
			rt.startDeployTime();

			if (accessor() != null) {
				accessor().start("deploy", Context.DeployStepContext::new);
			}
		}
	}

	/**
	 * Sets an end point for the deploy time measurement and increments the global
	 * deploy time value.
	 */
	@Deprecated
	public static void endDeployTime() {
		if (rt != null) {
			rt.endDeployTime();

			if (accessor() != null) {
				accessor().stop();
			}
		}
	}

	/**
	 * Stops the global runtime measurement.
	 */
	@Deprecated
	public static void stopRuntime() {
		if (accessor() != null) {
			throw new UnsupportedOperationException(
					"Stopping the runtime using GlobalMetricsManager is not supported anymore! Please use MetricsManager#stop instead.");
		}

		if (rt != null) {
			rt.stop();
		}
	}

	/**
	 * Resets the global runtime measurement.
	 */
	@Deprecated
	public static void resetRuntime() {
		if (accessor() != null) {
			throw new UnsupportedOperationException(
					"Timing using GlobalMetricsManager is not supported anymore! Please use MetricsManager#observe instead.");
		}

		globalTimeMeasurement[0] += rt.getValue();
		globalTimeMeasurement[1] += rt.getPmValue();
		globalTimeMeasurement[2] += rt.getIlpValue();
		globalTimeMeasurement[3] += rt.getDeployValue();
		globalTimeMeasurement[4] += rt.getRestValue();
		rt = null;
	}

	/**
	 * Returns the global captured time array.
	 *
	 * @return Global captured time array.
	 */
	@Deprecated
	public static double[] getGlobalTimeArray() {
		if (accessor() != null) {
			throw new UnsupportedOperationException(
					"Timing using GlobalMetricsManager is not supported anymore! Please use MetricsManager#observe instead.");
		}

		return globalTimeMeasurement;
	}

	/**
	 * Triggers a memory measurement.
	 *
	 * @return Index of the new measurement.
	 */
	@Deprecated
	public static int measureMemory() {
		if (mm == null) {
			mm = new MemoryDetailedMetric();
		}

		if (!MetricConfig.ENABLE_MEMORY) {
			return -1;
		}

		accessor().event("memory");

		return mm.capture();
	}

	@Deprecated
	public static int dummyMemory() {
		if (mm == null) {
			mm = new MemoryDetailedMetric();
		}

		if (!MetricConfig.ENABLE_MEMORY) {
			return -1;
		}

		accessor().event("memory");

		return mm.dummy();
	}

	/**
	 * Resets the global memory measurement.
	 */
	@Deprecated
	public static void resetMemory() {
		mm = null;
	}

	/**
	 * Returns the captured memory detailed metric.
	 *
	 * @return Captured memory detailed metric.
	 */
	@Deprecated
	public static MemoryDetailedMetric getMemory() {
		return mm;
	}

	/**
	 * Returns the maximum amount of memory (RAM) used by the running Java process
	 * in MiB.
	 *
	 * @return Maximum amount of memory (RAM) used by the running Java process in
	 *         MiB.
	 */
	@Deprecated
	public static double getMemoryPid() {
		return new MemoryPidMetric().getValue();
	}

}
