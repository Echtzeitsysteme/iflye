package gt.emoflon;

import facade.ModelFacade;
import gt.IncrementalPatternMatcher;
import gt.IncrementalPatternMatcherFactory;

public class EmoflonGtVnetFactory extends IncrementalPatternMatcherFactory {

  @Override
  public IncrementalPatternMatcher create() {
    return new EmoflonGtVnet(ModelFacade.getInstance().getRoot());
  }

}
