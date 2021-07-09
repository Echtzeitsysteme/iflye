package gt.emoflon;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import gt.emoflon.apps.EmoflonPatternMatcherVnetHiPEApp;
import model.Element;
import model.Root;
import model.SubstrateElement;
import model.VirtualElement;
import model.rules.vnet.api.VnetAPI;
import model.rules.vnet.api.VnetApp;
import model.rules.vnet.api.matches.VnetToServerMatch;
import patternmatching.IncrementalPatternMatcher;
import patternmatching.PatternMatchingConfig;
import patternmatching.PatternMatchingDelta;
import patternmatching.emoflon.Tuple;

/**
 * Implementation of the {@link IncrementalPatternMatcher} for eMoflon.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class EmoflonPatternMatcherVnet implements IncrementalPatternMatcher {

  /**
   * Vnet Rules API object generated from graph transformation patterns.
   */
  private final VnetAPI api;

  /**
   * Wrapper that initializes the API object.
   */
  private final VnetApp emoflonPatternMatcherApp;

  /**
   * Current state of the delta. Must be updated in every iteration.
   */
  private PatternMatchingDelta currentDelta = new PatternMatchingDelta();

  /**
   * Map for matches: (Virtual) element to (substrate) element.
   */
  private final Map<Element, List<Element>> virtualMatches = new UnifiedMap<>();

  /**
   * Map for GT matches: Tuple of virtual and substrate element to GraphTransformationMatch.
   */
  private final Map<Tuple, GraphTransformationMatch<?, ?>> tupleToGtMatch = new UnifiedMap<>();

  /**
   * Constructor that initializes the object for a given root node.
   * 
   * @param root Root node to work with.
   */
  public EmoflonPatternMatcherVnet(final Root root) {
    switch (PatternMatchingConfig.pm) {
      case HIPE:
        emoflonPatternMatcherApp = new EmoflonPatternMatcherVnetHiPEApp(root);
        break;
      default:
        throw new UnsupportedOperationException();
    }

    api = emoflonPatternMatcherApp.initAPI();

    /*
     * Matches
     */

    api.vnetToServer().subscribeAppearing(m -> {
      addMatch(currentDelta::addNetworkServerMatchPositive, m.getVirtualNetwork(),
          m.getSubstrateNode());
      tupleToGtMatch.put(new Tuple(m.getVirtualNetwork(), m.getSubstrateNode()), m);
    });
  }

  /**
   * Applies a match for a given virtual and substrate element (creates the embedding).
   * 
   * @param virt Virtual element to embed.
   * @param sub Substrate element to embed on.
   * @param doUpdate True if PM should do updates.
   */
  public void apply(final VirtualElement virt, final SubstrateElement sub, final boolean doUpdate) {
    final GraphTransformationMatch<?, ?> match = tupleToGtMatch.get(new Tuple(virt, sub));
    if (match instanceof VnetToServerMatch) {
      api.vnetToServer().apply((VnetToServerMatch) match, doUpdate);
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
