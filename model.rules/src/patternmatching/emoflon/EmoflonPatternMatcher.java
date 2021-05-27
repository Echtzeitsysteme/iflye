package patternmatching.emoflon;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import model.Element;
import model.Root;
import model.SubstrateElement;
import model.VirtualElement;
import patternmatching.IncrementalPatternMatcher;
import patternmatching.PatternMatchingDelta;
import rules.api.RulesAPI;
import rules.api.matches.LinkPathMatchPositiveMatch;
import rules.api.matches.LinkServerMatchPositiveMatch;
import rules.api.matches.ServerMatchPositiveMatch;
import rules.api.matches.SwitchNodeMatchPositiveMatch;

/**
 * Implementation of the {@link IncrementalPatternMatcher} for eMoflon.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class EmoflonPatternMatcher implements IncrementalPatternMatcher {

  /**
   * Rules API object generated from graph transformation patterns.
   */
  private final RulesAPI api;

  /**
   * Wrapper that initializes the API object.
   */
  private final EmoflonPatternMatcherApp emoflonPatternMatcherApp;

  /**
   * Current state of the delta. Must be updated in every iteration.
   */
  private PatternMatchingDelta currentDelta = new PatternMatchingDelta();
  // private SubstrateNetwork substrateNetwork;

  /**
   * Map for matches: (Virtual) element to (substrate) element.
   */
  private final Map<Element, List<Element>> virtualMatches = new UnifiedMap<>();

  /**
   * Map for GT matches: Tuple of virtual and substrate element to GraphTransformationMatch.
   */
  private final Map<Tuple, GraphTransformationMatch<?, ?>> tupleToGtMatch = new UnifiedMap<>();

  // private Map<String, Element> allElements;
  // private Map<String, VirtualNetwork> allVirtualNetworks;

  // TODO: Currently all update and removal functionality is missing!

  /**
   * Constructor that initializes the object for a given root node.
   * 
   * @param root Root node to work with.
   */
  public EmoflonPatternMatcher(final Root root) {
    emoflonPatternMatcherApp = new EmoflonPatternMatcherApp(root);
    api = emoflonPatternMatcherApp.initAPI();

    /*
     * New elements
     */

    // api.substrateServer()
    // .subscribeAppearing(m -> currentDelta.addSubstrateServer(m.getSubstrateServer()));
    // api.substrateLink()
    // .subscribeAppearing(m -> currentDelta.addSubstrateLink(m.getSubstrateLink()));
    //
    // api.virtualServer()
    // .subscribeAppearing(m -> currentDelta.addVirtualServer(m.getVirtualServer()));
    // api.virtualSwitch()
    // .subscribeAppearing(m -> currentDelta.addVirtualSwitch(m.getVirtualSwitch()));
    // api.virtualLink().subscribeAppearing(m -> currentDelta.addVirtualLink(m.getVirtualLink()));

    /*
     * Matches
     */

    // api.networkMatch().subscribeAppearing(
    // m -> currentDelta.addNetworkMatch(m.getVirtualNetwork(), m.getSubstrateNetwork()));

    api.serverMatchPositive().subscribeAppearing(m -> {
      addMatch(currentDelta::addServerMatchPositive, m.getVirtualNode(), m.getSubstrateNode());
      tupleToGtMatch.put(new Tuple(m.getVirtualNode(), m.getSubstrateNode()), m);
    });

    // api.serverMatchNegative().subscribeAppearing(m -> {
    // addMatch(currentDelta::addServerMatchNegative, m.getVirtualNode(), m.getSubstrateNode());
    // });

    // api.serverMatchSwitchNegative().subscribeAppearing(m -> {
    // addMatch(currentDelta::addServerMatchSwitchNegative, m.getVirtualNode(),
    // m.getSubstrateNode());
    // });

    api.switchNodeMatchPositive().subscribeAppearing(m -> {
      addMatch(currentDelta::addSwitchMatchPositive, m.getVirtualSwitch(), m.getSubstrateNode());
      tupleToGtMatch.put(new Tuple(m.getVirtualSwitch(), m.getSubstrateNode()), m);
    });

    api.linkPathMatchPositive().subscribeAppearing(m -> {
      addMatch(currentDelta::addLinkPathMatchPositive, m.getVirtualLink(), m.getSubstratePath());
      tupleToGtMatch.put(new Tuple(m.getVirtualLink(), m.getSubstratePath()), m);
    });

    // api.linkPathMatchNegative().subscribeAppearing(m -> {
    // addMatch(currentDelta::addLinkPathMatchNegatives, m.getVirtualLink(), m.getSubstratePath());
    // });

    api.linkServerMatchPositive().subscribeAppearing(m -> {
      addMatch(currentDelta::addLinkServerMatchPositive, m.getVirtualLink(),
          m.getSubstrateServer());
      tupleToGtMatch.put(new Tuple(m.getVirtualLink(), m.getSubstrateServer()), m);
    });

  }

  public void apply(final VirtualElement virt, final SubstrateElement sub) {
    final GraphTransformationMatch<?, ?> match = tupleToGtMatch.get(new Tuple(virt, sub));
    if (match instanceof ServerMatchPositiveMatch) {
      api.serverMatchPositive().apply((ServerMatchPositiveMatch) match);
    } else if (match instanceof SwitchNodeMatchPositiveMatch) {
      api.switchNodeMatchPositive().apply((SwitchNodeMatchPositiveMatch) match);
    } else if (match instanceof LinkPathMatchPositiveMatch) {
      api.linkPathMatchPositive().apply((LinkPathMatchPositiveMatch) match);
    } else if (match instanceof LinkServerMatchPositiveMatch) {
      api.linkServerMatchPositive().apply((LinkServerMatchPositiveMatch) match);
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
   * @param virtual Virtual element for the mapping.
   * @param substrate Substrate element for the mapping.
   */
  public void addMatch(final BiConsumer<Element, Element> deltaModification, final Element virtual,
      final Element substrate) {
    virtualMatches.computeIfAbsent(virtual, k -> new LinkedList<>()).add(substrate);
    deltaModification.accept(virtual, substrate);
  }

}
