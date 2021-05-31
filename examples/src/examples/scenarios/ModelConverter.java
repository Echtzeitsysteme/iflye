package examples.scenarios;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import facade.ModelFacade;

/**
 * Model converter that converts a json file with network information to the model.
 * 
 * @author Maximilian Kratz (maximilian.kratz@stud.tu-darmstadt.de)
 */
public class ModelConverter {

  private ModelConverter() {}

  public static void main(final String[] args) {
    convert("vnets.json");
    ModelFacade.getInstance().persistModel();
  }

  public static List<String> convert(final String path) {
    final JsonObject json = readFile(path);
    final JsonArray networks = (JsonArray) json.get("networks");
    final List<String> networkOutputIds = new LinkedList<String>();

    for (int i = 0; i < networks.size(); i++) {
      final JsonObject net = (JsonObject) networks.get(i);
      final JsonElement name = net.get("id");
      final JsonArray switches = (JsonArray) net.get("switches");
      final JsonArray servers = (JsonArray) net.get("servers");
      final JsonArray links = (JsonArray) net.get("links");

      createNetwork(name, switches, servers, links);

      networkOutputIds.add(name.getAsString());
    }

    return networkOutputIds;
  }

  private static void createNetwork(final JsonElement name, final JsonArray switches,
      final JsonArray servers, final JsonArray links) {

    final String networkId = name.getAsString();

    // Network itself
    ModelFacade.getInstance().addNetworkToRoot(networkId, true);

    // Switches
    for (final JsonElement actSw : switches) {
      final JsonObject sw = (JsonObject) actSw;
      ModelFacade.getInstance().addSwitchToNetwork(sw.get("id").getAsString(), networkId, 0);
    }

    // Servers
    for (final JsonElement actSrv : servers) {
      final JsonObject srv = (JsonObject) actSrv;
      ModelFacade.getInstance().addServerToNetwork(srv.get("id").getAsString(), networkId,
          srv.get("cpu").getAsInt(), srv.get("memory").getAsInt(), srv.get("storage").getAsInt(),
          1);
    }

    // Links
    for (final JsonElement actLink : links) {
      final JsonObject link = (JsonObject) actLink;
      ModelFacade.getInstance().addLinkToNetwork(link.get("id").getAsString(), networkId,
          link.get("bw").getAsInt(), link.get("source").getAsString(),
          link.get("target").getAsString());
    }
  }

  private static void writeFile(final String path, final JsonObject json) {
    FileWriter file = null;
    try {
      file = new FileWriter(path);
      file.write(json.toString());
    } catch (final IOException e) {
      e.printStackTrace();
    } finally {
      try {
        file.flush();
        file.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static JsonObject readFile(final String path) {
    String read = "";
    try {
      read = Files.readString(Path.of(path));
    } catch (final IOException e) {
      throw new IllegalArgumentException();
    }

    // return new JsonParser().parse(read).getAsJsonObject();
    return new Gson().fromJson(read, JsonObject.class);
  }

}

