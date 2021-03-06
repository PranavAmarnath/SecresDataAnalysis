package com.secres;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * <code>View</code> creates and initializes the <i>whole</i> GUI of the Main application.<br>
 * The GUI uses standard Swing components with nested {@link JPanel}s.
 * 
 * The main layout used is {@link CardLayout} for an MDI with easy visualization.
 * <P>
 * There are two {@link JTable}s each with their own dataset. {@link GraphCharts} reads from the tables for the visualization.
 * <P>
 * All of <code>View</code>'s components are accessed on the EDT per Swing's threading rules.
 * 
 * @author Pranav Amarnath
 *
 */
public class View {

	/** Main frame */
	private static JFrame frame;
	
	/** Main panel */
	private JPanel mainPanel;
	/** Tree panel */
	private JPanel treePanel;
	/** Graph panel */
	private JPanel graphPanel;
	/** Panel with Cards - CardLayout */
	private JPanel cardsPanel;
	/** Filler panel with no node selected. */
	private JPanel emptyPanel;
	/** Table data */
	private JPanel tableGlobalPanel, tableCountryPanel;
	
	private final String EMPTYPANEL = "Card that is empty";
	private final String TABLEGLOBALPANEL = "Card with Global Data Table";
	private final String TABLECOUNTRYPANEL = "Card with Country Data Table";
	
	private final String ALLLINEPANEL = "Card with Basic Line Chart";
	private final String AVGLINEPANEL = "Card with Average Temperature Line Chart";
	private final String AVGSCATTERPANEL = "Card with Average Temperature Scatter Plot";
	private final String SEASONPANEL = "Card with Temperatures from Seasons Scatter Plot";
	private final String THERMOMETERPANEL = "Card with Temperatures from Averages into Thermometer";
	private final String AVGBARPANEL = "Card with Average Temperature Bar Chart";
	private final String DOUBLEBARGREATESTPANEL = "Card with Most Difference in a century Bar Chart";
	private final String DOUBLEBARLEASTPANEL = "Card with Least Difference in a century Bar Chart";
	private final String MULTILINEECONOMYPANEL = "Card with Top 5 Economies Line Chart";
	
	private JSplitPane splitPane;
	private JScrollPane treeScroll;
	private JScrollPane tableGlobalScroll, tableCountryScroll;
	
	private JTree componentTree;
	private DefaultMutableTreeNode root, dataRootNode, globalTableNode, countryTableNode;
	private DefaultMutableTreeNode graphRootNode;
	private DefaultMutableTreeNode globalNode, basicLineNode, basicLineByYearNode, basicScatterByYearNode, basicScatterCoolingNode, thermometerSlideNode;
	private DefaultMutableTreeNode countryNode, basicBarNode, doubleBarGreatestNode, doubleBarLeastNode, multiLineEconomyNode;
	
	/** <code>JTable</code> data */
	private static JTable tableGlobalData, tableCountryData;
	
	private JMenuBar menuBar;
	private JMenu file, view;
	private ButtonGroup viewGroup;
	private JMenuItem about, preferences, close;
	private JMenuItem light;
	private static JMenuItem nimbus;
	private static boolean nimbusEnabled = false;
	private JMenuItem system;
	
	private FlatLightLaf lightLaf = new FlatLightLaf();
	private FlatDarkLaf darkLaf = new FlatDarkLaf();
	//private StandardChartTheme themeLight = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
	//private StandardChartTheme themeDark = (StandardChartTheme) StandardChartTheme.createDarknessTheme();
	
	/** Default constructor */
	public View() {
		createGUI();
	}
	
