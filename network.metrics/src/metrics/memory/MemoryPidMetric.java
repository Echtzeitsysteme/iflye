package metrics.memory;

import java.io.File;
import java.util.List;
import org.unix4j.Unix4j;
import metrics.IMetric;

/**
 * Memory PID metric implementation. This metric will call the system to get the Java process PID
 * and query proc for the highest used RAM value by this process.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class MemoryPidMetric implements IMetric {

  private final long memory;
  private static final long KILOBYTE = 1024L;

  public MemoryPidMetric() {
    // final Runtime rt = Runtime.getRuntime();
    // try {
    // // final Process proc = rt.exec("/bin/bash -c grep VmHWM /proc/" + getPid() + "/status");
    // // final String[] cmd = {"/bin/bash", "-c", "'grep VmHWM /proc/" + getPid() + "/status'"};
    // final String[] cmd = {"/bin/bash", "-c", "cat /etc/hosts"};
    // // final Process proc = rt.exec("/bin/bash -c 'grep VmHWM /proc/" + getPid() + "/status'");
    // final Process proc = rt.exec(cmd);
    // // final Process proc = rt.exec("/bin/echo hello");
    // // value = rt.exec("grep VmHWM /proc/" + getPid() + "/status").getInputStream().toString();
    //
    // final BufferedReader stdOutStream =
    // new BufferedReader(new InputStreamReader(proc.getInputStream()));
    // value = stdOutStream.readLine();
    // // value = proc.getInputStream().toString();
    //
    // stdOutStream.close();
    // } catch (final IOException e) {
    // // e.printStackTrace();
    // value = "";
    // }
    //
    // System.out.println("===> DEBUG: " + value);

    final File file = new File("/proc/" + getPid() + "/status");
    final List<String> lines = Unix4j.grep("VmHWM", file).toStringList();
    final String memComplex = lines.get(0);
    final String mem =
        memComplex.substring(memComplex.indexOf(" ")).replace(" ", "").replace("kB", "");
    memory = Long.valueOf(mem);
  }

  @Override
  public double getValue() {
    // return 1.0 * memory / MEGABYTE;
    // return -1;
    return 1.0 * memory / KILOBYTE;
  }

  private long getPid() {
    return ProcessHandle.current().pid();
  }

}
