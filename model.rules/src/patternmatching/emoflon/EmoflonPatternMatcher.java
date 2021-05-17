package patternmatching.emoflon;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import model.Element;
import model.Root;
import model.VirtualNetwork;
import patternmatching.IncrementalPatternMatcher;
import patternmatching.PatternMatchingDelta;
import rules.api.RulesAPI;
import rules.api.RulesHiPEApp;

public class EmoflonPatternMatcher implements IncrementalPatternMatcher {

  private final RulesAPI api;
  private PatternMatchingDelta currentDelta = new PatternMatchingDelta();
  // TODO: Collections for matches here

  // private SubstrateNetwork substrateNetwork;

  private final Map<Element, List<Element>> virtualMatches = new UnifiedMap<>();
  private Map<String, Element> allElements;
  private Map<String, VirtualNetwork> allVirtualNetworks;

  // TODO: Currently all update and add element functionality is missing!

  public EmoflonPatternMatcher(final Root root) {
    api = new RulesHiPEApp().initAPI();

    api.serverMatchPositive().subscribeAppearing(m -> {
      addMatch(currentDelta::addServerMatchPositive, m.getVirtualNode(), m.getSubstrateNode());
    });

    api.serverMatchNegative().subscribeAppearing(m -> {
      addMatch(currentDelta::addServerMatchNegative, m.getVirtualNode(), m.getSubstrateNode());
    });

    api.switchNodeMatchPositive().subscribeAppearing(m -> {
      addMatch(currentDelta::addSwitchMatchPositive, m.getVirtualSwitch(), m.getSubstrateNode());
    });

    api.linkPathMatchPositive().subscribeAppearing(m -> {
      addMatch(currentDelta::addLinkPathMatchPositive, m.getVirtualLink(), m.getSubstratePath());
    });

    api.linkPathMatchNegative().subscribeAppearing(m -> {
      addMatch(currentDelta::addLinkPathMatchNegatives, m.getVirtualLink(), m.getSubstratePath());
    });

    api.linkServerMatchPositive().subscribeAppearing(m -> {
      addMatch(currentDelta::addLinkServerMatchPositive, m.getVirtualLink(),
          m.getSubstrateServer());
    });
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

  public void addMatch(final BiConsumer<Element, Element> deltaModification, final Element virtual,
      final Element substrate) {
    // map.computeIfAbsent(key, k -> new HashSet<V>()).add(v);
    virtualMatches.computeIfAbsent(virtual, k -> new LinkedList<>()).add(substrate);
    deltaModification.accept(virtual, substrate);
  }

}
