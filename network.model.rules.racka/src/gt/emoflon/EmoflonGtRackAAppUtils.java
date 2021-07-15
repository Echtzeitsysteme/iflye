package gt.emoflon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import gt.emoflon.apps.EmoflonGtRackAHiPEApp;
import network.model.rules.racka.api.RackaAPI;

/**
 * Utility class for the Rules Apps.
 * 
 * Parts of this implementation are heavily inspired, taken or adapted from the idyve project [1].
 * 
 * [1] Tomaszek, S., Modellbasierte Einbettung von virtuellen Netzwerken in Rechenzentren,
 * http://dx.doi.org/10.12921/TUPRINTS-00017362. – DOI 10.12921/TUPRINTS– 00017362, 2020.
 * 
 * @author Stefan Tomaszek (ES TU Darmstadt) [idyve project]
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class EmoflonGtRackAAppUtils {

  /**
   * Private constructor forbids instantiation of objects.
   */
  private EmoflonGtRackAAppUtils() {}

  /**
   * Extracts the specified 'ibex-patterns.xmi' file if not already present.
   * 
   * @param workspacePath The path of the workspace.
   */
  public static void extractFiles(final String workspacePath) {
    final File target = new File(workspacePath + RackaAPI.patternPath);
    if (target.exists()) {
      return;
    }
    try (final InputStream is = EmoflonGtRackAHiPEApp.class
        .getResourceAsStream("/network/model/rules/racka/api/ibex-patterns.xmi")) {
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