	/** Creates main GUI. */
	private void createGUI() {
		frame = new JFrame("Secres GUI") {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 600);
			}
		};
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				createQuit(frame);
			}
		});

		/** Reference: @see https://stackoverflow.com/a/56924202/13772184 */
		// loading an image from a file
		Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		URL imageResource = View.class.getResource("/gear.png"); // URL: https://cdn.pixabay.com/photo/2012/05/04/10/57/gear-47203_1280.png
		Image image = defaultToolkit.getImage(imageResource);

		try {
			Taskbar taskbar = Taskbar.getTaskbar();
			// set icon for mac os (and other systems which do support this method)
			taskbar.setIconImage(image);
		} catch (UnsupportedOperationException e) {
			// set icon for windows (and other systems which do support this method)
			frame.setIconImage(image);
		}

		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		file = new JMenu("File");
		view = new JMenu("View");
		menuBar.add(file);
		menuBar.add(view);
		
		light = new JRadioButtonMenuItem("Light");
		light.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					UIManager.setLookAndFeel(lightLaf);
				} catch (UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
				SwingUtilities.updateComponentTreeUI(frame);
			}
		});
		nimbus = new JRadioButtonMenuItem("Nimbus");
		nimbus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
				    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				        if ("Nimbus".equals(info.getName())) {
				            UIManager.setLookAndFeel(info.getClassName());
				            break;
				        }
				    }
				} catch (Exception e1) {
				    // If Nimbus is not available, you can set the GUI to another look and feel.
					JOptionPane.showMessageDialog(frame, "Nimbus is not available! Requires JRE 6 or later.", "Error", JOptionPane.ERROR_MESSAGE);
					try {
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					} catch (Exception e2) {}
				}
				SwingUtilities.updateComponentTreeUI(frame);
			}
		});
		system = new JRadioButtonMenuItem(System.getProperty("os.name").toString());
		system.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e3) {
					e3.printStackTrace();
				}
				SwingUtilities.updateComponentTreeUI(frame);
			}
		});
		view.add(light);
		view.addSeparator();
		view.add(nimbus);
		view.add(system);
		viewGroup = new ButtonGroup();
		viewGroup.add(light);
		light.setSelected(true);
		viewGroup.add(nimbus);
		viewGroup.add(system);
		
		about = new JMenuItem("About");
		file.add(about);
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createAbout(frame);
			}
		});
		preferences = new JMenuItem("Preferences");
		file.add(preferences);
		/** Sets Ctrl + Comma (',') accelerator for 'Preferences' <code>JMenuItem</code> */
		//preferences.setAccelerator(KeyStroke.getKeyStroke(',', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		preferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createPreferences(frame);
			}
		});
		close = new JMenuItem("Close");
		file.add(close);
		close.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createQuit(frame);
			}
		});
		
		mainPanel = new JPanel(new BorderLayout());
		frame.add(mainPanel);
		
		treePanel = new JPanel(new BorderLayout());
		treeScroll = new JScrollPane();
		
		componentTree = new JTree(setTreeModel());
		componentTree.setShowsRootHandles(true);
		componentTree.collapseRow(0);
		componentTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) componentTree.getLastSelectedPathComponent();
				componentTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
				if(node == globalTableNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, TABLEGLOBALPANEL);
				}
				else if(node == countryTableNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, TABLECOUNTRYPANEL);
				}
				else if(node == basicLineNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, ALLLINEPANEL);
				}
				else if(node == basicLineByYearNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, AVGLINEPANEL);
				}
				else if(node == basicScatterByYearNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, AVGSCATTERPANEL);
				}
				else if(node == basicScatterCoolingNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, SEASONPANEL);
				}
				else if(node == thermometerSlideNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, THERMOMETERPANEL);
				}
				else if(node == basicBarNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, AVGBARPANEL);
				}
				else if(node == doubleBarGreatestNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, DOUBLEBARGREATESTPANEL);
				}
				else if(node == doubleBarLeastNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
					cl.show(cardsPanel, DOUBLEBARLEASTPANEL);
				}
				else if(node == multiLineEconomyNode) {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
					cl.show(cardsPanel, MULTILINEECONOMYPANEL);
				}
				else {
					CardLayout cl = (CardLayout)(cardsPanel.getLayout());
				    cl.show(cardsPanel, EMPTYPANEL);
				}
				//treePanel.repaint(); // removes Nimbus Laf paint artifacts
			}
		});
		
		treeScroll.setViewportView(componentTree);
		treePanel.add(treeScroll);
		
		graphPanel = new JPanel(new BorderLayout());
		cardsPanel = new JPanel(new CardLayout());
		//cardsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		tableGlobalPanel = new JPanel(new BorderLayout());
		tableCountryPanel = new JPanel(new BorderLayout());
		
		tableGlobalData = new JTable(Main.getGlobalModel().getModel()) {
			@Override
			public boolean isCellEditable(int row, int column) {  
				return false;
			}
		};
		
		tableCountryData = new JTable(Main.getCountryModel().getModel()) {
			@Override
			public boolean isCellEditable(int row, int column) {  
				return false;
			}
		};
		
		tableGlobalData.setCellSelectionEnabled(true);
		tableGlobalData.setToolTipText("<html>Display of CSV Data in Table format.<br>Double-click/CTRL+C on a cell to copy text.</html>");
        tableGlobalData.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		        handleCopy(tableGlobalData, e);
		    }
		});
        tableCountryData.setCellSelectionEnabled(true);
		tableCountryData.setToolTipText("<html>Display of CSV Data in Table format.<br>Double-click/CTRL+C on a cell to copy text.</html>");
        tableCountryData.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		        handleCopy(tableCountryData, e);
		    }
		});
		
		tableGlobalScroll = new JScrollPane(tableGlobalData);
		//tableGlobalScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		tableCountryScroll = new JScrollPane(tableCountryData);
		
		//tableGlobalData.setAutoCreateRowSorter(true);
		tableGlobalData.setAutoResizeMode(0);
		tableGlobalData.getTableHeader().setReorderingAllowed(false);
		
		//tableCountryData.setAutoCreateRowSorter(true);
		tableCountryData.setAutoResizeMode(0);
		tableCountryData.getTableHeader().setReorderingAllowed(false);
		
		tableGlobalPanel.add(tableGlobalScroll);
		tableCountryPanel.add(tableCountryScroll);
		
		emptyPanel = new JPanel(new BorderLayout());
		JLabel emptyLabel = new JLabel("Click nodes on the left to display items.", JLabel.CENTER);
		emptyLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
		emptyLabel.setForeground(new Color(150, 150, 150));
		emptyPanel.add(emptyLabel);
		
		cardsPanel.add(emptyPanel, EMPTYPANEL);
		cardsPanel.add(tableGlobalPanel, TABLEGLOBALPANEL);
		cardsPanel.add(tableCountryPanel, TABLECOUNTRYPANEL);
		CardLayout cl = (CardLayout)(cardsPanel.getLayout());
		//System.out.println(SwingUtilities.isEventDispatchThread());
	    cl.show(cardsPanel, EMPTYPANEL);
		
	    cardsPanel.add(GraphCharts.getBasicLineChart().updateView(), ALLLINEPANEL);
	    cardsPanel.add(GraphCharts.getAvgChart().updateViewLine(), AVGLINEPANEL);
	    cardsPanel.add(GraphCharts.getAvgChart().updateViewScatter(), AVGSCATTERPANEL);
	    cardsPanel.add(GraphCharts.getSeasonsCharts().updateView(), SEASONPANEL);
	    cardsPanel.add(GraphCharts.getBarChart().updateView(), AVGBARPANEL);
	    cardsPanel.add(GraphCharts.getBarChartChange().updateViewGreatest(), DOUBLEBARGREATESTPANEL);
	    cardsPanel.add(GraphCharts.getBarChartChange().updateViewLeast(), DOUBLEBARLEASTPANEL);
	    cardsPanel.add(GraphCharts.getEconomyChart().updateView(), MULTILINEECONOMYPANEL);
	    cardsPanel.add(GraphCharts.getThermometerChart().updateView(), THERMOMETERPANEL);
		
		graphPanel.add(cardsPanel);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, graphPanel);
		splitPane.setContinuousLayout(true);
		mainPanel.add(splitPane);
		
		frame.pack();
		splitPane.setResizeWeight(0.25);
		splitPane.setDividerLocation(0.25);
	}
	
	/**	@return The <code>DefaultMutableTreeNode</code> for the <code>JTree</code> */
	private DefaultMutableTreeNode setTreeModel() {
		root = new DefaultMutableTreeNode("Access");
		dataRootNode = new DefaultMutableTreeNode("Data");
		root.add(dataRootNode);
		
		globalTableNode = new DefaultMutableTreeNode("Global Data");
        dataRootNode.add(globalTableNode);
        countryTableNode = new DefaultMutableTreeNode("Country Data");
        dataRootNode.add(countryTableNode);
        
		graphRootNode = new DefaultMutableTreeNode("Graphs");
		root.add(graphRootNode);
		
		globalNode = new DefaultMutableTreeNode("Global Data Graphs");
		basicLineNode = new DefaultMutableTreeNode("Basic Line Chart");
		globalNode.add(basicLineNode);
		basicLineByYearNode = new DefaultMutableTreeNode("Line Chart By Year");
		globalNode.add(basicLineByYearNode);
		basicScatterByYearNode = new DefaultMutableTreeNode("Scatter Plot By Year");
		globalNode.add(basicScatterByYearNode);
		basicScatterCoolingNode = new DefaultMutableTreeNode("Scatter Plot By Season");
		globalNode.add(basicScatterCoolingNode);
		thermometerSlideNode = new DefaultMutableTreeNode("Thermometer Averages");
		globalNode.add(thermometerSlideNode);
		
		countryNode = new DefaultMutableTreeNode("Country Data Graphs");
		basicBarNode = new DefaultMutableTreeNode("Bar Chart By Country");
		countryNode.add(basicBarNode);
		doubleBarGreatestNode = new DefaultMutableTreeNode("Greatest Increase in Temp.");
		countryNode.add(doubleBarGreatestNode);
		doubleBarLeastNode = new DefaultMutableTreeNode("Least Increase in Temp.");
		countryNode.add(doubleBarLeastNode);
		multiLineEconomyNode = new DefaultMutableTreeNode("Top 5 Economies Temp.");
		countryNode.add(multiLineEconomyNode);
		
		graphRootNode.add(globalNode);
		graphRootNode.add(countryNode);
        
        return root;
	}
	
	/** @return The main <code>JFrame</code> */
	public static JFrame getFrame() {
		return frame;
	}
	
	/** @return The Global <code>JTable</code> */
	public static JTable getGlobalTable() {
		return tableGlobalData;
	}
	
	/** @return The Country <code>JTable</code> */
	public static JTable getCountryTable() {
		return tableCountryData;
	}
	
	/**
	 * Handles {@link Clipboard} mechanism for tables
	 * 
	 * @param table  <code>JTable</code> to put copy mechanism
	 * @param e  <code>MouseEvent</code>
	 */
	private void handleCopy(JTable table, MouseEvent e) {
		if(e.getClickCount() == 2) { /** e.getClickCount() == 2 -> double-click */
            //System.out.println("right click");
            Point p = e.getPoint();
            int row = table.rowAtPoint(p);
            int col = table.columnAtPoint(p);
            Object value = table.getValueAt(row, col);
            StringSelection stringSelection = new StringSelection(value.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);
            JOptionPane.showMessageDialog(frame, "Copied text!\nHover the mouse over the table for more info.");
        }
	}
	
	/**
	 * Converts a given Image into a BufferedImage
	 * 
	 * @param img The Image to be converted
	 * @return The converted <code>BufferedImage</code>
	 */
	public static BufferedImage toBufferedImage(Image img) {
		/** Reference: @see https://stackoverflow.com/a/13605411 */
	    if (img instanceof BufferedImage) {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	
	/** Instantiates About dialog */
	static void createAbout() {
		createAbout(null);
	}
	
	/**
	 * Instantiates About dialog
	 * @param frame
	 */
	static void createAbout(JFrame frame) {
		/*
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
    	//textArea.setText("\u00a9 2020-2021 Pranav Amarnath");
		// Add LICENSE text
    	textArea.setText(LICENSE);
    	JScrollPane scrollPane = new JScrollPane(textArea);
    	*/
		JPanel mainPanel = new JPanel(new BorderLayout());
		URL imageResource = Main.class.getResource("/gear.png"); // URL: https://cdn.pixabay.com/photo/2012/05/04/10/57/gear-47203_1280.png
		BufferedImage img = toBufferedImage(new ImageIcon(imageResource).getImage());
		JLabel icon = new JLabel();
		icon.setIcon(new ImageIcon(img));
		Image dimg = img.getScaledInstance(49, 51, Image.SCALE_SMOOTH);
		icon.setIcon(new ImageIcon(dimg));
		JPanel imgPanel = new JPanel();
		imgPanel.add(icon);
		mainPanel.add(imgPanel);
		
		JPanel text1Panel = new JPanel();
		JPanel text2Panel = new JPanel();
		//JPanel text3Panel = new JPanel();
		JLabel text1 = new JLabel("Version 1.0", SwingConstants.CENTER);
		text1Panel.add(text1);
		JLabel text2 = new JLabel("<html>Copyright \u00a9 2020-2021 Pranav Amarnath<br><div style='text-align: center;'>All Rights Reserved.</div></html>", SwingConstants.CENTER);
		text2Panel.add(text2);
		//JLabel text3 = new JLabel("All Rights Reserved.", SwingConstants.CENTER);
		//text3Panel.add(text3);
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
		textPanel.add(text1Panel);
		textPanel.add(text2Panel);
		//textPanel.add(text3Panel);
		mainPanel.add(textPanel, BorderLayout.SOUTH);
		
		JOptionPane.showOptionDialog(frame, mainPanel, "About", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
	}
	
	/** Instantiates Preferences dialog */
	static void createPreferences() {
		createPreferences(null);
	}
	
	/**
	 * Instantiates Preferences dialog
	 * @param frame
	 */
	static void createPreferences(JFrame frame) {
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JButton helpButton = new JButton("Help");
		helpButton.setToolTipText("Go to home website");
		helpButton.putClientProperty("JButton.buttonType", "help");
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setText("An error occurred.\nThe current system does not support Desktop.\nRefer to this link instead:\nhttps://github.com/PranavAmarnath/RisingTemperatures");
		helpButton.addActionListener(e -> {
			try {
			    Desktop.getDesktop().browse(new URL("https://github.com/PranavAmarnath/RisingTemperatures").toURI());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(frame, textArea, "Error", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		});
		JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		helpPanel.add(helpButton);
		northPanel.add(helpPanel);
		
		mainPanel.add(northPanel, BorderLayout.NORTH);
		
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		
		/** Reference: @see https://stackoverflow.com/a/4059365 */
		StyledDocument doc = textPane.getStyledDocument();
		
		SimpleAttributeSet headers = new SimpleAttributeSet();
		StyleConstants.setBold(headers, true);
		StyleConstants.setFontSize(headers, 17);
		StyleConstants.setForeground(headers, Color.RED);
		
		SimpleAttributeSet text = new SimpleAttributeSet();
		StyleConstants.setFontSize(text, 14);
		StyleConstants.setForeground(text, Color.BLUE);
		
		try {
			doc.insertString(doc.getLength(), "Table Info:\n", headers);
			doc.insertString(doc.getLength(), "Double click over a cell to copy text.\n", text);
			doc.insertString(doc.getLength(), "Press Ctrl-C to copy multiple cells or row(s).\n", text);
			doc.insertString(doc.getLength(), "Hover over the table for similar info.\n", text);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		try {
			doc.insertString(doc.getLength(), "Graph How-to (Interactive):\n", headers);
			doc.insertString(doc.getLength(), "Right-click on any graph for more preferences.\n", text);
			doc.insertString(doc.getLength(), "Ctrl-press on any graph to pan.\n", text);
			doc.insertString(doc.getLength(), "Scroll the mouse on any graph to zoom.\n", text);
			doc.insertString(doc.getLength(), "Drag on any graph from top left to bottom right to select a zoomed-in portion.\n", text);
			//System.out.println(SwingUtilities.isEventDispatchThread());
			doc.insertString(doc.getLength(), "Drag the mouse up and release (~1 sec.) to exit zoomed state.\n", text);
			doc.insertString(doc.getLength(), "Hover the mouse over any point/bar etc. to view the value.\n", text);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		JScrollPane scrollPane = new JScrollPane(textPane);
		mainPanel.add(scrollPane);
		
        JOptionPane.showMessageDialog(frame, mainPanel, "Preferences", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/** Instantiates Quit dialog. */
	static void createQuit() {
		int input = JOptionPane.showConfirmDialog(null, "Do you want to quit?", "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    	if(input == 0) {
    		System.exit(0);
        }
    	else {
    		frame.setVisible(true);
    	}
	}
	/**
	 * Instantiates Quit dialog.
	 * @param frame
	 */
	static void createQuit(JFrame frame) {
		int input = JOptionPane.showConfirmDialog(frame, "Do you want to quit?", "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    	if(input == 0) {
    		System.exit(0);
        }
    	else {
    		frame.setVisible(true);
    	}
	}
	
}
