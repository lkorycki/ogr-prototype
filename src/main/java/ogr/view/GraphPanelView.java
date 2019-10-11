package ogr.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import ogr.controller.GraphPanelController;

import org.jgraph.JGraph;

public class GraphPanelView extends JPanel {
	
	private JGraph graph;
	private GraphPanelController controller;
	private JTextPane editText;
	private JScrollPane graphScroll;
	private JPopupMenu popup;
	private JMenuItem removeItem;
	
	public GraphPanelView(GraphPanelController _controller) {
		super();	
		setLayout(new BorderLayout());
		controller = _controller;
		popup = new JPopupMenu();
		
		// Graph scroll
		graphScroll = new JScrollPane();
		add(graphScroll, BorderLayout.CENTER);
		
		// Edit text panel
		editText = new JTextPane();
		JScrollPane editScroll = new JScrollPane(editText);
		editText.setPreferredSize(new Dimension(0,80));
		editText.addKeyListener(controller.getEditTextListener());
		editScroll.setBorder(BorderFactory.createTitledBorder("Selected cell:"));	
		add(editScroll, BorderLayout.SOUTH);
			
		// Popup
		JMenuItem nodeItem = new JMenuItem("Add node");
		nodeItem.addActionListener(controller.addNodeListener());
		JMenuItem edgeItem = new JMenuItem("Add edge");
		edgeItem.addActionListener(controller.addEdgeListener());
		removeItem = new JMenuItem("Remove");
		removeItem.addActionListener(controller.getRemoveListener());
    	popup.add(nodeItem);
        popup.add(edgeItem);
        popup.add(removeItem);    
	}
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
	
	public void setGraph(JGraph _graph)	{
		if(_graph == null) return;
		graph = _graph;
		graph.addMouseListener(controller.getGraphMouseListener());
		graph.addKeyListener(controller.getGraphKeyListener());
		graphScroll.getViewport().removeAll();	
		graphScroll.getViewport().add(graph);
	}
	
	public JGraph getGraph()	{
		return graph;
	}
	
	public JTextPane getEditText()	{
		return editText;
	}
	
	public GraphPanelController getController()	{
		return controller;
	}
	
	public JPopupMenu getPopup()	{
		return popup;
	}
	
	public JMenuItem getRemoveItem()	{
		return removeItem;
	}
}
