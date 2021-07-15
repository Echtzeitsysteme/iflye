package gt.emoflon;

import facade.ModelFacade;
import gt.IncrementalPatternMatcherFactory;

public class EmoflonGtRackAFactory extends IncrementalPatternMatcherFactory {

  @Override
  public gt.IncrementalPatternMatcher create() {
    return new EmoflonGtRackA(ModelFacade.getInstance().getRoot());
  }

}
