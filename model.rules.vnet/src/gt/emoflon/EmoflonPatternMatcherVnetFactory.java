package gt.emoflon;

import facade.ModelFacade;
import patternmatching.IncrementalPatternMatcher;
import patternmatching.IncrementalPatternMatcherFactory;

public class EmoflonPatternMatcherVnetFactory extends IncrementalPatternMatcherFactory {

  @Override
  public IncrementalPatternMatcher create() {
    return new EmoflonPatternMatcherVnet(ModelFacade.getInstance().getRoot());
  }

}
