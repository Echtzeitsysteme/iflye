package test.runner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import test.algorithms.heuristics.TafAlgorithmGoogleFatTreeTest;
import test.algorithms.heuristics.TafAlgorithmTest;
import test.generators.GoogleFatTreeNetworkGeneratorTest;
import test.generators.OneTierNetworkGeneratorTest;
import test.generators.TwoTierNetworkGeneratorTest;
import test.metrics.AcceptedVnrMetricTest;
import test.metrics.ActiveSubstrateServerMetricTest;
import test.metrics.ActiveSubstrateSwitchMetricTest;
import test.metrics.AveragePathLengthMetricTest;
import test.metrics.RuntimeMetricTest;
import test.metrics.TotalCommunicationCostMetricTest;
import test.metrics.TotalPathCostMetricTest;
import test.metrics.TotalTafCommunicationCostMetricTest;
import test.model.ModelFacadeCreationTest;
import test.model.ModelFacadeEmbeddingTest;
import test.model.ModelFacadePathBasicTest;
import test.model.ModelFacadePathTwoTierDetailTest;
import test.model.ModelFacadePathYenTest;

/**
 * Global test suite class that enables running all registered junit tests at once in Eclipse.
 * Reference for the "bug" in Eclipse: https://bugs.eclipse.org/bugs/show_bug.cgi?id=111126
 * 
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ModelFacadeCreationTest.class, ModelFacadeEmbeddingTest.class,
    ModelFacadePathBasicTest.class, ModelFacadePathYenTest.class,
    ModelFacadePathTwoTierDetailTest.class, TafAlgorithmTest.class,
    TafAlgorithmGoogleFatTreeTest.class, OneTierNetworkGeneratorTest.class,
    TwoTierNetworkGeneratorTest.class, GoogleFatTreeNetworkGeneratorTest.class,
    AcceptedVnrMetricTest.class, ActiveSubstrateServerMetricTest.class,
    ActiveSubstrateSwitchMetricTest.class, AveragePathLengthMetricTest.class,
    TotalCommunicationCostMetricTest.class, TotalPathCostMetricTest.class,
    TotalTafCommunicationCostMetricTest.class, RuntimeMetricTest.class})
public class GlobalTestSuite {

}
