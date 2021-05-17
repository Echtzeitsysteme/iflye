package patternmatching.emoflon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.emf.common.util.URI;
import model.Root;
import rules.api.RulesAPI;
import rules.api.RulesHiPEApp;

public class EmoflonPatternMatcherApp extends RulesHiPEApp {
  private static Path tempDir;

  private static Path createTempDir() {
    if (tempDir == null) {
      try {
        tempDir = Files.createTempDirectory("eMoflonTmp");
      } catch (final IOException e) {
        throw new RuntimeException("Unable to create temporary directory for emoflon", e);
      }
    }
    return tempDir;
  }

  public EmoflonPatternMatcherApp(final Root model) {
    super(createTempDir().normalize().toString() + "/");
    extractFiles();
    if (model.eResource() == null) {
      createModel(URI.createURI("model.xmi"));
      resourceSet.getResources().get(0).getContents().add(model);
    } else {
      resourceSet = model.eResource().getResourceSet();
    }
  }


  private void extractFiles() {
    final File target = new File(workspacePath + RulesAPI.patternPath);
    if (target.exists()) {
      return;
    }
    try (final InputStream is =
        EmoflonPatternMatcherApp.class.getResourceAsStream("/rules/api/ibex-patterns.xmi")) {
      target.getParentFile().mkdirs();
      if (is == null) {
        throw new IllegalStateException("ibex-patterns are missing from the resources");
      }
      Files.copy(is, target.toPath());
      target.deleteOnExit();
    } catch (final IOException e) {
      throw new IllegalStateException("Something went wrong while copying emoflon resources", e);
    }
  }

}
