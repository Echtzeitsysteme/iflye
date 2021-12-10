package test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import facade.ModelFacade;
import facade.config.ModelFacadeConfig;
import generators.FatTreeNetworkGenerator;
import generators.config.FatTreeConfig;
import model.converter.FileUtils;

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

  /**
   * Old ignore bandwidth value.
   */
  private boolean oldIgnoreBw;

  /**
   * Old link host embed path value.
   */
  private boolean oldLinkHostEmbedPath;

  /**
   * Old Yen path generation value.
   */
  private boolean oldYenPathGen;

  @BeforeEach
  public void resetModel() {
    ModelFacade.getInstance().resetAll();

    // Save old values
    oldLowerLimit = ModelFacadeConfig.MIN_PATH_LENGTH;
    oldUpperLimit = ModelFacadeConfig.MAX_PATH_LENGTH;
    oldIgnoreBw = ModelFacadeConfig.IGNORE_BW;
    oldLinkHostEmbedPath = ModelFacadeConfig.LINK_HOST_EMBED_PATH;
    oldYenPathGen = ModelFacadeConfig.YEN_PATH_GEN;
    ModelFacadeConfig.MIN_PATH_LENGTH = 1;
    ModelFacadeConfig.MAX_PATH_LENGTH = 4;
    ModelFacadeConfig.IGNORE_BW = false;
    ModelFacadeConfig.LINK_HOST_EMBED_PATH = false;
    ModelFacadeConfig.YEN_PATH_GEN = false;
  }

  @AfterEach
  public void restoreConfig() {
    ModelFacadeConfig.MIN_PATH_LENGTH = oldLowerLimit;
    ModelFacadeConfig.MAX_PATH_LENGTH = oldUpperLimit;
    ModelFacadeConfig.IGNORE_BW = oldIgnoreBw;
    ModelFacadeConfig.LINK_HOST_EMBED_PATH = oldLinkHostEmbedPath;
    ModelFacadeConfig.YEN_PATH_GEN = oldYenPathGen;
  }

  @Test
  public void testFatTreePathGenDijkstra() {
    final FatTreeConfig subConfig = new FatTreeConfig(6);
    final FatTreeNetworkGenerator gen = new FatTreeNetworkGenerator(subConfig);
    gen.createNetwork("sub", false);

    final StringBuilder builder = new StringBuilder();
    ModelFacade.getInstance().getAllPathsOfNetwork("sub").forEach(p -> {
      builder.append(p.getName() + System.lineSeparator());
    });

    // FileUtils.writeFile("resources/refPathDijkstra.txt", builder.toString());
    final String refPath =
        FileUtils.replaceLinebreaks(FileUtils.readFile("resources/refPathDijkstra.txt"));
    assertEquals(refPath, builder.toString());
  }

  @Test
  public void testFatTreePathGenYen() {
    ModelFacadeConfig.YEN_PATH_GEN = true;
    final FatTreeConfig subConfig = new FatTreeConfig(6);
    final FatTreeNetworkGenerator gen = new FatTreeNetworkGenerator(subConfig);
    gen.createNetwork("sub", false);

    final StringBuilder builder = new StringBuilder();
    ModelFacade.getInstance().getAllPathsOfNetwork("sub").forEach(p -> {
      builder.append(p.getName() + System.lineSeparator());
    });

    // FileUtils.writeFile("resources/refPathYen.txt", builder.toString());
    final String refPath =
        FileUtils.replaceLinebreaks(FileUtils.readFile("resources/refPathYen.txt"));
    assertEquals(refPath, builder.toString());
  }

}
