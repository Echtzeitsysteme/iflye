package scenarios.load;

import java.util.List;
import java.util.function.Function;

import algorithms.AbstractAlgorithm;
import facade.ModelFacade;
import scenarios.modules.Module;

/**
 * An experiment is any configurable class to run a defined scenario with
 * variable configurations.
 * 
 * The configuration is mostly tailored to the VNE problem domain.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public interface Experiment extends AutoCloseable {

	/**
	 * Run the configured scenario.
	 */
	public void run();

	/**
	 * Close any resources that were opened during the initialization.
	 */
	@Override
	default void close() {
		// noop
	}

	/**
	 * The the default modules to configure this experiment.
	 * 
	 * @return A list of all modules that could be used to configure this
	 *         Experiment.
	 */
	default public List<Module> getConfigurationModules() {
		return List.of();
	}

	/**
	 * Get the currently configured algorithm factory.
	 * 
	 * @return the current algorithm factory.
	 */
	public Function<ModelFacade, AbstractAlgorithm> getAlgoFactory();

	/**
	 * Set the algorithm factory.
	 * 
	 * @param algoFactory the algorithm factory to use.
	 */
	public void setAlgoFactory(Function<ModelFacade, AbstractAlgorithm> algoFactory);

	/**
	 * Get if the model should be currently persisted after each step (depending on
	 * the scenario).
	 * 
	 * @return if the model will be persisted after each step.
	 */
	public boolean isPersistModel();

	/**
	 * Set if the model should be persisted after each step (depending on the
	 * scenario).
	 * 
	 * @param persistModel if the model will be persisted after each step.
	 */
	public void setPersistModel(boolean persistModel);

	/**
	 * Get the currently configured path to persist the model to.
	 * 
	 * @return the path where to persist the model after each step.
	 */
	public String getPersistModelPath();

	/**
	 * Set the path where to persist the model to after each step (depending on the
	 * scenario).
	 * 
	 * @param persistModelPath path to persist the model to.
	 */
	public void setPersistModelPath(String persistModelPath);

	/**
	 * Get if a vNet should be removed from the model if it failed to embed.
	 * 
	 * @return if unembedded vNets should be removed from the model.
	 */
	public boolean isRemoveUnembeddedVnets();

	/**
	 * Set if a vNet should be removed from the model if it failed to embed.
	 * 
	 * @param removeUnembeddedVnets if unembedded vNets should be removed from the
	 *                              model.
	 */
	public void setRemoveUnembeddedVnets(boolean removeUnembeddedVnets);

	/**
	 * Get the currently configured substrate network file path.
	 * 
	 * @return the path to the file that provided the substrate network.
	 */
	public String getSubNetPath();

	/**
	 * Set the substrate network file path.
	 * 
	 * @param subNetPath The path to the JSON file that provides the substrate
	 *                   network configuration.
	 */
	public void setSubNetPath(String subNetPath);

	/**
	 * Get the currently configured virtual network file path.
	 * 
	 * @return the path to the file that provided the virtual network.
	 */
	public String getVirtNetsPath();

	/**
	 * Set the virtual network file path.
	 * 
	 * @param virtNetPath The path to the JSON file that provides the virtual
	 *                    network configuration.
	 */
	public void setVirtNetsPath(String virtNetsPath);

}
