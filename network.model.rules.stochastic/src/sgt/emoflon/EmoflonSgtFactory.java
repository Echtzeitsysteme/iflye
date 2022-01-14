package sgt.emoflon;

import facade.ModelFacade;
import gt.IncrementalPatternMatcher;
import gt.IncrementalPatternMatcherFactory;

public class EmoflonSgtFactory extends IncrementalPatternMatcherFactory {

	@Override
	public IncrementalPatternMatcher create() {
		return new EmoflonSgt(ModelFacade.getInstance().getRoot());
	}

}
