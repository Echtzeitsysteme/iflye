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
	private CsvUtil() {
	}

	/**
	 * Counter for the number of lines within the CSV output file.
	 */
	protected static int csvCounter = 0;

	/**
	 * CSV file header format for normal runs (simulation).
	 */
	private static CSVFormat formatNormal = CSVFormat.DEFAULT.withHeader("counter", "timestamp", "lastVNR", "time_pm",
			"time_ilp", "time_deploy", "time_rest", "accepted_vnrs", "total_path_cost", "average_path_length",
			"total_communication_cost_a", "total_communication_cost_b", "total_communication_cost_c",
			"total_communication_cost_d", "total_taf_communication_cost", "operation_cost", "memory_start",
			"memory_ilp", "memory_end", "memory_pid_max");

	/**
	 * CSV file header format for mean and standard derivation (after simulation).
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
			"memory_pid_max", "memory_pid_max_stddev", //
			"time_total", "time_total_stddev" //
	);

	/**
	 * CSV file header format for summing up the time of an experiment (after
	 * simulation).
	 */
	private static CSVFormat formatTimeSum = CSVFormat.DEFAULT.withHeader( //
			"time_total", "time_total_stddev", //
			"time_pm", "time_pm_stddev", //
			"time_ilp", "time_ilp_stddev", //
			"time_deploy", "time_deploy_stddev", //
			"time_rest", "time_rest_stddev" //
	);

	/**
	 * Appends the current state of the metrics to the CSV file.
	 *
	 * @param lastVnr The Name of the last embedded virtual network (request).
	 * @param csvPath Path for the CSV file.
	 * @param sNet    Substrate network to export metrics for.
	 */
	public static void appendCsvLine(final String lastVnr, final String csvPath, final SubstrateNetwork sNet) {
		final String[] content = new String[20];
		content[0] = String.valueOf(csvCounter++); // line counter
		content[1] = String.valueOf(java.time.LocalDateTime.now()); // time stamp
		content[2] = String.valueOf(lastVnr); // name of the last embedded virtual network
		content[3] = String.valueOf(GlobalMetricsManager.getRuntime().getPmValue() / MetricConsts.NANO_TO_MILLI); // PM
		content[4] = String.valueOf(GlobalMetricsManager.getRuntime().getIlpValue() / MetricConsts.NANO_TO_MILLI); // ILP
		content[5] = String.valueOf(GlobalMetricsManager.getRuntime().getDeployValue() / MetricConsts.NANO_TO_MILLI); // Deploy
		content[6] = String.valueOf(GlobalMetricsManager.getRuntime().getRestValue() / MetricConsts.NANO_TO_MILLI); // Rest
		content[7] = String.valueOf((int) new AcceptedVnrMetric(sNet).getValue());
		content[8] = String.valueOf(new TotalPathCostMetric(sNet).getValue());
		content[9] = String.valueOf(new AveragePathLengthMetric(sNet).getValue());
		content[10] = String.valueOf(new TotalCommunicationCostMetricA(sNet).getValue());
		content[11] = String.valueOf(new TotalCommunicationCostMetricB(sNet).getValue());
		content[12] = String.valueOf(new TotalCommunicationCostMetricC(sNet).getValue());
		content[13] = String.valueOf(new TotalCommunicationCostMetricD(sNet).getValue());
		content[14] = String.valueOf(new TotalTafCommunicationCostMetric(sNet).getValue());
		content[15] = String.valueOf(new OperatingCostMetric(sNet).getValue());
		content[16] = String.valueOf(GlobalMetricsManager.getMemory().getValue(0)); // Memory start
																					// execute
		content[17] = String.valueOf(GlobalMetricsManager.getMemory().getValue(1)); // Memory before ILP
		content[18] = String.valueOf(GlobalMetricsManager.getMemory().getValue(2)); // Memory end
																					// execute
		content[19] = String.valueOf(GlobalMetricsManager.getMemoryPid()); // Maximum amount of memory
																			// (RAM) consumed
		writeCsvLine(csvPath, formatNormal, content);
	}

	/**
	 * Loads the CSV file for the given path and parses its metric values to a list
	 * (lines) of double arrays (columns). Please notice that this method has
	 * hard-coded values for the specific metric implementation.
	 *
	 * @param csvPath Path for the metric CSV file to load.
	 * @return List of double arrays with the metrics.
	 */
	public static List<Double[]> loadCsvFile(final String csvPath) {
		final List<Double[]> metrics = new LinkedList<>();

		try {
			final CSVParser parser = new CSVParser(new FileReader(csvPath), formatNormal);
			final List<CSVRecord> recs = parser.getRecords();
			for (int i = 1; i < recs.size(); i++) {
				final Double[] val = new Double[18];
				final CSVRecord rec = recs.get(i);
				for (int j = 3; j <= 19; j++) {
					val[j - 3] = Double.valueOf(rec.get(j));
				}

				// Sum time metrics up
				val[17] = val[0] // time_pm
						+ val[1] // time_ilp
						+ val[2] // time_deploy
						+ val[3]; // time_rest

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
	 * @param csvPath   Path for the statistics CSV file.
	 * @param statsLine Line to add to the statistics CSV file.
	 */
	public static void appendCsvStatsLine(final String csvPath, final String[] statsLine) {
		writeCsvLine(csvPath, formatStats, statsLine);
	}

	/**
	 * Creates a CSV file with the header and one line of the summed time values.
	 *
	 * @param csvPath   Path for the time sum CSV file.
	 * @param statsLine Line to add to the time sume CSV file.
	 */
	public static void createCsvTimeSumFile(final String csvPath, final String[] statsLine) {
		writeCsvLine(csvPath, formatTimeSum, statsLine);
	}

	/**
	 * Method that actually writes the CSV file.
	 *
	 * @param csvPath Path for the CSV file to create or append line.
	 * @param format  Chosen header format of the CSV file.
	 * @param content Content of the line to add as string array.
	 */
	private static void writeCsvLine(final String csvPath, final CSVFormat format, final String[] content) {
		// If file path is null, do not create a file at all
		if (csvPath == null) {
			return;
		}

		try {
			BufferedWriter out;
			// If file does not exist, write header to it
			if (Files.notExists(Path.of(csvPath))) {
				out = Files.newBufferedWriter(Paths.get(csvPath), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
				try (final CSVPrinter printer = new CSVPrinter(out, format)) {
					printer.close();
				}
			}

			out = Files.newBufferedWriter(Paths.get(csvPath), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
			try (final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
				printer.printRecord((Object[]) content);
				printer.close();
			}
			out.close();
		} catch (final IOException e) {
			// TODO: Error handling
			e.printStackTrace();
		}
	}

}
