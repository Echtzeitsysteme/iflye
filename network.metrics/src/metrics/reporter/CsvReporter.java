package metrics.reporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import metrics.Reporter;

/**
 * A reporter that writes metrics to a CSV file. The CSV file will be created if
 * it does not exist. If the file already exists, it will append new data to the
 * end of the file. Columns will be written according to the existing header or
 * a given default, and new columns will be added if they do not exist in the
 * file.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class CsvReporter extends GroupByTagValueReporter implements Reporter {

	/**
	 * The file to which the metrics will be written.
	 */
	protected final File outputFile;

	/**
	 * The separator used in the CSV file.
	 */
	protected String separator = ",";

	/**
	 * The headers of the CSV file. Initially, these are the default headers. If the
	 * file already exists, the headers will be read from the first line of the
	 * file. New headers will be added if they do not exist in the file. They will
	 * be written in the order they are added.
	 */
	protected List<String> headers = new ArrayList<>(List.of("level_0.counter", "timestamp", "lastVNR", "time_total",
			"time_prepare", "time_execute", "time_pm", "time_ilp", "time_deploy", "accepted_vnrs", "total_path_cost",
			"average_path_length", "total_communication_cost_a", "total_communication_cost_b",
			"total_communication_cost_c", "total_communication_cost_d", "total_communication_objective_c",
			"total_communication_objective_d", "total_taf_communication_cost", "operation_cost", "memory_total",
			"memory_prepare", "memory_execute"));

	/**
	 * The tags that will be persisted in the CSV file. Tags are filtered by this
	 * list before writing to the file.
	 */
	protected List<String> persistTags = new ArrayList<>(List.of("lastVNR", "substrate network", "algorithm",
			"virtual network", "started", "objective", "series uuid", "series group uuid", "exception"));

	/**
	 * Initializes a new CsvReporter with the given output file. The file will be
	 * created if it does not exist. If the file already exists, the headers will be
	 * read from the first line of the file.
	 */
	public CsvReporter(File outputFile) {
		super();

		this.outputFile = outputFile;
		if (this.outputFile.exists()) {
			try {
				List<String> existingLines = Files.readAllLines(this.outputFile.toPath());
				if (!existingLines.isEmpty()) {
					String[] originalHeaders = existingLines.get(0).split(this.separator);
					this.headers = new ArrayList<>(Arrays.asList(originalHeaders));
				}
			} catch (IOException _ignored) {
			}
		}
	}

	/**
	 * Gets the initial entry for the CSV file, based on the tags of the given meter
	 * which should be persisted.
	 * 
	 * @param meter The meter to get the tags from.
	 * @return A map of tags that will be persisted in the CSV file.
	 */
	protected Map<String, String> getPersistedTags(Map<String, String> tags) {
		return tags.entrySet().stream().filter((tag) -> persistTags.contains(tag.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Write the given entry to the CSV file. If the file does not exist, it will be
	 * created. If the file already exists, the entry will be appended to the end of
	 * the file. If the entry contains new headers, they will be added to the file.
	 * The order of the headers will be preserved.
	 * 
	 * @param entry    The entry to write to the CSV file.
	 * @param groupKey The key used to group this entry.
	 * @throws RuntimeException If an error occurs while writing to the file.
	 */
	@Override
	protected void flushEntry(GroupedReporter.Entry entry, String groupKey) {
		getPersistedTags(entry.tags()).entrySet()
				.forEach((tag) -> entry.values().putIfAbsent(tag.getKey(), tag.getValue()));

		if (entry.values().isEmpty()) {
			return;
		}

		final boolean hasUpdatedHeader = updateHeaders(entry.values().keySet());

		if (hasUpdatedHeader || !outputFile.exists()) {
			try {
				// We can't just change the first row but need to rewrite the file
				final List<String> updatedLines = updateFileEntries();
				updatedLines.add(buildCsvRow(this.headers, entry.values()));
				Files.write(this.outputFile.toPath(), updatedLines);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
				// Only append the new entry to the file
				writer.println(buildCsvRow(this.headers, entry.values()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Append new headers to the existing headers. If the header already exists, it
	 * will not be added again. New headers will be added alphabetically.
	 * 
	 * @param headers The headers to add to the existing headers.
	 * @return true if the headers were updated, false otherwise.
	 */
	private boolean updateHeaders(final Set<String> headers) {
		List<String> newHeaders = new LinkedList<>(headers);
		Collections.sort(newHeaders);
		boolean hasUpdatedHeader = false;

		for (String key : newHeaders) {
			if (!this.headers.contains(key)) {
				this.headers.add(key);
				hasUpdatedHeader = true;
			}
		}

		return hasUpdatedHeader;
	}

	/**
	 * Reads the existing lines of the CSV file and adds new columns from the
	 * headers. The existing lines are preserved and the new columns are added to
	 * the end of each row. The headers are set as first line.
	 * 
	 * @param existingHeaders The existing headers of the CSV file.
	 * @return A list of updated lines that could be written to the CSV file.
	 * @throws IOException If an error occurs while reading the file.
	 */
	private List<String> updateFileEntries() throws IOException {
		final List<String> updatedLines = new ArrayList<>();
		updatedLines.add(String.join(this.separator, this.headers));

		if (!outputFile.exists()) {
			return updatedLines;
		}

		List<String> existingLines = Files.readAllLines(this.outputFile.toPath());
		if (existingLines.isEmpty()) {
			return updatedLines;
		}

		// We need the order of the current headers to index the entries for the new row
		final String[] existingHeaders = existingLines.get(0).split(this.separator);

		for (int i = 1; i < existingLines.size(); i++) {
			// split with a limit of -1 to keep empty entries
			final String[] oldValues = existingLines.get(i).split(",", -1);
			final Map<String, Object> rowMap = new LinkedHashMap<>();

			for (int j = 0; j < existingHeaders.length; j++) {
				rowMap.put(existingHeaders[j], oldValues[j]);
			}
			updatedLines.add(buildCsvRow(this.headers, rowMap));
		}

		return updatedLines;
	}

	/**
	 * Builds a CSV row from the given header keys and row data.
	 * 
	 * @param headerKeys The keys of the headers.
	 * @param row        The row data.
	 * @return A CSV row as a string.
	 */
	private String buildCsvRow(Collection<String> headerKeys, Map<String, Object> row) {
		return headerKeys.stream().map((key) -> row.getOrDefault(key, "")).map(Object::toString)
				.collect(Collectors.joining(this.separator));
	}

}
