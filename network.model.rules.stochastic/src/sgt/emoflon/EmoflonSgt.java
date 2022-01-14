package sgt.emoflon;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.ibex.gt.api.GraphTransformationRule;

import gt.IncrementalPatternMatcher;
import gt.PatternMatchingConfig;
import gt.PatternMatchingDelta;
import model.Root;
import network.model.rules.stochastic.api.StochasticAPI;
import network.model.rules.stochastic.api.StochasticApp;

public class EmoflonSgt implements IncrementalPatternMatcher {
	
	protected final StochasticApp app;
	protected final StochasticAPI api;
	
	final public static String NETWORK_MATCH_NAME = "embedNetwork";
	final public static String SERVER_MATCH_NAME = "serverMatchPositive";
	final public static String SERVER_BACKUP_MATCH_NAME = "serverMatchPositiveBackup";
	final public static String SWITCH_MATCH_NAME = "switchNodeMatchPositive";
	final public static String PATH2LINK_MATCH_NAME = "linkPathMatchPositive";
	final public static String SERVER2LINK_MATCH_NAME = "linkServerMatchPositive";
	
	final Map<String, GraphTransformationRule<?,?>> mappingRules;
	
	protected EmoflonSgt(final Root root) {
		app = switch (PatternMatchingConfig.pm) {
			case DEMOCLES -> throw new UnsupportedOperationException();
			case HIPE -> new EmoflonSgtHiPEApp(root);
			case VIATRA -> throw new UnsupportedOperationException();
			default -> throw new UnsupportedOperationException(); 
		};
		api = app.initAPI();
		mappingRules = initMappingRules();
	}
	
	protected Map<String, GraphTransformationRule<?,?>> initMappingRules() {
		Map<String, GraphTransformationRule<?,?>> mappingRules = new HashMap<>();
		mappingRules.put(NETWORK_MATCH_NAME, api.embedNetwork());
		mappingRules.put(SERVER_MATCH_NAME, api.serverMatchPositive());
		mappingRules.put(SERVER_BACKUP_MATCH_NAME, api.serverMatchPositiveBackup());
		mappingRules.put(SWITCH_MATCH_NAME, api.switchNodeMatchPositive());
		mappingRules.put(PATH2LINK_MATCH_NAME, api.linkPathMatchPositive());
		mappingRules.put(SERVER2LINK_MATCH_NAME, api.linkServerMatchPositive());
		
		return mappingRules;
	}
	
	public Map<String, GraphTransformationRule<?,?>> getMappingRules() {
		return mappingRules;
	}

	@Override
	public void dispose() {
		api.terminate();
	}

	@Override
	public PatternMatchingDelta run() {
		api.updateMatches();
		return null;
	}

}
