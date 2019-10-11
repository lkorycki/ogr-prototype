package ogr.controller;

import java.awt.EventQueue;

import javax.swing.JTabbedPane;

import ogr.MApplication;
import ogr.view.ProcessingWindowView;


public class MainPanelController extends Controller {
	
	private JTabbedPane tabbedPane;
	
	public MainPanelController(MApplication _mapp) {
		super(_mapp);
	}
	
	public void setPanels(JTabbedPane _tabbedPane)	{
		tabbedPane = _tabbedPane;
	}
	
	public void changeTabbedPane(int idx)	{
		tabbedPane.setSelectedIndex(idx);
	}
	
	public void cnvImage2Graph()	{
		launchProcessingWindow();		
	}
	
	public void launchProcessingWindow()	{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {										
					// Give controller access to the application 
					ProcessingWindowController wwc = new ProcessingWindowController(getMApp());
					
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
