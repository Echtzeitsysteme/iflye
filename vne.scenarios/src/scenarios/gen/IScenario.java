package scenarios.gen;

import facade.ModelFacade;

/**
 * Super type for all scenarios.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public interface IScenario {

	/**
	 * ModelFacade instance.
	 */
	public static ModelFacade facade = ModelFacade.getInstance();

}
