package facade.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import model.SubstrateNetwork;

/**
 * Test class for the ModelFacade that tests some embedding tasks.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class ModelFacadeEmbeddingTest {

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();
	}
	
	@Test
	public void testEmbedNetworkToNetwork() {
		ModelFacade.getInstance().addNetworkToRoot("sub", false);
		ModelFacade.getInstance().addNetworkToRoot("virt", true);
		
		// No guests before embedding anything
		assertTrue(((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub"))
				.getGuests().isEmpty());
		
		ModelFacade.getInstance().embedNetworkToNetwork("sub", "virt");
		
		assertFalse(((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub"))
				.getGuests().isEmpty());
		assertEquals("virt", ((SubstrateNetwork) ModelFacade.getInstance().getNetworkById("sub"))
				.getGuests().get(0).getName());
	}
	
	@Test
	public void testEmbedServerToServer() {
		//TODO
	}
	
	@Test
	public void testEmbedSwitchToServer() {
		//TODO
	}
	
	@Test
	public void testEmbedSwitchtoSwitch() {
		//TODO
	}
	
	@Test
	public void testEmbedLinkToServer() {
		//TODO
	}
	
	@Test
	public void testEmbedLinkToLink() {
		//TODO
	}
	
	@Ignore
	@Test
	public void testEmbedLinkToPath() {
		//TODO: Implement after creation of all paths is implemented.
	}
	
}
