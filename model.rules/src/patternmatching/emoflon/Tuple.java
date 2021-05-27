package patternmatching.emoflon;

import model.SubstrateElement;
import model.VirtualElement;

/**
 * Tuple class for the pattern matcher.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class Tuple {

  public final VirtualElement x;
  public final SubstrateElement y;

  public Tuple(final VirtualElement x, final SubstrateElement y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return x + "->" + y;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Tuple) {
      final Tuple o = (Tuple) obj;
      return this.x.getName().equals(o.x.getName()) && this.y.getName().equals(o.y.getName());
    }

    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (x == null ? 0 : x.hashCode());
    result = prime * result + (y == null ? 0 : y.hashCode());
    return result;
  }

}
