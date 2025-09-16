package gt.emoflon;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.emoflon.ibex.gt.api.GraphTransformationMatch;

import gt.IncrementalPatternMatcher;
import gt.PatternMatchingConfig;
import gt.PatternMatchingDelta;
import gt.emoflon.apps.EmoflonGtRackBHiPEApp;
import model.Element;
import model.Root;
import model.SubstrateElement;
import model.VirtualElement;
import network.model.rules.rackb.api.RackbAPI;
import network.model.rules.rackb.api.RackbApp;
import network.model.rules.rackb.api.matches.LinkPathMatchPositiveMatch;
import network.model.rules.rackb.api.matches.LinkPathMatchServerServerMatch;
import network.model.rules.rackb.api.matches.LinkServerMatchPositiveMatch;
import network.model.rules.rackb.api.matches.ServerMatchPositiveMatch;
import network.model.rules.rackb.api.matches.SwitchMatchPositiveMatch;

/**
 * Implementation of the {@link IncrementalPatternMatcher} for eMoflon.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class EmoflonGtRackB implements IncrementalPatternMatcher {

	/**
	 * Rack Rules API object generated from graph transformation patterns.
	 */
	private final RackbAPI api;

	/**
	 * Wrapper that initializes the API object.
	 */
	private final RackbApp emoflonPatternMatcherApp;

	/**
	 * Current state of the delta. Must be updated in every iteration.
	 */
	private PatternMatchingDelta currentDelta = new PatternMatchingDelta();

	/**
	 * Map for matches: (Virtual) element to (substrate) element.
	 */
	private final UnifiedMap<Element, List<Element>> virtualMatches = new UnifiedMap<Element, List<Element>>();

	/**
	 * Map for GT matches: Tuple of virtual and substrate element to
	 * GraphTransformationMatch.
	 */
	private final UnifiedMap<Tuple, GraphTransformationMatch<?, ?>> tupleToGtMatch = new UnifiedMap<Tuple, GraphTransformationMatch<?, ?>>();

	/**
	 * Constructor that initializes the object for a given root node.
	 *
	 * @param root Root node to work with.
	 */
	public EmoflonGtRackB(final Root root) {
		switch (PatternMatchingConfig.pm) {
		case HIPE:
			emoflonPatternMatcherApp = new EmoflonGtRackBHiPEApp(root);
			break;
		default:
			throw new UnsupportedOperationException();
		}

		api = emoflonPatternMatcherApp.initAPI();

		/*
		 * Matches
		 */

		api.serverMatchPositive().subscribeAppearing(m -> {
			addMatch(currentDelta::addServerMatchPositive, m.getVirtualNode(), m.getSubstrateNode());
			tupleToGtMatch.put(new Tuple(m.getVirtualNode(), m.getSubstrateNode()), m);
		});

		api.switchMatchPositive().subscribeAppearing(m -> {
			addMatch(currentDelta::addSwitchMatchPositive, m.getVirtualSwitch(), m.getSubstrateNode());
			tupleToGtMatch.put(new Tuple(m.getVirtualSwitch(), m.getSubstrateNode()), m);
		});

		api.linkPathMatchPositive().subscribeAppearing(m -> {
			addMatch(currentDelta::addLinkPathMatchPositive, m.getVirtualLink(), m.getSubstratePath());
			tupleToGtMatch.put(new Tuple(m.getVirtualLink(), m.getSubstratePath()), m);
		});

		api.linkPathMatchServerServer().subscribeAppearing(m -> {
			addMatch(currentDelta::addLinkPathMatchPositive, m.getVirtualLink(), m.getSubstratePath());
			tupleToGtMatch.put(new Tuple(m.getVirtualLink(), m.getSubstratePath()), m);
		});

		api.linkServerMatchPositive().subscribeAppearing(m -> {
			addMatch(currentDelta::addLinkServerMatchPositive, m.getVirtualLink(), m.getSubstrateServer());
			tupleToGtMatch.put(new Tuple(m.getVirtualLink(), m.getSubstrateServer()), m);
		});
	}

	/**
	 * Applies a match for a given virtual and substrate element (creates the
	 * embedding).
	 *
	 * @param virt     Virtual element to embed.
	 * @param sub      Substrate element to embed on.
	 * @param doUpdate True if PM should do updates.
	 */
	public void apply(final VirtualElement virt, final SubstrateElement sub, final boolean doUpdate) {
		final GraphTransformationMatch<?, ?> match = tupleToGtMatch.get(new Tuple(virt, sub));
		if (match instanceof ServerMatchPositiveMatch) {
			api.serverMatchPositive().apply((ServerMatchPositiveMatch) match, doUpdate);
		} else if (match instanceof SwitchMatchPositiveMatch) {
			api.switchMatchPositive().apply((SwitchMatchPositiveMatch) match, doUpdate);
		} else if (match instanceof LinkServerMatchPositiveMatch) {
			api.linkServerMatchPositive().apply((LinkServerMatchPositiveMatch) match, doUpdate);
		} else if (match instanceof LinkPathMatchPositiveMatch) {
			api.linkPathMatchPositive().apply((LinkPathMatchPositiveMatch) match, doUpdate);
		} else if (match instanceof LinkPathMatchServerServerMatch) {
			api.linkPathMatchServerServer().apply((LinkPathMatchServerServerMatch) match, doUpdate);
		}
	}

	@Override
	public void dispose() {
		api.terminate();
	}

	@Override
	public PatternMatchingDelta run() {
		api.updateMatches();
		final PatternMatchingDelta old = currentDelta;
		currentDelta = new PatternMatchingDelta();
		return old;
	}

	/**
	 * Adds a match to the collection virtualMatches.
	 *
	 * @param deltaModification Modification (input).
	 * @param virtual           Virtual element for the mapping.
	 * @param substrate         Substrate element for the mapping.
	 */
	public void addMatch(final BiConsumer<Element, Element> deltaModification, final Element virtual,
			final Element substrate) {
		if (!virtualMatches.containsKey(virtual)) {
			virtualMatches.put(virtual, List.of(substrate));
		}
		deltaModification.accept(virtual, substrate);
	}

}
