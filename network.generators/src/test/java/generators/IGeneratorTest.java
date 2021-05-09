package generators;

import facade.ModelFacade;

/**
 * A super type interface that acts as a common type for network generator tests.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public interface IGeneratorTest {

  /**
   * ModelFacade instance.
   */
  public static ModelFacade facade = ModelFacade.getInstance();

}
