package gt;

public interface IncrementalPatternMatcher {

  void dispose();

  PatternMatchingDelta run();

}
