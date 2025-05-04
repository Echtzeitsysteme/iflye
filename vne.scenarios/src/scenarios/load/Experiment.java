package scenarios.load;

import java.util.function.Function;

import algorithms.AbstractAlgorithm;
import facade.ModelFacade;

public interface Experiment {
	public void run();

	public Function<ModelFacade, AbstractAlgorithm> getAlgoFactory();

	public void setAlgoFactory(Function<ModelFacade, AbstractAlgorithm> algoFactory);

	public boolean isPersistModel();

	public void setPersistModel(boolean persistModel);

	public String getPersistModelPath();

	public void setPersistModelPath(String persistModelPath);

	public boolean isRemoveUnembeddedVnets();

	public void setRemoveUnembeddedVnets(boolean removeUnembeddedVnets);

	public String getSubNetPath();

	public void setSubNetPath(String subNetPath);

	public String getVirtNetsPath();

	public void setVirtNetsPath(String virtNetsPath);
}
