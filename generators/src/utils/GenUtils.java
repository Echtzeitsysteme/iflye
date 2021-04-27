package utils;

import config.GlobalGeneratorConfig;
import facade.ModelFacade;

/**
 * Network generator utilities.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class GenUtils {

	private GenUtils() {}
	
	public static String getServerId() {
		return GlobalGeneratorConfig.SERVER + GlobalGeneratorConfig.SEPARATOR
				+ ModelFacade.getInstance().getNextId();
	}
	
	public static String getSwitchId() {
		return GlobalGeneratorConfig.SWITCH + GlobalGeneratorConfig.SEPARATOR
				+ ModelFacade.getInstance().getNextId();
	}
	
	public static String getLinkdId() {
		return GlobalGeneratorConfig.LINK + GlobalGeneratorConfig.SEPARATOR
				+ ModelFacade.getInstance().getNextId();
	}
	
}
