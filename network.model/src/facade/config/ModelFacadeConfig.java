package facade.config;

public interface ModelFacadeConfig {
	
	/**
	 * Minimum length of paths to create. This number has to be at least 2.
	 */
	public static int MIN_PATH_LENGTH = 2;
	
	/**
	 * Maximum length of paths to create. This number has to be at least MIN_PATH_LENGTH.
	 */
	public static int MAX_PATH_LENGTH = 4;
	
}
