package ogr.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ogr.controller.MainPanelController;
import ogr.controller.MainWindowController;
import ogr.controller.MenuBarController;
import ogr.model.Item;

import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MainWindowView extends JFrame {
	
	private MainWindowController controller;
	private JPanel contentPane;
	private JSplitPane splitPane;
	private JPanel bottomButtons;
	
	public MainWindowView(MainWindowController controller) {
		super();
		this.controller = controller; // give the reference
		setTitle("OCR for Graphs");
		setIcon();
		setWindowSize(800, 600);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		navigationBarSetup();
		contentPaneSetup();
		setVisible(true);
	}
	
	/**
	 * Sets icon for this window.
	 */
	private void setIcon() {
		ImageIcon img = new ImageIcon("src/main/resources/images/ogr_icon_128.png");
		setIconImage(img.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
	}
	
	/**
	 * Set dimensions of the window and center it on screen.
	 * @param width
	 * @param height
	 */
	private void setWindowSize(int width, int height) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(new Rectangle(new Dimension(width, height)));
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2
				- getSize().height / 2);
	}
	
	/**
	 * Creates the content pane.
	 */
	private void contentPaneSetup() {
		// Initialize the content pane
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// Split the window horizontally
		splitPane = new JSplitPane();
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		JList<Item> list = controller.getMApp().getItemList();
		scrollPane.setViewportView(list);
		scrollPane.setMinimumSize(new Dimension(150, 0));
		
		MainPanelController mpc = new MainPanelController(controller.getMApp());
		MainPanelView mpv = new MainPanelView(mpc);
		splitPane.setRightComponent(mpv);
		mpc.setPanels(mpv.getTabbedPanel());
		controller.getMApp().setMainPanel(mpv);
		
		bottomButtons = new JPanel();
		contentPane.add(bottomButtons, BorderLayout.SOUTH);
		bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.X_AXIS));
		
	}
	
	/**
	 * Creates the navigation bar.
	 */
	private void navigationBarSetup() {
		MenuBarController mbc = new MenuBarController(controller.getMApp());
		setJMenuBar(new MenuBarView(mbc));
	}
}
