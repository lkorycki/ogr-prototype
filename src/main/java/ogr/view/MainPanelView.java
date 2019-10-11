package ogr.view;

import javax.swing.JPanel;

import ogr.controller.GraphPanelController;
import ogr.controller.MainPanelController;
import ogr.util.ImagePanel;

import javax.swing.JTabbedPane;

import java.awt.BorderLayout;

public class MainPanelView extends JPanel {
	
	private MainPanelController controller;
	private JTabbedPane tabbedPane;
	
	public MainPanelView(MainPanelController _controller) {
		super();
		controller = _controller;
		setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);
		
		ImagePanel imagePanel = new ImagePanel();
		tabbedPane.addTab("Image", null, imagePanel, null);
		
		GraphPanelController gpc = new GraphPanelController(controller.getMApp());
		GraphPanelView gpv = new GraphPanelView(gpc);
		gpc.setPanel(gpv);
		tabbedPane.addTab("Graph", null, gpv, null);
	}
	
	public MainPanelController getController()	{
		return controller;
	}
	
	public JTabbedPane getTabbedPanel()	{
		return tabbedPane;
	}
	
	public GraphPanelView getGraphPanel()	{
		return (GraphPanelView) tabbedPane.getComponent(1);
	}
	
	public ImagePanel getImagePanel()	{
		return (ImagePanel) tabbedPane.getComponent(0);
	}

	public void resetView() {
		getImagePanel().setImage(null);
		getGraphPanel().setGraph(null);
	}
	
}
