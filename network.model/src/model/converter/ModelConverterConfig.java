package model.converter;

/**
 * Configuration of the model converter classes
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class ModelConverterConfig {

  /**
   * Scaling factor of all memory related resources of the servers (memory, storage).
   */
  public static double MEMORY_SCALING = 1.0 / 1024;

  /**
   * Private constructor ensures no instantiation of this class.
   */
  private ModelConverterConfig() {}
}
