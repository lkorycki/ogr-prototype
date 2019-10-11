package ogr.view;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import ogr.controller.MenuBarController;

public class MenuBarView extends JMenuBar {

	private JMenu fileMenu;
	private JMenu helpMenu;
	private JMenu newMenu;
	
	public MenuBarView(MenuBarController controller) {
		super();
		
		// Menu labels...
		add(fileMenu = new JMenu("File"));
		add(helpMenu = new JMenu("Settings"));
		fileMenu.add(newMenu = new JMenu("New"));
		
		// Menu items...
		newMenu.add("from file...").addActionListener(controller.getLoadFileListener());
		newMenu.add("from server...").addActionListener(controller.getLoadFileFromServerListener());
		fileMenu.add("OGR it!").addActionListener(controller.getOgrItListener());
		fileMenu.add("OCR it!").addActionListener(controller.getOcrItListener());
		//fileMenu.add("Export"); // TODO
		fileMenu.add("Save").addActionListener(controller.getSaveListener());
		fileMenu.add("Rename").addActionListener(controller.getRenameListener());
		fileMenu.add("Delete").addActionListener(controller.getDeleteListener());
		fileMenu.add("Exit").addActionListener(controller.getExitListener());
		//helpMenu.add("How-to"); // TODO
		helpMenu.add("Change text color").addActionListener(controller.getTextColorListener());
		helpMenu.add("Change server address").addActionListener(controller.getServerAddressListener());
		helpMenu.add("About").addActionListener(controller.getShowAboutListener());
	}
}
