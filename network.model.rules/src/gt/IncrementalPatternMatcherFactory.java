package gt;

public abstract class IncrementalPatternMatcherFactory {

  public abstract IncrementalPatternMatcher create();

  @Override
  public boolean equals(final Object obj) {
    return getClass().equals(obj.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

}
