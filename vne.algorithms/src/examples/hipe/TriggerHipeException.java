package examples.hipe;

import patternmatching.IncrementalPatternMatcher;
import patternmatching.emoflon.EmoflonPatternMatcherFactory;

/**
 * Minimal example to trigger an exception from/with HiPE.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TriggerHipeException {

  /**
   * Incremental pattern matcher to use.
   */
  private static IncrementalPatternMatcher patternMatcher;

  /**
   * Main method that triggers an exception (at least on my system).
   * 
   * @param args Arguments that will be ignored.
   */
  public static void main(final String[] args) {
    patternMatcher = new EmoflonPatternMatcherFactory().create();
    patternMatcher.run();
    System.out.println("Finished.");
  }

}
