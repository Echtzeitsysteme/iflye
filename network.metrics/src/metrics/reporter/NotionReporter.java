package metrics.reporter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import iflye.dependencies.logging.IflyeLogger;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import metrics.MetricTransformer;
import metrics.Reporter;
import metrics.manager.MetricsManager;

/**
 * NotionReporter is a reporter that sends metrics to a Notion database. It uses
 * the Notion API to create pages in the database.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class NotionReporter extends GroupByTagValueReporter implements Reporter {

	/**
	 * Logger for system outputs.
	 */
	protected final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * The HTTP client used to send requests to the Notion API.
	 */
	protected final HttpClient notionClient = HttpClient.newHttpClient();

	/**
	 * The ID of the current series of metrics used to group them.
	 */
	protected String seriesId;

	/**
	 * The token used to authenticate with the Notion API.
	 */
	protected final String token;

	/**
	 * The ID of the database used to store the parent series.
	 */
	protected final String seriesDatabaseId;

	/**
	 * The ID of the database used to store the metrics.
	 */
	protected final String metricDatabaseId;

	/**
	 * The map of formatters used to format the properties of the metrics. The key
	 * is the property name and the value is the formatter.
	 */
	protected final Map<String, PropertyFormat> properties = new HashMap<>();

	/**
	 * A default configuration of the NotionReporter with preset property formats.
	 */
	public static class Default extends NotionReporter {
		public Default(final String token, final String seriesDatabaseId, final String metricDatabaseId) {
			super(token, seriesDatabaseId, metricDatabaseId);

			this.addPropertyFormat("Name", PROPERTY_TYPE.TITLE);
			this.addPropertyFormat("Series", PROPERTY_TYPE.RELATION);
			this.addPropertyFormat("substrate network", PROPERTY_TYPE.SELECT);
			this.addPropertyFormat("virtual network", PROPERTY_TYPE.SELECT);
			this.addPropertyFormat("algorithm", PROPERTY_TYPE.SELECT);
			this.addPropertyFormat("started", new PropertyFormat() {
				@Override
				public String type(String key, String value) {
					return "date";
				}

				@Override
				public String label(String key, String value) {
					return "Series Date"; // Renaming the key to a more human-readable format
				}
			});
			this.addPropertyFormat("objective", PROPERTY_TYPE.SELECT);
			this.addPropertyFormat("lastVNR", PROPERTY_TYPE.TEXT);
			this.addPropertyFormat("series uuid", PROPERTY_TYPE.SELECT);
			this.addPropertyFormat("exception", PROPERTY_TYPE.MULTI_SELECT);
			this.addPropertyFormat("series group uuid", PROPERTY_TYPE.SELECT);
			this.addPropertyFormat("gips.solver_threads", PROPERTY_TYPE.NUMBER);
		}
	}

	/**
	 * Creates a new NotionReporter with the given token and database IDs.
	 * 
	 * @param token            the token used to authenticate with the Notion API
	 * @param seriesDatabaseId the ID of the database used to store the parent
	 *                         series
	 * @param metricDatabaseId the ID of the database used to store the metrics
	 */
	public NotionReporter(final String token, final String seriesDatabaseId, final String metricDatabaseId) {
		super();

		this.token = token;
		this.metricDatabaseId = metricDatabaseId;
		this.seriesDatabaseId = seriesDatabaseId;
		
		IflyeLogger.configureLogging(logger);
	}

	/**
	 * Enhance the MetricTransformer with a method to provide a proper format
	 * instruction for Notion.
	 * 
	 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
	 */
	public static interface NotionMeter extends MetricTransformer {

		/**
		 * Provides a property format for the given meter and value.
		 * 
		 * @param meter the meter to format
		 * @param key   the key of the property
		 * @param value the value of the property
		 * @return the property format
		 */
		public PropertyFormat getNotionPropertyFormat(Meter meter, String key, Object value);

	}

	/**
	 * The PropertyFormat interface is used to format the properties of the metrics.
	 * 
	 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
	 */
	@FunctionalInterface
	public static interface PropertyFormat {

		/**
		 * Formats the property with the given key, type and value.
		 * 
		 * @param key   the key of the property
		 * @param value the value of the property
		 * @return the formatted property
		 * @see #label(String, String)
		 * @see #type(String, String)
		 * @see #value(String, String)
		 */
		default public String format(final String key, final String value) {
			return "\"" + label(key, value) + "\": { \"" + type(key, value) + "\": " + value(key, value) + " }";
		}

		/**
		 * Returns the label of the property.
		 * 
		 * @param key   the key of the property
		 * @param value the value of the property
		 * @return the label of the property
		 */
		default public String label(final String key, final String value) {
			return key;
		}

		/**
		 * Returns the formatted value of the property, according to the type.
		 * 
		 * @param key   the key of the property
		 * @param value the value of the property
		 * @return the formatted value of the property
		 * @see #type(String, String)
		 * @see #format(String, String)
		 */
		default public String value(final String key, final String value) {
			switch (type(key, value)) {
			case "select":
				return "{ \"name\": \"" + value.replace(",", ";") + "\" }";
			case "multi_select":
				return "[" + String.join(", ", Arrays.asList(value.split("\s*,\s*")).stream()
						.map((i) -> "{ \"name\": \"" + i + "\" }").toList()) + "]";
			case "date":
				return "{ \"start\": \"" + value + "\" }";
			case "title":
				return "[{ \"text\": { \"content\": \"" + value + "\" } }]";
			case "rich_text":
				return "[{ \"text\": { \"content\": \"" + value + "\" } }]";
			case "relation":
				return "[{ \"id\": \"" + value + "\" }]";
			case "number":
				return String.valueOf(value);
			default:
				return "\"" + value + "\"";
			}
		}

		/**
		 * Returns the Notion type of the property.
		 * 
		 * @param key   the key of the property
		 * @param value the value of the property
		 * @return the type of the property
		 * @see #value(String, String)
		 * @see #format(String, String)
		 */
		public String type(final String key, final String value);

	}

	/**
	 * The most commonly used property types in Notion.
	 * 
	 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
	 */
	public static enum PROPERTY_TYPE implements PropertyFormat {
		SELECT {
			@Override
			public String type(String key, String value) {
				return "select";
			}
		},
		MULTI_SELECT {
			@Override
			public String type(String key, String value) {
				return "multi_select";
			}
		},
		DATE {
			@Override
			public String type(String key, String value) {
				return "date";
			}
		},
		TITLE {
			@Override
			public String type(String key, String value) {
				return "title";
			}
		},
		TEXT {
			@Override
			public String type(String key, String value) {
				return "rich_text";
			}
		},
		RELATION {
			@Override
			public String type(String key, String value) {
				return "relation";
			}
		},
		NUMBER {
			@Override
			public String type(String key, String value) {
				return "number";
			}
		}
	}

	/**
	 * Assigns a property format to the given key.
	 * 
	 * @param key    the key of the property
	 * @param format the property format
	 * @return this NotionReporter instance
	 */
	public NotionReporter addPropertyFormat(final String key, final PropertyFormat format) {
		this.properties.put(key, format);
		return this;
	}

	/**
	 * Prepare a new API request with the token and the given url.
	 * 
	 * @param url the url of the request
	 * @return the prepared request
	 * @see #postMetric(String, Collection)
	 */
	protected HttpRequest.Builder prepareRequest(final String url) {
		return HttpRequest.newBuilder().header("Authorization", "Bearer " + this.token)
				.header("Notion-Version", "2022-06-28").header("Content-Type", "application/json").uri(URI.create(url));
	}

	/**
	 * Initializes the NotionReporter by creating a new series in the Notion
	 * database. The series is used to group the metrics together and will have all
	 * provided tags for quick filtering.
	 * 
	 * @see #flushEntry(Map, Map)
	 */
	@Override
	public void initialized() {
		if (this.token == null || this.seriesDatabaseId == null) {
			return;
		}

		Map<String, String> tags = MetricsManager.getInstance().getTags().stream()
				.collect(Collectors.toMap(Tag::getKey, Tag::getValue));
		final String name = this.getSeriesDate(tags) + ": " + getName(tags);

		tags.putIfAbsent("Name", name);
		Collection<String> properties = formatTags(tags).values();

		try {
			HttpResponse<String> response = this.postMetric(this.seriesDatabaseId, properties);

			Matcher matcher = Pattern.compile(".*?\"id\":\\s*\"([\\p{XDigit}-]{36})\",.*?").matcher(response.body());
			matcher.matches();
			this.seriesId = matcher.group(1);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Flushes a metrics entry to the notion database. It will have all tags
	 * assigned as properties if there was a format defined. If there is an active
	 * series ID, it will be used to create a relation to the series.
	 * 
	 * @param entry    the entry to flush
	 * @param groupKey the group key of the entry
	 * @see #initialized()
	 */
	@Override
	protected void flushEntry(GroupedReporter.Entry entry, String groupKey) {
		if (this.token == null || this.metricDatabaseId == null) {
			return;
		}

		getPersistedTags(entry.tags()).entrySet()
				.forEach((tag) -> entry.values().putIfAbsent(tag.getKey(), tag.getValue()));

		if (entry.values().isEmpty()) {
			return;
		}

		final String name = this.getSeriesDate(entry.tags()) + ": "
				+ String.valueOf(entry.tags().getOrDefault("lastVNR", "")) + " (" + getName(entry.tags()) + ")";
		final List<String> properties = new ArrayList<>();
		if (this.formatTag("Name", name) != null) {
			properties.add(this.formatTag("Name", name));
		}
		if (this.seriesId != null && this.formatTag("Series", this.seriesId) != null) {
			properties.add(this.formatTag("Series", this.seriesId));
		}
		properties.addAll(entry.values().values().stream().map((e) -> String.valueOf(e)).toList());

		try {
			this.postMetric(this.metricDatabaseId, properties);
		} catch (IOException | InterruptedException | NoSuchElementException e) {
			// Don't propagate error to prevent failing the entire series
			logger.warning("Failed to write to Notion DB!");
			logger.warning("Entry was:");
			logger.warning(properties.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Collects the metrics for the given meter and formats them as their notion
	 * property format.
	 * 
	 * @param metric           the metric to collect
	 * @param meterTransformer the metric transformer to use
	 * @param entry            the entry to add the metric to
	 * @param meter            the meter to add the metric for
	 * @return true if the entry was modified, false otherwise
	 */
	@Override
	protected boolean collectEntry(final Map<String, Object> metric, final MetricTransformer meterTransformer,
			final GroupedReporter.Entry entry, final Meter meter) {
		if (this.token == null || this.metricDatabaseId == null) {
			// Short-circuiting if there is no connection configured
			return true;
		}

		boolean found = false;
		for (Map.Entry<String, Object> value : metric.entrySet()) {
			NotionMeter notionMeter = (meterTransformer instanceof NotionMeter) ? (NotionMeter) meterTransformer : null;
			PropertyFormat format = this.properties.getOrDefault(value.getKey(),
					notionMeter != null ? notionMeter.getNotionPropertyFormat(meter, value.getKey(), value.getValue())
							: null);
			if (format == null) {
				continue;
			}

			entry.values().put(value.getKey(), format.format(value.getKey(), String.valueOf(value.getValue())));
			found = true;
		}

		return found;
	}

	/**
	 * Returns the tags of the given meter as a map of their corresponding Notion
	 * property format.
	 * 
	 * @param meter the meter to get the tags for
	 * @return a map of tags as their corresponding Notion property format
	 * @see #formatTags(Map)
	 * @see #getTags(Meter)
	 */
	protected Map<String, String> getPersistedTags(final Map<String, String> tags) {
		return formatTags(tags);
	}

	/**
	 * Filter and format the given tags as their corresponding Notion property
	 * format.
	 * 
	 * @param tags the tags to format
	 * @return a map of tags as their corresponding Notion property format
	 */
	protected Map<String, String> formatTags(final Map<String, String> tags) {
		return tags.entrySet().stream().filter((tag) -> this.properties.containsKey(tag.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, (tag) -> this.formatTag(tag.getKey(), tag.getValue())));
	}

	/**
	 * Sends a POST request to the Notion API to create a new page in the database.
	 * 
	 * @param targetDatabaseId the ID of the database to create the page in
	 * @param properties       the properties of the page
	 * @return the response of the request
	 * @throws IOException            if an I/O error occurs
	 * @throws InterruptedException   if the request is interrupted
	 * @throws NoSuchElementException if the request fails
	 * @see #prepareRequest(String)
	 * @see #flushEntry(Map, Map)
	 * @see #initialized()
	 */
	protected HttpResponse<String> postMetric(final String targetDatabaseId, final Collection<String> properties)
			throws IOException, InterruptedException {
		final String body = "{\n" + "	\"parent\": { \"database_id\": \"" + targetDatabaseId + "\" },\n"
				+ "	\"properties\": {\n" + String.join(",\n", properties) + "\n" + "	},\n" + "	\"children\": []\n"
				+ "}";

		HttpRequest request = prepareRequest("https://api.notion.com/v1/pages").POST(BodyPublishers.ofString(body))
				.build();

		HttpResponse<String> response = this.notionClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new NoSuchElementException("Failed to write metric to the Notion DB with status "
					+ response.statusCode() + "! Response: " + response.body());
		}

		return response;
	}

	/**
	 * Returns the name of the series based on the given tags.
	 * 
	 * @param tags the tags to get the name from
	 * @return the name of the series
	 */
	protected String getName(Map<String, String> tags) {
		if (tags.containsKey("name")) {
			return tags.get("name");
		} else {
			return tags.get("substrate network") + "/" + tags.get("virtual network") + "/" + tags.get("implementation");
		}
	}

	/**
	 * Returns the date of the series based on the given tags.
	 * 
	 * @param tags the tags to get the date from
	 * @return the date of the series
	 */
	protected String getSeriesDate(Map<String, String> tags) {
		return OffsetDateTime.parse(tags.get("started")).truncatedTo(ChronoUnit.SECONDS)
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	/**
	 * Formats the given key and value as their corresponding Notion property
	 * format.
	 * 
	 * @param key   the key of the property
	 * @param value the value of the property
	 * @return the formatted property
	 */
	protected String formatTag(final String key, final String value) {
		if (!this.properties.containsKey(key)) {
			return null;
		}

		return this.properties.get(key).format(key, value);
	}

}
