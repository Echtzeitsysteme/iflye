package metrics.memory;

import java.io.File;
import java.util.List;
import org.unix4j.Unix4j;
import metrics.IMetric;
import metrics.MetricConsts;

/**
 * Memory PID metric implementation. This metric will call the system to get the Java process PID
 * and query proc for the highest used RAM value by this process.
 * 
 * Please keep in mind that this metric will slow down your program, as it has to query for proc
 * files on the system.
 * 
 * Important: Currently, this metric is only able to run on GNU/Linux and/or macOS.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class MemoryPidMetric implements IMetric {

  /**
   * Measured value of maximum used RAM in kiB.
   */
  private final long memory;

  /**
   * Creates a new instance of this memory PID metric and saves the value. Creation time is equal to
   * measurement time.
   */
  public MemoryPidMetric() {
    final File file = new File("/proc/" + getPid() + "/status");
    final List<String> lines = Unix4j.grep("VmHWM", file).toStringList();
    final String memComplex = lines.get(0);
    final String mem =
        memComplex.substring(memComplex.indexOf(" ")).replace(" ", "").replace("kB", "");
    long memory = -1L;
    try {
      memory = Long.valueOf(mem);
    } catch (final NumberFormatException ex) {
      System.err.println("Catched an exception while parsing the string: " + memComplex);
      System.err.println("Attaching the complete proc status of my PID:");
      lines.addAll(Unix4j.cat(file).toStringList());
      lines.forEach(l -> System.err.println(l));
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
