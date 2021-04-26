package facade.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import facade.ModelFacade;
import model.Node;
import model.Server;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.Switch;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Test class for the ModelFacade.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class ModelFacadeTest {

	@BeforeEach
	public void resetModel() {
		ModelFacade.getInstance().resetAll();
	}
	
	@Test
	public void testNoNetworksAfterInit() {
		assertTrue(ModelFacade.getInstance().getAllNetworks().isEmpty());
	}
	
	@Test
	public void testSingleSubstrateNetworkCreation() {
		final String id = "test";
		ModelFacade.getInstance().addNetworkToRoot(id, false);
		assertEquals(1, ModelFacade.getInstance().getAllNetworks().size());
		assertTrue(ModelFacade.getInstance().getNetworkById(id) instanceof SubstrateNetwork);
	}
	
	@Test
	public void testSingleVirtualNetworkCreation() {
		final String id = "test";
		ModelFacade.getInstance().addNetworkToRoot(id, true);
		assertEquals(1, ModelFacade.getInstance().getAllNetworks().size());
		assertTrue(ModelFacade.getInstance().getNetworkById(id) instanceof VirtualNetwork);
	}
	
	@Test
	public void testGetNetworkById() {
		final String id = "123";
		ModelFacade.getInstance().addNetworkToRoot(id, false);
		assertEquals(id, ModelFacade.getInstance().getNetworkById(id).getName());
	}
	
	@Test
	public void testNetworkExists() {
		final String id = "123";
		assertFalse(ModelFacade.getInstance().networkExists(id));
		ModelFacade.getInstance().addNetworkToRoot(id, false);
		assertTrue(ModelFacade.getInstance().networkExists(id));
	}
	
	@Test
	public void testRejectNetworkIdIfExists() {
		final String id = "123";
		ModelFacade.getInstance().addNetworkToRoot(id, false);
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addNetworkToRoot(id, false);
		});
	}
	
	@Test
	public void testAddSubstrateSwitch() {
		final String id = "sw_1";
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addSwitchToNetwork(id, "net", 0);
		assertEquals(1, ModelFacade.getInstance().getNetworkById("net").getNodes().size());
		assertTrue(ModelFacade.getInstance().getSwitchById(id) instanceof SubstrateSwitch);
	}
	
	@Test
	public void testAddVirtualSwitch() {
		final String id = "sw_1";
		ModelFacade.getInstance().addNetworkToRoot("net", true);
		ModelFacade.getInstance().addSwitchToNetwork(id, "net", 0);
		assertEquals(1, ModelFacade.getInstance().getNetworkById("net").getNodes().size());
		assertTrue(ModelFacade.getInstance().getSwitchById(id) instanceof VirtualSwitch);
	}
	
	@Test
	public void testRejectSwitchIdIfExists() {
		final String id = "123";
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addSwitchToNetwork(id, "net", 0);
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addSwitchToNetwork(id, "net", 0);
		});
	}
	
	@Test
	public void testAddSubstrateServer() {
		final String id = "srv_1";
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addServerToNetwork(id, "net", 0, 0, 0, 0);
		assertEquals(1, ModelFacade.getInstance().getNetworkById("net").getNodes().size());
		assertTrue(ModelFacade.getInstance().getServerById(id) instanceof SubstrateServer);
	}
	
	@Test
	public void testAddVirtualServer() {
		final String id = "srv_1";
		ModelFacade.getInstance().addNetworkToRoot("net", true);
		ModelFacade.getInstance().addServerToNetwork(id, "net", 0, 0, 0, 0);
		assertEquals(1, ModelFacade.getInstance().getNetworkById("net").getNodes().size());
		assertTrue(ModelFacade.getInstance().getServerById(id) instanceof VirtualServer);
	}
	
	@Test
	public void testRejectServerIdIfExists() {
		final String id = "123";
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addServerToNetwork(id, "net", 0, 0, 0, 0);
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addServerToNetwork(id, "net", 0, 0, 0, 0);
		});
	}
	
	@Test
	public void testRejectServerIfNegativeValues() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addServerToNetwork("1", "net", -1, 0, 0, 0);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addServerToNetwork("1", "net", 0, -1, 0, 0);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addServerToNetwork("1", "net", 0, 0, -1, 0);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addServerToNetwork("1", "net", 0, 0, 0, -1);
		});
	}
	
	@Test
	public void testGetAllServers() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addServerToNetwork("1", "net", 0, 0, 0, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "net", 0, 0, 0, 0);
		final List<Node> allServers = ModelFacade.getInstance().getAllServersOfNetwork("net");
		assertEquals(2, allServers.size());
		assertEquals("1", allServers.get(0).getName());
		assertEquals("2", allServers.get(1).getName());
	}
	
	@Test
	public void testGetServerById() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addServerToNetwork("1", "net", 1, 2, 3, 4);
		Server ret = ModelFacade.getInstance().getServerById("1");
		assertEquals("1", ret.getName());
		assertEquals(1, ret.getCpu());
		assertEquals(2, ret.getMemory());
		assertEquals(3, ret.getStorage());
		assertEquals(4, ret.getDepth());
	}
	
	@Test
	public void testGetSwitchById() {
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addSwitchToNetwork("sw", "net", 5);
		Switch ret = ModelFacade.getInstance().getSwitchById("sw");
		assertEquals("sw", ret.getName());
		assertEquals(5, ret.getDepth());
	}
	
	@Test
	public void testAddSubstrateLink() {
		final String id = "link_1";
		ModelFacade.getInstance().addNetworkToRoot("net", false);
		ModelFacade.getInstance().addServerToNetwork("1", "net", 0, 0, 0, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "net", 0, 0, 0, 0);
		ModelFacade.getInstance().addLinkToNetwork(id, "net", 0, "1", "2");
		assertEquals(1, ModelFacade.getInstance().getNetworkById("net").getLinks().size());
		assertTrue(ModelFacade.getInstance().getLinkById(id) instanceof SubstrateLink);
	}
	
	@Test
	public void testAddVirtualLink() {
		final String id = "link_1";
		ModelFacade.getInstance().addNetworkToRoot("net", true);
		ModelFacade.getInstance().addServerToNetwork("1", "net", 0, 0, 0, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "net", 0, 0, 0, 0);
		ModelFacade.getInstance().addLinkToNetwork(id, "net", 0, "1", "2");
		assertEquals(1, ModelFacade.getInstance().getNetworkById("net").getLinks().size());
		assertTrue(ModelFacade.getInstance().getLinkById(id) instanceof VirtualLink);
	}
	
	@Test
	public void testRejectLinkIdIfExists() {
		final String id = "link_1";
		ModelFacade.getInstance().addNetworkToRoot("net", true);
		ModelFacade.getInstance().addServerToNetwork("1", "net", 0, 0, 0, 0);
		ModelFacade.getInstance().addServerToNetwork("2", "net", 0, 0, 0, 0);
		ModelFacade.getInstance().addLinkToNetwork(id, "net", 0, "1", "2");
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addLinkToNetwork(id, "net", 0, "1", "2");
		});
	}
	
	@Test
	public void testRejectLinkIfSourceNodeDoesNotExist() {
		final String id = "link_1";
		ModelFacade.getInstance().addNetworkToRoot("net", true);
		ModelFacade.getInstance().addSwitchToNetwork("1", "net", 0);
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addLinkToNetwork(id, "net", 0, "inexistend", "1");
		});
	}
	
	@Test
	public void testRejectLinkIfTargetNodeDoesNotExist() {
		final String id = "link_1";
		ModelFacade.getInstance().addNetworkToRoot("net", true);
		ModelFacade.getInstance().addSwitchToNetwork("1", "net", 0);
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addLinkToNetwork(id, "net", 0, "1", "inexistend");
		});
	}
	
	@Test
	public void testAddIdIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addNetworkToRoot(null, false);
		});
	}
	
	@Test
	public void testAddIdIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			ModelFacade.getInstance().addNetworkToRoot("", false);
		});
	}
	
}
