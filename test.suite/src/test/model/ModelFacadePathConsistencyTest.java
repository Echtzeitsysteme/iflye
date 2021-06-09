package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.FatTreeNetworkGenerator;
import generators.config.FatTreeConfig;
import model.converter.BasicModelConverter;

/**
 * Test class for the ModelFacade that tests the consistency of the path generation. This test class
 * is necessary, because some parallelization in the path generation created different models each
 * time they ran.
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelFacadePathConsistencyTest {

  /*
   * Variables to save the ModelFacade's configuration of path limits to.
   */

  /**
   * Old lower limit value.
   */
  private int oldLowerLimit;

  /**
   * Old upper limit value.
   */
  private int oldUpperLimit;

  @Before
  public void resetModel() {
    ModelFacade.getInstance().resetAll();

    // Save old values
    oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
    oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    ModelFacadeConfig.IGNORE_BW = false;
    ModelFacadeConfig.LINK_HOST_EMBED_PATH = false;
    ModelFacadeConfig.YEN_PATH_GEN = false;
  }

  @After
  public void restoreConfig() {
    ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
    ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
  }

  @Test
  public void testFatTreePathGen() {
    final FatTreeConfig subConfig = new FatTreeConfig(6);
    final FatTreeNetworkGenerator gen = new FatTreeNetworkGenerator(subConfig);
    gen.createNetwork("sub", false);

    final StringBuilder builder = new StringBuilder();
    ModelFacade.getInstance().getAllPathsOfNetwork("sub").forEach(p -> {
      builder.append(p.getName() + System.lineSeparator());
    });

    // BasicModelConverter.writeFile("resources/refPath.txt", builder.toString());
    final String refPath = BasicModelConverter.readFile("resources/refPath.txt");
    assertEquals(refPath, builder.toString());
  }

}
