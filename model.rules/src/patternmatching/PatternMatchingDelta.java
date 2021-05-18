package patternmatching;

import java.util.HashSet;
import java.util.Set;
import model.Element;
import model.SubstrateNetwork;
import model.VirtualNetwork;

public class PatternMatchingDelta {

  public static class Match {
    private final Element substrateElement;
    private final Element virtualElement;
    private final boolean selectable;
    // TODO: ^maybe selectable can be removed in the future

    // public Match(final String virtualId, final String substrateId) {
    // this(virtualId, substrateId, true);
    // }
    //
    // public Match(final String virtualId, final String substrateId, final boolean selectable) {
    // super();
    // this.substrateId = substrateId;
    // this.virtualId = virtualId;
    // this.selectable = selectable;
    // }

    public Match(final Element virtualElement, final Element substrateElement) {
      this.substrateElement = substrateElement;
      this.virtualElement = virtualElement;
      this.selectable = true;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final Match other = (Match) obj;
      if (substrateElement == null) {
        if (other.substrateElement != null) {
          return false;
        }
      } else if (!substrateElement.equals(other.substrateElement)) {
        return false;
      }
      if (virtualElement == null) {
        if (other.virtualElement != null) {
          return false;
        }
      } else if (!virtualElement.equals(other.virtualElement)) {
        return false;
      }
      return true;
    }

    public Element getSubstrate() {
      return substrateElement;
    }

    public Element getVirtual() {
      return virtualElement;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (substrateElement == null ? 0 : substrateElement.hashCode());
      result = prime * result + (virtualElement == null ? 0 : virtualElement.hashCode());
      return result;
    }

    public boolean isSelectable() {
      return selectable;
    }

    @Override
    public String toString() {
      return "Match [" + virtualElement.getName() + (selectable ? "-->" : "-/>")
          + substrateElement.getName() + "]";
    }

  }

  // private final Set<SubstrateServer> newSubstrateServers = new HashSet<>();
  // private final Set<SubstrateLink> newSubstrateLinks = new HashSet<>();
  // private final Set<VirtualServer> newVirtualServers = new HashSet<>();
  // private final Set<VirtualSwitch> newVirtualSwitches = new HashSet<>();
  // private final Set<VirtualLink> newVirtualLinks = new HashSet<>();

  private final Set<Match> newNetworkMatches = new HashSet<>();
  private final Set<Match> newServerMatchPositives = new HashSet<>();
  private final Set<Match> newServerMatchNegatives = new HashSet<>();
  private final Set<Match> newServerMatchSwitchNegatives = new HashSet<>();
  private final Set<Match> newSwitchMatchPositives = new HashSet<>();
  private final Set<Match> newLinkPathMatchPositives = new HashSet<>();
  private final Set<Match> newLinkPathMatchNegatives = new HashSet<>();
  private final Set<Match> newLinkServerMatchPositives = new HashSet<>();

  private <T> void addValue(final T value, final Set<T> newMatches) {
    newMatches.add(value);
  }

  /*
   * Adders
   */

  // public void addSubstrateServer(final SubstrateServer server) {
  // addValue(server, newSubstrateServers);
  // }
  //
  // public void addSubstrateLink(final SubstrateLink link) {
  // addValue(link, newSubstrateLinks);
  // }
  //
  // public void addVirtualServer(final VirtualServer server) {
  // addValue(server, newVirtualServers);
  // }
  //
  // public void addVirtualSwitch(final VirtualSwitch sw) {
  // addValue(sw, newVirtualSwitches);
  // }
  //
  // public void addVirtualLink(final VirtualLink link) {
  // addValue(link, newVirtualLinks);
  // }

  public void addNetworkMatch(final VirtualNetwork virt, final SubstrateNetwork sub) {
    addValue(new Match(virt, sub), newNetworkMatches);
  }

  public void addServerMatchPositive(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newServerMatchPositives);
  }

  public void addServerMatchNegative(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newServerMatchNegatives);
  }

  public void addServerMatchSwitchNegative(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newServerMatchSwitchNegatives);
  }

  public void addSwitchMatchPositive(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newSwitchMatchPositives);
  }

  public void addLinkPathMatchPositive(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newLinkPathMatchPositives);
  }

  public void addLinkPathMatchNegatives(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newLinkPathMatchNegatives);
  }

  public void addLinkServerMatchPositive(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newLinkServerMatchPositives);
  }

  /*
   * Getters
   */

  // public Set<SubstrateServer> getNewSubstrateServers() {
  // return newSubstrateServers;
  // }
  //
  // public Set<SubstrateLink> getNewSubstrateLinks() {
  // return newSubstrateLinks;
  // }
  //
  // public Set<VirtualServer> getNewVirtualServers() {
  // return newVirtualServers;
  // }
  //
  // public Set<VirtualSwitch> getNewVirtualSwitches() {
  // return newVirtualSwitches;
  // }
  //
  // public Set<VirtualLink> getNewVirtualLinks() {
  // return newVirtualLinks;
  // }

  public Set<Match> getNewNetworkMatches() {
    return newNetworkMatches;
  }

  public Set<Match> getNewServerMatchPositives() {
    return newServerMatchPositives;
  }

  public Set<Match> getNewServerMatchNegatives() {
    return newServerMatchNegatives;
  }

  public Set<Match> getNewServerMatchSwitchNegatives() {
    return newServerMatchSwitchNegatives;
  }

  public Set<Match> getNewSwitchMatchPositives() {
    return newSwitchMatchPositives;
  }

  public Set<Match> getNewLinkPathMatchPositives() {
    return newLinkPathMatchPositives;
  }

  public Set<Match> getNewLinkPathMatchNegatives() {
    return newLinkPathMatchNegatives;
  }

  public Set<Match> getNewLinkServerMatchPositives() {
    return newLinkServerMatchPositives;
  }

}
