package example;

import generator.FileUtil;
import mlmodel.ModelPrediction;
//import scenario.AbstractScenario;
//import scenario.Sub_10_Virt_2_Scenario;
import transform.encoding.AbstractEncoding;
//import transform.encoding.NodeAttributesEncoding;
import transform.label.AbstractEmbeddingLabel;
import transform.label.AbstractFeasableLabel;
//import transform.label.EmbeddingFunctionLabel;
//import transform.label.MultiClassLabel;
//import transform.label.OneClassLabel;
//import transform.label.TwoClassLabel;

public class Config {
//	public static AbstractScenario scenario = new Sub_10_Virt_2_Scenario();
	public static String dataSetsRootDirectory = FileUtil.outputDirectory + "/xmi";
	
	public static boolean predictSwitches = false;
	
	public static int generatorNumberOfRuns = 20000;
	public static boolean generatorExportFailedVnrs = false;
	public static int generatorVnrThreshold = 0;
	public static int generatorStartDataPointNumerationFrom = 0;
	
	public static boolean transformatorExportFailedVnrs = false;
	public static int transformatorVnrThreshold = 0;
//	public static AbstractEncoding transformatorInEncoding = new NodeAttributesEncoding();
//	public static AbstractEncoding transformatorOutEncoding = new MultiClassLabel();
	public static Integer transformatorMaxCount = null;
	
	public static boolean evaluationDataPoints = true;
	public static int evaluationDataPointsCount = 1500;
	public static boolean evaluationVneMlScenario = true;
	public static int evaluationVneMlScenarioRuns = 500;
	public static boolean evaluationGipsScenario = true;
	public static int evaluationGipsScenarioRuns = 50;
	public static boolean evaluationTafScenario = true;
	public static int evaluationTafScenarioRuns = 50;
	
	public static int evaluationFeasableScenarioRuns = 100;

//	public static ModelPrediction getModelForMlVne() {
//		ModelPrediction model = new ModelPrediction();
//		model.setInEncoding(new NodeAttributesEncoding());
//		model.setStandardizeInputVector(true);
//		model.setEmbeddingLabel((AbstractEmbeddingLabel) transformatorOutEncoding);
//		return model;
//	}
//	
//	public static ModelPrediction getModelForMlVneFeasable() {
//		ModelPrediction model = new ModelPrediction();
//		model.setInEncoding(new NodeAttributesEncoding());
//		model.setStandardizeInputVector(true);
//		model.setFeasableLabel((AbstractFeasableLabel) transformatorOutEncoding);
//		return model;
//	}

}
