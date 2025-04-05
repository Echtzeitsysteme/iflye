package metrics.memory;

import java.io.File;
import java.util.List;

import org.unix4j.Unix4j;

import metrics.IMetric;
import metrics.MetricConsts;

/**
 * Memory PID metric implementation. This metric will call the system to get the
 * Java process PID and query proc for the highest used RAM value by this
 * process.
 *
 * Please keep in mind that this metric will slow down your program, as it has
 * to query for proc files on the system.
 *
 * Important: Currently, this metric is only able to run on GNU/Linux and/or
 * macOS.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class MemoryPidMetric implements IMetric {

	/**
	 * Measured value of maximum used RAM in kiB.
	 */
	private final long memory;

	/**
	 * Creates a new instance of this memory PID metric and saves the value.
	 * Creation time is equal to measurement time.
	 */
	public MemoryPidMetric() {
		// If on windows, skip this metric because it is not supported
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			this.memory = -1;
			return;
		}
		final File file = new File("/proc/" + getPid() + "/status");
		if (!file.exists()) {
			System.err.println("Proc file for MemoryPidMetric is not available!");
			this.memory = -1;
			return;
		}

		final List<String> lines = Unix4j.grep("VmHWM", file).toStringList();
		final String memComplex = lines.get(0);
		final String mem = memComplex.replaceAll("\\D+", "");
		long memory = -1L;
		try {
			memory = Long.valueOf(mem);
		} catch (final NumberFormatException ex) {
			System.err.println("Catched an exception while parsing the string: " + memComplex);
		} finally {
			this.memory = memory;
		}
	}

	@Override
	public double getValue() {
		return 1.0 * memory / MetricConsts.KIBIBYTE;
	}

	/**
	 * Returns the process ID (PID) of the current java process.
	 *
	 * @return Process ID (PID) of the current java process.
	 */
	private long getPid() {
		return ProcessHandle.current().pid();
	}

}
