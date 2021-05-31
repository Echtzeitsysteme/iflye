package examples.scenarios;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import facade.ModelFacade;
import model.Link;
import model.Node;
import model.Server;
import model.Switch;

/**
 * Model converter that converts a JSON file with virtual or substrate network information to the
 * model.
 * 
 * @author Maximilian Kratz (maximilian.kratz@stud.tu-darmstadt.de)
 */
public class ModelConverter {

  /**
   * Private constructor ensures static use only.
   */
  private ModelConverter() {}

  /**
   * Converts a JSON file from a given path to the model.
   * 
   * @param path Path to read JSON file from.
   * @param isVirtual True if networks should be virtual.
   * @return List of all new network IDs.
   */
  public static List<String> jsonToModel(final String path, final boolean isVirtual) {
    final JsonObject json = readFile(path);
    final JsonArray networks = (JsonArray) json.get("networks");
    final List<String> networkOutputIds = new LinkedList<String>();

    for (int i = 0; i < networks.size(); i++) {
      final JsonObject net = (JsonObject) networks.get(i);
      final JsonElement name = net.get("id");
      final JsonArray switches = (JsonArray) net.get("switches");
      final JsonArray servers = (JsonArray) net.get("servers");
      final JsonArray links = (JsonArray) net.get("links");

      createNetwork(name, switches, servers, links, isVirtual);
      networkOutputIds.add(name.getAsString());

      if (!isVirtual) {
        ModelFacade.getInstance().createAllPathsForNetwork(name.getAsString());
      }
    }

    return networkOutputIds;
  }

  /**
   * Converts the actual model to a JSON file for given collection of IDs and given file path.
   * 
   * @param netIds Collection of network IDs to include.
   * @param path Path to save file to.
   */
  public static void modelToJson(final Collection<String> netIds, final String path) {
    final JsonObject json = new JsonObject();
    final JsonArray jsonNets = new JsonArray();

    for (final String net : netIds) {
      final JsonObject jsonNet = new JsonObject();
      final JsonArray jsonSwitches = new JsonArray();

      for (final Node swNode : ModelFacade.getInstance().getAllSwitchesOfNetwork(net)) {
        final Switch sw = (Switch) swNode;
        final JsonObject jsonSwitch = new JsonObject();
        jsonSwitch.addProperty("id", sw.getName());
        jsonSwitch.addProperty("depth", sw.getDepth());
        jsonSwitches.add(jsonSwitch);
      }

      final JsonArray jsonServers = new JsonArray();

      for (final Node srv : ModelFacade.getInstance().getAllServersOfNetwork(net)) {
        final Server srvInfo = (Server) srv;
        final JsonObject jsonServer = new JsonObject();
        jsonServer.addProperty("id", srv.getName());
        jsonServer.addProperty("cpu", srvInfo.getCpu());
        jsonServer.addProperty("memory", srvInfo.getMemory());
        jsonServer.addProperty("storage", srvInfo.getStorage());
        jsonServer.addProperty("depth", srvInfo.getDepth());
        jsonServers.add(jsonServer);
      }

      final JsonArray jsonLinks = new JsonArray();

      for (final Link l : ModelFacade.getInstance().getAllLinksOfNetwork(net)) {
        final JsonObject jsonLink = new JsonObject();
        jsonLink.addProperty("id", l.getName());
        jsonLink.addProperty("bw", l.getBandwidth());
        jsonLink.addProperty("source", l.getSource().getName());
        jsonLink.addProperty("target", l.getTarget().getName());
        jsonLinks.add(jsonLink);
      }

      jsonNet.addProperty("id", net);
      jsonNet.add("switches", jsonSwitches);
      jsonNet.add("servers", jsonServers);
      jsonNet.add("links", jsonLinks);

      jsonNets.add(jsonNet);
    }

    json.add("networks", jsonNets);
    writeFile(path, json);
  }

  /**
   * Creates a virtual network within the model for given parameters.
   * 
   * @param name JsonElement name.
   * @param switches JsonArray of switches.
   * @param servers JsonArray of servers.
   * @param links JsonArray of links.
   * @param isVirtual True if network should be virtual.
   */
  private static void createNetwork(final JsonElement name, final JsonArray switches,
      final JsonArray servers, final JsonArray links, final boolean isVirtual) {
    // Network itself
    final String networkId = name.getAsString();
    ModelFacade.getInstance().addNetworkToRoot(networkId, isVirtual);

    // Switches
    for (final JsonElement actSw : switches) {
      final JsonObject sw = (JsonObject) actSw;
      ModelFacade.getInstance().addSwitchToNetwork(sw.get("id").getAsString(), networkId,
          sw.get("depth").getAsInt());
    }

    // Servers
    for (final JsonElement actSrv : servers) {
      final JsonObject srv = (JsonObject) actSrv;
      ModelFacade.getInstance().addServerToNetwork(srv.get("id").getAsString(), networkId,
          srv.get("cpu").getAsInt(), srv.get("memory").getAsInt(), srv.get("storage").getAsInt(),
          srv.get("depth").getAsInt());
    }

    // Links
    for (final JsonElement actLink : links) {
      final JsonObject link = (JsonObject) actLink;
      ModelFacade.getInstance().addLinkToNetwork(link.get("id").getAsString(), networkId,
          link.get("bw").getAsInt(), link.get("source").getAsString(),
          link.get("target").getAsString());
    }
  }

  /*
   * Utility methods
   * 
   * TODO: These methods should be re-factored/moved to a generic helper class.
   */

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
    return new Gson().fromJson(read, JsonObject.class);
  }

}

