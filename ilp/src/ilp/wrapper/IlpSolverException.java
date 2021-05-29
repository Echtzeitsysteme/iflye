package ilp.wrapper;

/**
 * Custom exception for ILP solver related issues.
 * 
 * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [1].
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 * 
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class IlpSolverException extends RuntimeException {

  private static final long serialVersionUID = -7957152413203338752L;

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
