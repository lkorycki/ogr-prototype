package ogr.controller;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

import ogr.MApplication;
import ogr.engine.GraphCreator;
import ogr.util.TextParser;
import ogr.view.GraphPanelView;

public class GraphPanelController extends Controller {
	
	private GraphPanelView graphPanel;
	private int n;
	
	public GraphPanelController(MApplication _mapp) {
		super(_mapp);
		n = 0;
	}
	
	public void setGraph(JGraph jGraph)	{		
		if(jGraph == null) return;
		graphPanel.setGraph(jGraph);
		n = this.getMaxJGraphId(jGraph) + 1; // the next index
	}
	
	public void setPanel(GraphPanelView _graphPanel)	{
		graphPanel = _graphPanel;
	}
	
	/**
		MouseListener for the panel with JGraph:
		- mousePressed for popup menu on the right click
		- mousePressed for selecting a node and setting its label in the edit area
		- mouseReleased for canceling a selection
		- mouseReleased after moving the selected cell (a node or an edge)
	 */
	public MouseListener getGraphMouseListener() {
		return new MouseAdapter() {
					
			public void mousePressed(MouseEvent e) 
	        {
				DefaultGraphCell cell = (DefaultGraphCell) graphPanel.getGraph().getSelectionCell();
				
				// Popup menu
				if(SwingUtilities.isRightMouseButton(e))	{	
					if(cell == null) graphPanel.getRemoveItem().setEnabled(false);
					else graphPanel.getRemoveItem().setEnabled(true);
					graphPanel.getPopup().show(e.getComponent(), e.getX(), e.getY());
				}
				
				// Selecting a node
				else	{				
					if(cell == null || graphPanel.getGraph().getModel().isEdge(cell)) return;			
					String label = cell.toString();
					if(!label.isEmpty()) {
						TextParser.setLastNodeParams(label); // remember params for wrapping 
						graphPanel.getEditText().setText(TextParser.parseText(cell.toString(), true));		
					}
				}
	        }
			
			public void mouseReleased(MouseEvent e)	{						
				if(SwingUtilities.isLeftMouseButton(e))	{
					DefaultGraphCell cell = (DefaultGraphCell) graphPanel.getGraph().getSelectionCell();
					
					// Cancel a selection
					if(cell == null || graphPanel.getGraph().getModel().isEdge(cell)) {
						graphPanel.getEditText().setText("");
						return;
					}
					
					// Remember the new params of the cell after moving it
					TextParser.setLastNodeParams(cell.toString());
					int f = cell.toString().indexOf("</html>") + 7; // params start - overwrite them
					String newLabel = cell.toString().substring(0, f) + " " + (int)e.getX() + " "
									+ (int)e.getY() + " " + TextParser.lastNode.split(" ")[2];
					cell.setUserObject(newLabel); // update params
				}
			}
		};
	}
	
	/**
		KeyListener for the panel with JGraph:
		- keyPressed for deleting the selected node
	 */
	public KeyListener getGraphKeyListener(){		
		return new KeyAdapter() {
			 
			// Delete the selected node
			 public void keyPressed(KeyEvent e) {
				 JGraph jGraph = graphPanel.getGraph();
				 DefaultGraphCell cell = (DefaultGraphCell) jGraph.getSelectionCell();
				 if(cell == null) return;

				 char c = e.getKeyChar();
				 	
				 switch(c)	{
				 	case 127:	// DELETE
				 		jGraph.getModel().remove(new Object[] {cell});	
				 		break;
				 	default: break;
				 }
			 }
		};
	}
	
	/**
		ActionListener for removing a cell (node or edge) from the popup menu.
	 */
	public ActionListener getRemoveListener()	{
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JGraph jGraph = graphPanel.getGraph();
				DefaultGraphCell cell = (DefaultGraphCell) jGraph.getSelectionCell();
				if(cell == null) return;
				else jGraph.getModel().remove(new Object[] {cell});	
			}
		};
	}
	
	/**
		ActionListener for adding a node from the popup menu
	 */
	public ActionListener addNodeListener()	{
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Relative position
				Point p1 = MouseInfo.getPointerInfo().getLocation();
				Point p2 = getMApp().getMainPanel().getGraphPanel().getLocationOnScreen();
				int x = p1.x-p2.x, y = p1.y-p2.y;	
				
				// Attributes
				DefaultGraphCell node = GraphCreator.createNode(
						"<html>Insert text</html> " + x + " " + y + " " + (n++),
						x, y, 150, 100);
				graphPanel.getGraph().getGraphLayoutCache().insert(node);
			}
		};
	}
	
	/**
		ActionListener for adding an edge from the popup menu
	 */
	public ActionListener addEdgeListener()	{
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
						
				// Relative position
				ArrayList<Point> points = new ArrayList<Point>();
				Point p1 = MouseInfo.getPointerInfo().getLocation();
				Point p2 = getMApp().getMainPanel().getGraphPanel().getLocationOnScreen();
				points.add(new Point(p1.x-p2.x, p1.y-p2.y));
				points.add(new Point(p1.x-p2.x + 150, p1.y-p2.y));
				
				// Attributes
				DefaultEdge edge = new DefaultEdge();
				GraphConstants.setLineEnd(edge.getAttributes(), GraphConstants.ARROW_CLASSIC);
				GraphConstants.setEndFill(edge.getAttributes(), true);
				GraphConstants.setAbsolute(edge.getAttributes(), true);
				GraphConstants.setEditable(edge.getAttributes(), false);
				GraphConstants.setPoints(edge.getAttributes(), points);
				
				graphPanel.getGraph().getGraphLayoutCache().insert(edge);
			}
		};
	}
	
	/**
		KeyListener for cell auto-updating from the edit text area
	 */
	public KeyListener getEditTextListener(){		
		return new KeyAdapter() {
			
			 // Edit text area has been changed
			 public void keyReleased(KeyEvent e) {
				 JGraph jGraph = graphPanel.getGraph();
				 DefaultGraphCell cell = (DefaultGraphCell) jGraph.getSelectionCell();
				 if(cell == null) return;
				 
				 cell.setUserObject(TextParser.parseText(graphPanel.getEditText().getText(), false)); // update a node
				 graphPanel.getGraph().refresh();	
			 }
		};
	}
	
	private int getMaxJGraphId(JGraph jGraph)	{
		Object[] cells = jGraph.getGraphLayoutCache().getCells(jGraph.getGraphLayoutCache().getAllViews());
		int max = -1;
		for(Object cell : cells)	{
			DefaultGraphCell defCell = (DefaultGraphCell) cell;
			if(!jGraph.getModel().isEdge(defCell) && !defCell.toString().isEmpty()) {
				int id = Integer.parseInt(TextParser.getNodeId(cell.toString()));
				if(id > max) max = id;
			}				
		}		
		return max;
	}
}
