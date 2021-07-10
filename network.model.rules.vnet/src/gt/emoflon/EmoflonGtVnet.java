package gt.emoflon;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import facade.ModelFacade;
import gt.IncrementalPatternMatcher;
import gt.PatternMatchingConfig;
import gt.PatternMatchingDelta;
import gt.emoflon.apps.EmoflonGtVnetHiPEApp;
import model.Element;
import model.Link;
import model.Node;
import model.Root;
import model.SubstrateElement;
import model.SubstrateServer;
import model.VirtualElement;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;
import model.rules.vnet.api.VnetAPI;
import model.rules.vnet.api.VnetApp;
import model.rules.vnet.api.matches.VnetToServerMatch;

/**
 * Implementation of the {@link IncrementalPatternMatcher} for eMoflon.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class EmoflonGtVnet implements IncrementalPatternMatcher {

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
  public EmoflonGtVnet(final Root root) {
    switch (PatternMatchingConfig.pm) {
      case HIPE:
        emoflonPatternMatcherApp = new EmoflonGtVnetHiPEApp(root);
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

      // TODO: Currently, all virtual elements have to be embedded manually
      embedElements((VirtualNetwork) virt, (SubstrateServer) sub);
    }
  }

  /**
   * FIXME: Remove this method after implementing whole embedding using GT.
   * 
   * @param vnet Virtual network to embed.
   * @param sub Substrate server to embed virtual network on.
   */
  private void embedElements(final VirtualNetwork vnet, final SubstrateServer sub) {
    for (final Node n : vnet.getNodes()) {
      if (n instanceof VirtualServer) {
        ModelFacade.getInstance().embedServerToServer(sub.getName(), n.getName());
      } else if (n instanceof VirtualSwitch) {
        ModelFacade.getInstance().embedSwitchToNode(sub.getName(), n.getName());
      }
    }

    for (final Link l : vnet.getLinks()) {
      ModelFacade.getInstance().embedLinkToServer(sub.getName(), l.getName());
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
