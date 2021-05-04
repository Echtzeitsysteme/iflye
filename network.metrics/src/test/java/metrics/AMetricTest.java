package metrics;

import org.junit.Before;
import facade.ModelFacade;

/**
 * Abstract metric test class.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public abstract class AMetricTest {

  /**
   * ModelFacade object to work with.
   */
  final ModelFacade facade = ModelFacade.getInstance();

  @Before
  public void resetModel() {
    ModelFacade.getInstance().resetAll();
  }

}
