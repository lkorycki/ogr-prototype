package ogr.controller;

import ogr.MApplication;

public class Controller {
	
	// Reference to main application
	private MApplication mapp;
	
	/**
	 * Constructor saves the reference to main application.
	 * @param mapp
	 */
	public Controller(MApplication mapp) {
		
		// Set the reference
		this.mapp = mapp;
	}
	
	/**
	 * Returns the reference to main application.
	 * @return
	 */
	public MApplication getMApp() {
		return mapp;
	}
}
