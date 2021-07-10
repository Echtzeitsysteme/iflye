package gt;

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

    public Match(final Element virtualElement, final Element substrateElement) {
      this.substrateElement = substrateElement;
      this.virtualElement = virtualElement;
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

    @Override
    public String toString() {
      return "Match [" + virtualElement.getName() + "-->" + substrateElement.getName() + "]";
    }

  }

  /*
   * New matches for this delta object.
   */
  // private final Set<Match> newNetworkMatches = new HashSet<>();
  private final Set<Match> newServerMatchPositives = new HashSet<>();
  private final Set<Match> newSwitchMatchPositives = new HashSet<>();
  private final Set<Match> newLinkPathMatchPositives = new HashSet<>();
  private final Set<Match> newLinkServerMatchPositives = new HashSet<>();

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

  /*
   * Add other delta to this one
   */

  public void addOther(final PatternMatchingDelta other) {
    this.newServerMatchPositives.addAll(other.newServerMatchPositives);
    this.newSwitchMatchPositives.addAll(other.newSwitchMatchPositives);
    this.newLinkPathMatchPositives.addAll(other.newLinkPathMatchPositives);
    this.newLinkServerMatchPositives.addAll(other.newLinkServerMatchPositives);
  }

  public void clear() {
    newServerMatchPositives.clear();
    newSwitchMatchPositives.clear();
    newLinkPathMatchPositives.clear();
    newLinkServerMatchPositives.clear();
  }

}
