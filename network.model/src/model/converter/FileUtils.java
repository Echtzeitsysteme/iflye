package model.converter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * File utilities for loading and saving files.
 * 
 * @author Maximilian Kratz (maximilian.kratz@stud.tu-darmstadt.de)
 */
public class FileUtils {

  /**
   * Private constructor to forbid instantiation of objects.
   */
  private FileUtils() {}

  /**
   * Takes a given path and a JSON object and writes it to a file.
   * 
   * @param path Path for the file to write.
   * @param json JSON object to write to file.
   */
  protected static void writeFileFromJson(final String path, final JsonObject json) {
    FileUtils.writeFile(path, json.toString());
  }

  /**
   * Reads a file from a given path to a JSON object.
   * 
   * @param path Path for the file to read.
   * @return JSON object read from file.
   */
  protected static JsonObject readFileToJson(final String path) {
    return new Gson().fromJson(FileUtils.readFile(path), JsonObject.class);
  }

  /**
   * Writes given string content to a file at given path.
   * 
   * @param path Path to write file to.
   * @param string Content to write in file.
   */
  public static void writeFile(final String path, final String string) {
    FileWriter file = null;
    try {
      file = new FileWriter(path);
      file.write(string);
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

  /**
   * Reads a file from a given path and returns its content as string.
   * 
   * @param path Path to read file from.
   * @return File content as string.
   */
  public static String readFile(final String path) {
    String read = "";
    try {
      read = Files.readString(Path.of(path));
    } catch (final IOException e) {
      throw new IllegalArgumentException();
    }
    return read;
  }

}
