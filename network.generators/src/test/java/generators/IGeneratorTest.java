package generators;

import facade.ModelFacade;

/**
 * An super type interface that acts as a common type for network generator tests.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public interface IGeneratorTest {

  /**
   * ModelFacade instance.
   */
  public static ModelFacade facade = ModelFacade.getInstance();

}
