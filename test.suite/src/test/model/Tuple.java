package test.model;

/**
 * Tuple class for some path related tests.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 *
 * @param <X> Type of value 1.
 * @param <Y> Type of value 2.
 */
public class Tuple<X, Y> {

  public final X x;
  public final Y y;

  public Tuple(final X x, final Y y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return x + "->" + y;
  }

}
