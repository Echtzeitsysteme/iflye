package visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import facade.ModelFacade;
import model.VirtualNetwork;

/**
 * Visualization UI based on GraphStream, that can show network topologies based
 * on models read from XMI files.
 *
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class Application extends JFrame {
	
	private static final long serialVersionUID = -2863860255459271789L;
	
	private static final String APP_NAME = "Network Explorer";

	/**
	 * The NetworkGraph to render in the Frame
	 */
	private final NetworkGraph graph = new NetworkGraph("Network Viewer");
	
	/**
	 * The viewer where the graph is embedded.
	 */
	private SwingViewer graphViewer;
	
	/**
	 * The List for viewing all available networks.
	 */
	private final JList<String> networkList = new JList<>();
	
	/**
	 * All available networks to view.
	 */
	private List<String> networks = new LinkedList<>();
	
	/**
	 * The path to the currently loaded file.
	 */
	private String loadedModelFilePath;
	
	/**
	 * The name of the currently viewed network.
	 */
	private String networkId;
	
	/**
	 * If the graph view should be automatically laid out.
	 */
	private boolean autoLayout;
	
	/**
	 * If the view is currently ready for rerender. Prevents unintentional rerenders while rendering.
	 */
	private boolean ready = false;

	/**
	 * Main method that starts the visualization process.
	 *
	 * @param args First string is the path of the model to load, second string is
	 *             the name/ID of the network to visualize, and third parameter
	 *             (0/1) disables automatic shaping (enabled by default).
	 */
	public static void main(final String[] args) {
		try {
			System.setProperty("org.graphstream.ui", "swing");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
	        System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
	        System.setProperty("apple.awt.application.name", APP_NAME);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			// Continue to launch the app without proper design
			e.printStackTrace();
		}

		new Application(args.length < 1 ? null : args[0], args.length < 2 ? null : args[1], args.length < 3 || !"0".equals(args[2]));
	}
	
	/**
	 * Creates a new UI application with an embedded NetworkGraph for visualization.
	 * 
	 * @param path       Path to read file from.
	 */
	public Application(final String path) {
		this(path, null);
	}
	
	/**
	 * Creates a new UI application with an embedded NetworkGraph for visualization.
	 * 
	 * @param path       Path to read file from.
	 * @param networkId  Network ID of the network to visualize.
	 */
	public Application(final String path, final String networkId) {
		this(path, networkId, true);
	}
	
	/**
	 * Creates a new UI application with an embedded NetworkGraph for visualization.
	 * 
	 * @param path       Path to read file from.
	 * @param networkId  Network ID of the network to visualize.
	 * @param autoLayout If the graph should be automatically laid out
	 */
	public Application(final String path, final String networkId, final boolean autoLayout) {
		init();
		initMenu();
		initGraph();
		initToolbar();
		initNetworkList();

		final String selection = determineFilePath(path);
		if (selection == null) {
			System.exit(0);
			return;
		}
		
		loadModelFile(selection);

		refreshNetworkList();
		setNetworkId(networkId != null ? networkId : networks.getFirst());
		setAutoLayout(autoLayout);

		this.ready = true;
		rerender();

		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setVisible(true);
	}
	
	/**
	 * Initialize basic Frame attributes.
	 */
	private void init() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(800, 600));
		this.setSize(getPreferredSize());
		this.setBackground(Color.lightGray);
	}
	
	/**
	 * Initialize the menu bar.
	 */
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menu = new JMenu("File");
		JMenuItem menuItemOpen = new JMenuItem("Open Model...", KeyEvent.VK_T);
		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.META_MASK));
		menuItemOpen.addActionListener((ActionEvent e) -> {
			final String path = selectFile();
			if (path == null) {
				return;
			}
			
			loadModelFile(path);
			rerender();
		});
		menu.add(menuItemOpen);
		menuBar.add(menu);

		this.setJMenuBar(menuBar);
	}
	
	/**
	 * Initialize the graph view.
	 */
	private void initGraph() {
		this.graphViewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);

		View view = this.graphViewer.addDefaultView(false);   // false indicates "no JFrame".
		this.add((DefaultView) view, BorderLayout.CENTER);
	}
	
	/**
	 * Initialize the toolbar.
	 */
	private void initToolbar() {
	    JButton button = new JButton("Reload");
	    button.setBounds(150, 200, 220, 50);
	    this.add(button, BorderLayout.PAGE_START);

	    button.addActionListener((ActionEvent e) -> {
	    	loadModelFile(this.loadedModelFilePath);
	    	rerender();
	    });
	}
	
	/**
	 * Initialize the network list view.
	 */
	private void initNetworkList() {
		networkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		networkList.setLayoutOrientation(JList.VERTICAL);
		networkList.setVisibleRowCount(-1);
		networkList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				setNetworkId(networkList.getSelectedValue());
				rerender();
			}
		});

		JScrollPane listScroller = new JScrollPane(networkList);
		listScroller.setPreferredSize(new Dimension(250, 80));
		
		this.add(listScroller, BorderLayout.LINE_START);
	}
	
	/**
	 * Set the network ID to another value.
	 * 
	 * @param networkId
	 */
	public void setNetworkId(final String networkId) {
		if (!networks.contains(networkId)) {
			throw new RuntimeException("The supplied networkId " + networkId + " is invalid!");
		}

		this.networkId = networkId;
	}
	
	/**
	 * Toggle the auto layout of the graph view.
	 * 
	 * @param autoLayout
	 */
	public void setAutoLayout(final boolean autoLayout) {
		this.autoLayout = autoLayout;
	}
	
	/**
	 * Enable the auto layout of the graph view.
	 */
	public void enableAutoLayout() {
		setAutoLayout(true);
	}
	
	/**
	 * Disable the auto layout of the graph view.
	 */
	public void disableAutoLayout() {
		setAutoLayout(true);
	}
	
	/**
	 * Opens a FileChooser to select a file. Current path is last loaded model file path or falling back to current dir.
	 * 
	 * @return
	 */
	public String selectFile() {
		final String currentDirectory = this.loadedModelFilePath != null ? this.loadedModelFilePath : System.getProperty("user.dir");
		
		return selectFile(currentDirectory);
	}
	
	/**
	 * Opens a FileChooser to select a file at the specified location.
	 * 
	 * @param currentDirectoryPath
	 * @return
	 */
	public String selectFile(final String currentDirectoryPath) {
		JFileChooser fileChooser = new JFileChooser(currentDirectoryPath);
		int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            return file.getPath();
        }
        
        return null;
	}
	
	/**
	 * Select a file path based on the supplied path.
	 * 
	 * @param path
	 * @return
	 */
	private String determineFilePath(final String path) {
		if (path == null) {
			return selectFile();
		}
		
		File f = new File(path);
		if (!f.exists()) {
			return selectFile();
		}
		
		if (f.isDirectory()) {
			return selectFile(path);
		}
			
		return path;
	}
	
	/**
	 * Load the model from the supplied path.
	 * 
	 * @param path The path to the model to load.
	 */
	public void loadModelFile(final String path) {
		this.loadedModelFilePath = relativizePath(path);
		ModelFacade.getInstance().loadModel(this.loadedModelFilePath);
		graph.setModel(ModelFacade.getInstance());
	}
	
	/**
	 * Converts a path into a path relative to the current working directory.
	 * Necessary for loading models with the ModelFacade.
	 * 
	 * @param path
	 * @return
	 */
	private String relativizePath(final String path) {
		return Paths.get(System.getProperty("user.dir")).relativize(Paths.get(path).toAbsolutePath()).toString();
	}
	
	/**
	 * Updates the full UI with all changed configuration values.
	 */
	public void rerender() {
		if (!this.ready) {
			return;
		}

		this.ready = false;

		refreshNetworkList();
		refreshGraph();

		this.setTitle(this.loadedModelFilePath + " – " + this.networkId);
		
		this.ready = true;
	}
	
	/**
	 * Refreshes the graph view with updated values.
	 */
	public void refreshGraph() {
		graph.clear();
		graph.render(this.networkId);
		
		if (!this.autoLayout) {
    		graphViewer.disableAutoLayout();
    	} else {
    		graphViewer.enableAutoLayout();
    	}
	}
	
	/**
	 * Refreshes the network list view with updated values. Resets the currently selected networkId if necessary.
	 */
	public void refreshNetworkList() {
		networks = ModelFacade.getInstance().getAllNetworks().stream().filter((network) -> !(network instanceof VirtualNetwork)).map((network) -> network.getName()).toList();
		
		if (!networks.contains(this.networkId)) {
			setNetworkId(networks.getFirst());
		}

		networkList.setListData(networks.toArray(String[]::new));
		networkList.setSelectedValue(this.networkId, false);
	}

}
