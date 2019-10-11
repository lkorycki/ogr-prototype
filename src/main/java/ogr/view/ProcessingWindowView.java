package ogr.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;

import ogr.MApplication;
import ogr.controller.ProcessingWindowController;
import ogr.util.ImageCanvas;

public class ProcessingWindowView extends JFrame {
	
	private static ProcessingWindowController controller;
	private JPanel contentPane;
	private JTextPane logText;
	private JButton okButton;
	private JProgressBar progBar;
	private int width = 600, height = 600;
	private ImageCanvas imageCanvas;
	
	public ProcessingWindowView(ProcessingWindowController _controller) {
		super();
		controller = _controller; // give the reference
		setTitle("OGR is working");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setUndecorated(true);
		setResizable(false);
		
		// Size
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(new Rectangle(new Dimension(width, height)));
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2
				- getSize().height / 2);
		
		// Content pane
		contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// Log
		logText = new JTextPane();
		logText.setEditable(false);
		logText.setText("Running...\n");
		JScrollPane scroll = new JScrollPane(logText);
		DefaultCaret caret = (DefaultCaret)logText.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scroll.setPreferredSize(new Dimension(600,100));
		scroll.setMaximumSize(new Dimension(600,100));
		scroll.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		contentPane.add(scroll, BorderLayout.SOUTH);
		
		// OK button
		okButton = new JButton("OK");
		okButton.setPreferredSize(new Dimension(60,20));
		okButton.setEnabled(false);
		okButton.addActionListener(controller.getOkButtonListener());
		JPanel flowPanel = new JPanel(new FlowLayout());
		flowPanel.add(okButton);

		// Progress bar
		progBar = new JProgressBar();
		progBar.setBorder(new EmptyBorder(5,0,5,0));
		progBar.setStringPainted(true);
		
		// Info panel
		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.add(progBar, BorderLayout.NORTH);
		infoPanel.add(scroll, BorderLayout.CENTER);
		infoPanel.add(flowPanel, BorderLayout.SOUTH);
		contentPane.add(infoPanel, BorderLayout.SOUTH);
		
		imageCanvas = new ImageCanvas();
		contentPane.add(imageCanvas, BorderLayout.CENTER);
		setVisible(true);	
		
		controller.startProcessing();
	}
		
	public JTextPane getTextPane()	{
		return logText;
	}
	
	public JButton getOkButton()	{
		return okButton;
	}
	
	public JProgressBar getProgressBar()	{
		return progBar;
	}
	
	public ImageCanvas getImage()	{
		return imageCanvas;
	}
		
	public static void launch() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Create application base model
					MApplication mapp = new MApplication();
										
					// Give controller access to the application 
					ProcessingWindowController wwc = new ProcessingWindowController(mapp);
					
					// Initialize main window view
					ProcessingWindowView wwv = new ProcessingWindowView(wwc);
					wwc.setParentView(wwv);
					
				} catch (Exception e) {
					System.err.println("Error initializing waiting window!");
					e.printStackTrace(System.err);
				}
			}
		});
	}
}
