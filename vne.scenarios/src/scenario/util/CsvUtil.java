package scenario.util;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import metrics.MetricConsts;
import metrics.embedding.AcceptedVnrMetric;
import metrics.embedding.AveragePathLengthMetric;
import metrics.embedding.OperatingCostMetric;
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
   * TODO
   */
  private static CSVFormat format = CSVFormat.DEFAULT.withHeader("counter", "timestamp", "lastVNR",
      "time_pm", "time_ilp", "time_deploy", "time_rest", "accepted_vnrs", "total_path_cost",
      "average_path_length", "total_communication_cost_a", "total_communication_cost_b",
      "total_communication_cost_c", "total_communication_cost_d", "total_taf_communication_cost",
      "operation_cost", "memory_start", "memory_ilp", "memory_end", "memory_pid_max");

  /**
   * TODO
   */
  private static CSVFormat formatStats = CSVFormat.DEFAULT.withHeader("counter", //
      "time_pm", "time_pm_stddev", //
      "time_ilp", "time_ilp_stddev", //
      "time_deploy", "time_deploy_stddev", //
      "time_rest", "time_rest_stddev", //
      "accepted_vnrs", "accepted_vnrs_stddev", //
      "total_path_cost", "total_path_cost_stddev", //
      "average_path_length", "average_path_length_stddev", //
      "total_communication_cost_a", "total_communication_cost_a_stddev", //
      "total_communication_cost_b", "total_communication_cost_b_stddev", //
      "total_communication_cost_c", "total_communication_cost_c_stddev", //
      "total_communication_cost_d", "total_communication_cost_d_stddev", //
      "total_taf_communication_cost", "total_taf_communication_cost_stddev", //
      "operation_cost", "operation_cost_stddev", //
      "memory_start", "memory_start_stddev", //
      "memory_ilp", "memory_ilp_stddev", //
      "memory_end", "memory_end_stddev", //
      "memory_pid_max", "memory_pid_max_stddev"//
  );

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
        try (final CSVPrinter printer = new CSVPrinter(out, format)) {
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
            new OperatingCostMetric(sNet).getValue(), //
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

  /**
   * TODO!
   * 
   * @param csvPath
   * @return
   */
  public static List<Double[]> loadCsvFile(final String csvPath) {
    final List<Double[]> metrics = new LinkedList<Double[]>();

    try {
      final CSVParser parser = new CSVParser(new FileReader(csvPath), format);
      final List<CSVRecord> recs = parser.getRecords();
      for (int i = 1; i < recs.size(); i++) {
        final Double[] val = new Double[17];
        final CSVRecord rec = recs.get(i);
        for (int j = 3; j <= 19; j++) {
          val[j - 3] = Double.valueOf(rec.get(j));
        }
        metrics.add(val);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return metrics;
  }

  /**
   * Appends the given line to the statistics CSV file.
   * 
   * @param csvPath Path for the statistics CSV file.
   * @param statsLine Line to add to the statistics CSV file.
   */
  public static void appendCsvStatsLine(final String csvPath, final String[] statsLine) {
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
        try (final CSVPrinter printer = new CSVPrinter(out, formatStats)) {
          printer.close();
        }
      }

      out = Files.newBufferedWriter(Paths.get(csvPath), StandardOpenOption.APPEND,
          StandardOpenOption.CREATE);
      try (final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
        // TODO: Remove warning
        printer.printRecord(statsLine);
        printer.close();
      }
      out.close();
    } catch (final IOException e) {
      // TODO: Error handling
      e.printStackTrace();
    }
  }

}
