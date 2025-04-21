package metrics.reporter;

import io.micrometer.core.instrument.Meter;

/**
 * A reporter that groups metrics by tags before flushing. This is useful for
 * reporters that should have all metrics with the same tags in the same entry.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public abstract class GroupByTagValueReporter extends GroupedReporter<String> {

	protected String groupTagKeyName;

	public GroupByTagValueReporter() {
		this("series group uuid");
	}

	public GroupByTagValueReporter(final String groupTagKeyName) {
		super();

		this.groupTagKeyName = groupTagKeyName;
	}

	public String getGroupTagKeyName() {
		return this.groupTagKeyName;
	}

	/**
	 * Gives the tags of the given meter as the key for the group that the meter
	 * should be assigned to.
	 * 
	 * @param meter The meter to get the tags from.
	 * @return The tags of the meter as a map.
	 */
	@Override
	protected String getGroupKey(final Meter meter) {
		return getTags(meter).get(this.groupTagKeyName);
	}
}
