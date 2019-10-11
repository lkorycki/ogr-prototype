package ogr;

import java.awt.EventQueue;

import ogr.controller.MainWindowController;
import ogr.view.MainWindowView;

public class Launcher {
	
	/**
	 * Passes arguments to the application launcher method.
	 * @param args
	 */
	public static void main(String[] args) {
		launchApplication(args);
	}
	
	/**
	 * Launches the application by creating it's main window.
	 * @param args
	 */
	private static void launchApplication(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Create application base model
					MApplication mapp = new MApplication();
					
					// Give controller access to the application 
					MainWindowController mwc = new MainWindowController(mapp);
					
					// Initialize main window view
					MainWindowView mwv = new MainWindowView(mwc);
					mapp.setMainWindow(mwv);
				} catch (Exception e) {
					System.err.println("Error initializing main window of the application!");
					e.printStackTrace(System.err);
				}
			}
		});
	}
}
