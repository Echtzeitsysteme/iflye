package scenarios.load;

import java.util.function.Function;

import algorithms.AbstractAlgorithm;
import facade.ModelFacade;
import model.SubstrateNetwork;

/**
 * Blueprint to design custom experiments with different algorithms
 *
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public abstract class AbstractExperiment implements Experiment {

	/**
	 * Substrate network to use.
	 */
	protected SubstrateNetwork sNet;

	/**
	 * File path for the JSON file to load the substrate network from.
	 */
	protected String subNetPath;

	/**
	 * File path for the JSON file to load all virtual networks from.
	 */
	protected String virtNetsPath;

	/**
	 * The algorithm to use
	 */
	protected Function<ModelFacade, AbstractAlgorithm> algoFactory = null;

	/**
	 * If the model should be persisted after execution, optionally supply the file
	 * name.
	 */
	protected boolean persistModel = false;

	/**
	 * The path to the file where the model should be persisted.
	 */
	protected String persistModelPath;

	/**
	 * If VNets that where not successfully embedded should be removed from the
	 * model to prevent from blocking further embeddings.
	 */
	protected boolean removeUnembeddedVnets = false;

	public AbstractExperiment() {
	}

	@Override
	public Function<ModelFacade, AbstractAlgorithm> getAlgoFactory() {
		return algoFactory;
	}

	@Override
	public void setAlgoFactory(Function<ModelFacade, AbstractAlgorithm> algoFactory) {
		this.algoFactory = algoFactory;
	}

	@Override
	public boolean isPersistModel() {
		return persistModel;
	}

	@Override
	public void setPersistModel(boolean persistModel) {
		this.persistModel = persistModel;
	}

	@Override
	public String getPersistModelPath() {
		return persistModelPath;
	}

	@Override
	public void setPersistModelPath(String persistModelPath) {
		this.persistModelPath = persistModelPath;
	}

	@Override
	public boolean isRemoveUnembeddedVnets() {
		return removeUnembeddedVnets;
	}

	@Override
	public void setRemoveUnembeddedVnets(boolean removeUnembeddedVnets) {
		this.removeUnembeddedVnets = removeUnembeddedVnets;
	}

	@Override
	public String getSubNetPath() {
		return subNetPath;
	}

	@Override
	public void setSubNetPath(String subNetPath) {
		this.subNetPath = subNetPath;
	}

	@Override
	public String getVirtNetsPath() {
		return virtNetsPath;
	}

	@Override
	public void setVirtNetsPath(String virtNetsPath) {
		this.virtNetsPath = virtNetsPath;
	}

}
