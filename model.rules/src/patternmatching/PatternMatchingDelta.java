package patternmatching;

import java.util.HashSet;
import java.util.Set;
import model.Element;

/**
 * Data object that holds new matches from a pattern matcher.
 * 
 * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [1].
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 * 
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class PatternMatchingDelta {

  /**
   * Data object that holds one particular match from a virtual to a substrate element.
   * 
   * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
   * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
   */
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

  /*
   * New matches for this delta object.
   */
  // private final Set<Match> newNetworkMatches = new HashSet<>();
  private final Set<Match> newServerMatchPositives = new HashSet<>();
  private final Set<Match> removedServerMatchPositives = new HashSet<>();

  private final Set<Match> newSwitchMatchPositives = new HashSet<>();
  private final Set<Match> removedSwitchMatchPositives = new HashSet<>();

  private final Set<Match> newLinkPathMatchPositives = new HashSet<>();
  private final Set<Match> removedLinkPathMatchPositives = new HashSet<>();

  private final Set<Match> newLinkServerMatchPositives = new HashSet<>();
  private final Set<Match> removedLinkServerMatchPositives = new HashSet<>();

  /**
   * Adds a given value of type T to a given set of matches.
   * 
   * @param <T> Type parameter.
   * @param value Value of type T.
   * @param newMatches Set of type T for adding value to.
   */
  private <T> void addValue(final T value, final Set<T> newMatches) {
    newMatches.add(value);
  }

  /**
   * Removes a given value of type T (by adding it to the removed set given).
   * 
   * @param <T> Type parameter.
   * @param value Value of type T
   * @param newMatches Collection to remove match from.
   * @param updatedMatches Collection to update match in.
   * @param removedMatches Collection to add match to (because it is removed).
   */
  private <T> void removeValue(final T value, final Set<T> newMatches, final Set<T> updatedMatches,
      final Set<T> removedMatches) {
    if (updatedMatches != null) {
      updatedMatches.remove(value);
    }
    if (!newMatches.remove(value)) {
      removedMatches.add(value);
    }
  }

  /*
   * Adders
   */

  public void addServerMatchPositive(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newServerMatchPositives);
  }

  public void addSwitchMatchPositive(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newSwitchMatchPositives);
  }

  public void addLinkPathMatchPositive(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newLinkPathMatchPositives);
  }

  public void addLinkServerMatchPositive(final Element virtual, final Element substrate) {
    addValue(new Match(virtual, substrate), newLinkServerMatchPositives);
  }

  /*
   * Removers
   */

  public void removeServerMatchPositive(final Element virtual, final Element substrate) {
    // TODO: Change null to updatedServerMatchPositives
    removeValue(new Match(virtual, substrate), newServerMatchPositives, null,
        removedServerMatchPositives);
  }

  public void removeSwitchMatchPositive(final Element virtual, final Element substrate) {
    // TODO: Change null
    removeValue(new Match(virtual, substrate), newSwitchMatchPositives, null,
        removedSwitchMatchPositives);
  }

  public void removeLinkPathMatchPositive(final Element virtual, final Element substrate) {
    // TODO: Change null
    removeValue(new Match(virtual, substrate), newLinkPathMatchPositives, null,
        removedLinkPathMatchPositives);
  }

  public void removeLinkServerMatchPositive(final Element virtual, final Element substrate) {
    // TODO: Change null
    removeValue(new Match(virtual, substrate), newLinkServerMatchPositives, null,
        removedLinkServerMatchPositives);
  }

  /*
   * Getters
   */

  public Set<Match> getNewServerMatchPositives() {
    return newServerMatchPositives;
  }

  public Set<Match> getNewSwitchMatchPositives() {
    return newSwitchMatchPositives;
  }

  public Set<Match> getNewLinkPathMatchPositives() {
    return newLinkPathMatchPositives;
  }

  public Set<Match> getNewLinkServerMatchPositives() {
    return newLinkServerMatchPositives;
  }

  public Set<Match> getRemovedServerMatchPositives() {
    return removedServerMatchPositives;
  }

  public Set<Match> getRemovedSwitchMatchPositives() {
    return removedSwitchMatchPositives;
  }

  public Set<Match> getRemovedLinkPathMatchPositives() {
    return removedLinkPathMatchPositives;
  }

  public Set<Match> getRemovedLinkServerMatchPositives() {
    return removedLinkServerMatchPositives;
  }

}
