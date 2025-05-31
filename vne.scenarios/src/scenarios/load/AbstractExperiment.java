package scenarios.load;

import java.util.function.Function;

import algorithms.AbstractAlgorithm;
import facade.ModelFacade;
import iflye.dependencies.logging.IflyeLogger;
import model.SubstrateNetwork;

/**
 * Blueprint to design custom experiments with different algorithms
 *
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public abstract class AbstractExperiment extends IflyeLogger implements Experiment {

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

	/**
	 * Constructs a new uninitialized experiment.
	 */
	public AbstractExperiment() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<ModelFacade, AbstractAlgorithm> getAlgoFactory() {
		return algoFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAlgoFactory(Function<ModelFacade, AbstractAlgorithm> algoFactory) {
		this.algoFactory = algoFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPersistModel() {
		return persistModel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPersistModel(boolean persistModel) {
		this.persistModel = persistModel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPersistModelPath() {
		return persistModelPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPersistModelPath(String persistModelPath) {
		this.persistModelPath = persistModelPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRemoveUnembeddedVnets() {
		return removeUnembeddedVnets;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRemoveUnembeddedVnets(boolean removeUnembeddedVnets) {
		this.removeUnembeddedVnets = removeUnembeddedVnets;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSubNetPath() {
		return subNetPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSubNetPath(String subNetPath) {
		this.subNetPath = subNetPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVirtNetsPath() {
		return virtNetsPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVirtNetsPath(String virtNetsPath) {
		this.virtNetsPath = virtNetsPath;
	}

}
