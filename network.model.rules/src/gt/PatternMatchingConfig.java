package gt;

/**
 * Configuration of the emoflon pattern matcher.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class PatternMatchingConfig {

	/**
	 * Private constructor ensures no instantiation of this class.
	 */
	private PatternMatchingConfig() {
	}

	/**
	 * Pattern matcher instance to choose.
	 *
	 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
	 */
	public enum PatternMatcher {
		HIPE, DEMOCLES, VIATRA;
	}

	/**
	 * Pattern matching mechanism for the VnePmMdvneAlgorithm.
	 */
	public static PatternMatcher pm = PatternMatcher.HIPE;

}
