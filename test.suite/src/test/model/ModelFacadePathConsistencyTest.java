package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
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

  @Test
  public void testFatTreePathGen() {
    final FatTreeConfig subConfig = new FatTreeConfig(6);
    final FatTreeNetworkGenerator gen = new FatTreeNetworkGenerator(subConfig);
    gen.createNetwork("sub", false);
    ModelFacade.getInstance().persistModel("tmp.xmi");
    final String genModel = BasicModelConverter.readFile("tmp.xmi");
    final String refModel = BasicModelConverter.readFile("resources/modelPathRef.xmi");
    assertEquals(refModel, genModel);
  }

}
