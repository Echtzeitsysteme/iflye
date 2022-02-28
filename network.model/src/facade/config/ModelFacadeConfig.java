package facade.config;

import facade.ModelFacade;

/**
 * Configuration parameters for the {@link ModelFacade}.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@es.tu-darmstadt.de>}
 */
public class ModelFacadeConfig {

	/**
	 * Private constructor ensures no instantiation of this class.
	 */
	private ModelFacadeConfig() {
	}

	/**
	 * Minimum length of paths to create.
	 */
	public static int MIN_PATH_LENGTH = 1;

	/**
	 * Maximum length of paths to create. This number has to be at least
	 * MIN_PATH_LENGTH.
	 */
	public static int MAX_PATH_LENGTH = 4;

	/**
	 * If true, the ModelFacade determines the depth of all servers and sets the
	 * maximum path length accordingly. If true, this settings overwrites
	 * {@link #MAX_PATH_LENGTH}.
	 */
	public static boolean MAX_PATH_LENGTH_AUTO = false;

	/**
	 * If true, the model embedding will ignore bandwidth constraints of links.
	 */
	public static boolean IGNORE_BW = false;

	/**
	 * If true, all virtual paths will be generated using Yen's algorithm with the
	 * parameter {@link #YEN_K} for the number of K fastest links per node pair.
	 */
	public static boolean YEN_PATH_GEN = false;

	/**
	 * Number of K fastest links per node pair if {@link #YEN_PATH_GEN} is true.
	 * (Else the parameter does not matter.)
	 */
	public static int YEN_K = 2;

}
