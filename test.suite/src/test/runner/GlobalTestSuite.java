package test.runner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import algorithms.heuristics.TafAlgorithmTest;
import facade.test.ModelFacadeCreationTest;
import facade.test.ModelFacadeEmbeddingTest;
import facade.test.ModelFacadePathTest;
import generators.OneTierNetworkGeneratorTest;
import generators.TwoTierNetworkGeneratorTest;

/**
 * Global test suite class that enables running all registered junit tests at once in Eclipse.
 * Reference for the "bug" in Eclipse: https://bugs.eclipse.org/bugs/show_bug.cgi?id=111126
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ModelFacadeCreationTest.class, ModelFacadeEmbeddingTest.class,
    ModelFacadePathTest.class, TafAlgorithmTest.class, OneTierNetworkGeneratorTest.class,
    TwoTierNetworkGeneratorTest.class})
public class GlobalTestSuite {

}
