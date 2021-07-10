package gt.emoflon;

import facade.ModelFacade;
import gt.IncrementalPatternMatcherFactory;

public class EmoflonGtRackFactory extends IncrementalPatternMatcherFactory {

  @Override
  public gt.IncrementalPatternMatcher create() {
    return new EmoflonGtRack(ModelFacade.getInstance().getRoot());
  }

}
