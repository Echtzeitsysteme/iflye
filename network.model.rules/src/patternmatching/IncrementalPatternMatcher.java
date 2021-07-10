package patternmatching;

public interface IncrementalPatternMatcher {

  void dispose();

  PatternMatchingDelta run();

}
