package gt.emoflon;

import facade.ModelFacade;
import gt.IncrementalPatternMatcher;
import gt.IncrementalPatternMatcherFactory;

public class EmoflonGtFactory extends IncrementalPatternMatcherFactory {

  @Override
  public IncrementalPatternMatcher create() {
    return new EmoflonGt(ModelFacade.getInstance().getRoot());
  }

}
