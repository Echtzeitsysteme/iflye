package patternmatching.emoflon;

import facade.ModelFacade;
import patternmatching.IncrementalPatternMatcher;
import patternmatching.IncrementalPatternMatcherFactory;

public class EmoflonPatternMatcherFactory extends IncrementalPatternMatcherFactory {

  @Override
  public IncrementalPatternMatcher create() {
    return new EmoflonPatternMatcher(ModelFacade.getInstance().getRoot());
  }

}
