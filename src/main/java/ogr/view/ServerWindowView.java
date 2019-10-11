package ogr.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JButton;

import ogr.controller.ServerWindowController;
import ogr.util.ImagePanel;

public class ServerWindowView extends JDialog {

	private ServerWindowController controller;
	private JPanel contentPane;
	private JSplitPane splitPane;
	private JPanel bottomButtons;
	private ImagePanel imagePanel;
	private Vector<String> photoList;
	
	public ServerWindowView(ServerWindowController controller) {
		super();
		this.controller = controller; // give the reference
		setTitle("Load file from server...");
		setWindowSize(600, 300);
		
		photoList = controller.getPhotoList();
		if (photoList == null) dispose(); // no photos were returned
		else {
			contentPaneSetup();
			setVisible(true);
		}
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
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		
		// Split the window horizontally
		splitPane = new JSplitPane();
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		JList<String> list = new JList<String>(photoList);
		ListSelectionModel lsm = list.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty() || e.getValueIsAdjusting()) return;
				int idx = lsm.getMinSelectionIndex();
				int last_idx = lsm.getMaxSelectionIndex();
		        if (idx == last_idx) {
		        	String imageName = controller.getPhotoList().get(idx);
		        	File imageFile = controller.getPhoto(imageName);
		        	imagePanel.setImage(imageFile);
		        	controller.setSelectedFile(idx);
		        }
			}
		});
		scrollPane.setViewportView(list);
		scrollPane.setMinimumSize(new Dimension(150, 0));
		
		imagePanel = new ImagePanel();
		splitPane.setRightComponent(imagePanel);
		
		bottomButtons = new JPanel();
		contentPane.add(bottomButtons, BorderLayout.SOUTH);
		bottomButtons.setLayout(new FlowLayout());
		
		JButton downButton = new JButton("Download");
		downButton.setPreferredSize(new Dimension(90,20));
		downButton.addActionListener(controller.okButtonListener());
		bottomButtons.add(downButton);
	}
	
}
