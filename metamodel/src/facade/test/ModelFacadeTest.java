package facade.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;

public class ModelFacadeTest {

	@Before
	public void resetModel() {
		ModelFacade.getInstance().resetAll();
	}
	
	@Test
	public void testNoNetworksAfterInit() {
		assertTrue(ModelFacade.getInstance().getAllNetworks().isEmpty());
	}
	
	@Test
	public void testSingleNetworkCreation() {
		ModelFacade.getInstance().addNetworkToRoot("test", false);
		assertEquals(1, ModelFacade.getInstance().getAllNetworks().size());
	}
	
}
