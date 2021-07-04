package scenario.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import metrics.MetricConsts;
import metrics.embedding.AcceptedVnrMetric;
import metrics.embedding.AveragePathLengthMetric;
import metrics.embedding.TotalCommunicationCostMetricA;
import metrics.embedding.TotalCommunicationCostMetricB;
import metrics.embedding.TotalCommunicationCostMetricC;
import metrics.embedding.TotalCommunicationCostMetricD;
import metrics.embedding.TotalPathCostMetric;
import metrics.embedding.TotalTafCommunicationCostMetric;
import metrics.manager.GlobalMetricsManager;
import model.SubstrateNetwork;

/**
 * CSV utilities for exporting the metrics.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class CsvUtil {

  /**
   * Private constructor ensures no instantiation of this class.
   */
  private CsvUtil() {}

  /**
   * Counter for the number of lines within the CSV output file.
   */
  protected static int csvCounter = 0;

  /**
   * Appends the current state of the metrics to the CSV file.
   * 
   * @param lastVnr The Name of the last embedded virtual network (request).
   * @param csvPath Path for the CSV file.
   * @param sNet Substrate network to export metrics for.
   */
  public static void appendCsvLine(final String lastVnr, final String csvPath,
      final SubstrateNetwork sNet) {
    // If file path is null, do not create a file at all
    if (csvPath == null) {
      return;
    }

    try {
      BufferedWriter out;
      // If file does not exist, write header to it
      if (Files.notExists(Path.of(csvPath))) {
        out = Files.newBufferedWriter(Paths.get(csvPath), StandardOpenOption.APPEND,
            StandardOpenOption.CREATE);
        try (final CSVPrinter printer = new CSVPrinter(out,
            CSVFormat.DEFAULT.withHeader("counter", "timestamp", "lastVNR", "time_pm", "time_ilp",
                "time_deploy", "time_rest", "accepted_vnrs", "total_path_cost",
                "average_path_length", "total_communication_cost_a", "total_communication_cost_b",
                "total_communication_cost_c", "total_communication_cost_d",
                "total_taf_communication_cost", "memory_start", "memory_ilp", "memory_end",
                "memory_pid_max"))) {
          printer.close();
        }
      }

      out = Files.newBufferedWriter(Paths.get(csvPath), StandardOpenOption.APPEND,
          StandardOpenOption.CREATE);
      try (final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
        printer.printRecord( //
            csvCounter++, // line counter
            java.time.LocalDateTime.now(), // time stamp
            lastVnr, // name of the last embedded virtual network
            GlobalMetricsManager.getRuntime().getPmValue() / MetricConsts.NANO_TO_MILLI, // PM
            GlobalMetricsManager.getRuntime().getIlpValue() / MetricConsts.NANO_TO_MILLI, // ILP
            GlobalMetricsManager.getRuntime().getDeployValue() / MetricConsts.NANO_TO_MILLI, // Deploy
            GlobalMetricsManager.getRuntime().getRestValue() / MetricConsts.NANO_TO_MILLI, // Rest
            (int) new AcceptedVnrMetric(sNet).getValue(), //
            new TotalPathCostMetric(sNet).getValue(), //
            new AveragePathLengthMetric(sNet).getValue(), //
            new TotalCommunicationCostMetricA(sNet).getValue(), //
            new TotalCommunicationCostMetricB(sNet).getValue(), //
            new TotalCommunicationCostMetricC(sNet).getValue(), //
            new TotalCommunicationCostMetricD(sNet).getValue(), //
            new TotalTafCommunicationCostMetric(sNet).getValue(), //
            GlobalMetricsManager.getMemory().getValue(0), // Memory start execute
            GlobalMetricsManager.getMemory().getValue(1), // Memory before ILP
            GlobalMetricsManager.getMemory().getValue(2), // Memory end execute
            GlobalMetricsManager.getMemoryPid() // Maximum amount of memory (RAM) consumed
        );
        printer.close();
      }
      out.close();
    } catch (final IOException e) {
      // TODO: Error handling
      e.printStackTrace();
    }
  }

}
