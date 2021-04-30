package facade.config;

public class ModelFacadeConfig {
	
	/**
	 * Private constructor ensures no instantiation of this class.
	 */
	private ModelFacadeConfig() {}
	
	/**
	 * Minimum length of paths to create. This number has to be at least 2.
	 */
	public static final int MIN_PATH_LENGTH = 2;
	
	/**
	 * Maximum length of paths to create. This number has to be at least MIN_PATH_LENGTH.
	 */
	public static final int MAX_PATH_LENGTH = 4;
	
	/**
	 * If true, the model embedding will ignore bandwidth constraints of links.
	 */
	public static boolean IGNORE_BW = false;
	
}
