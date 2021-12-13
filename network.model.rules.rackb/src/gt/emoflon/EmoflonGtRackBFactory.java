package gt.emoflon;

import facade.ModelFacade;
import gt.IncrementalPatternMatcherFactory;

public class EmoflonGtRackBFactory extends IncrementalPatternMatcherFactory {

	@Override
	public gt.IncrementalPatternMatcher create() {
		return new EmoflonGtRackB(ModelFacade.getInstance().getRoot());
	}

}
