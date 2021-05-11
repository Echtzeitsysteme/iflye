package ilp.wrapper;

public class IlpSolverException extends RuntimeException {

  public IlpSolverException() {
    super();
  }

  public IlpSolverException(final String message) {
    super(message);
  }

  public IlpSolverException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public IlpSolverException(final Throwable cause) {
    super(cause);
  }

}
