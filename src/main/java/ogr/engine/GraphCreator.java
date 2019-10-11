package ogr.engine;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

public class GraphCreator {
	
	private ArrayList<GraphCreatorNode> graphCreatorNodes;
	private int n = 0;
	Tesseract OCR = new Tesseract(); // OCR module
	
	int jGraphX = 860; float scaleX = 1; // for cells positions scaling 
	int jGraphY = 640; float scaleY = 1;
	Size imgSize;
	
	/**
		Default constructor.
	 */
	public GraphCreator() {
		
		imgSize = null;
		scaleX = -1;
		scaleY = -1;
		n = 0;	
		graphCreatorNodes = null;	
		OCR.setLanguage("ogr");
	}
	
	/**
		Constructor with the given image.
		@param _imgSize : size of the original photo - for scaling
	 */
	public GraphCreator(Size _imgSize) {
		
		imgSize = _imgSize;
		scaleX = ((float) imgSize.width/jGraphX);
		scaleY = ((float) imgSize.height/jGraphY);
		n = 0;
		
		graphCreatorNodes = null;
		
		OCR.setLanguage("ogr");
	}
		
	/**
		Process of JGraph generating.
		@param rawNodes : raw nodes from the ImageProcessor
		@param rawLines : raw edges from the ImageProcessor
	 */
	public JGraph createJGraph(ArrayList<RawNode> rawNodes, ArrayList<RawLine> rawLines) {
		
		createGraphCreatorNodes(rawNodes);
		connectGraphCreatorNodes(rawLines);
		return generateGraph();
	}
		
	/**
		Displaying a logical structure of the graph.
	 */
	public void display() {	
		for(GraphCreatorNode graphNode : graphCreatorNodes) {
			graphNode.display();
		}
	}
	
	/**
		Create JGraph from GraphCreatorNodes.
	 */
	private JGraph generateGraph() {
		
		// Graph and model
		GraphModel graphModel = new DefaultGraphModel();
		JGraph jGraph = new JGraph(graphModel);
		jGraph.setCloneable(true);
		jGraph.setInvokesStopCellEditing(true);
		jGraph.setJumpToDefaultPort(true);
		
		// Adding nodes and edges to the jGraph
		ArrayList<DefaultGraphCell> nodes = new ArrayList<DefaultGraphCell>(n);
		ArrayList<DefaultGraphCell> edges = new ArrayList<DefaultGraphCell>();
		
		// Nodes
		for(GraphCreatorNode graphCreatorNode : graphCreatorNodes) {
			
			Point p = graphCreatorNode.getRawNode().getCenterPoint();
			String text = graphCreatorNode.getText();
			String cost = graphCreatorNode.getCost();
			String id = Integer.toString(graphCreatorNode.getIdx());
			String nodeLabel = "<html>" + text + " : [" + cost 
					+ "]</html> " + (int)(p.x/scaleX) + " " + (int)(p.y/scaleY) + " " + id;		
			nodes.add(graphCreatorNode.getIdx(), createNode(nodeLabel, p.x/scaleX, p.y/scaleY, 150, 100)); // id is 1:1 (!)	
		}
		
		// Edges (adding only from the source)
		for(GraphCreatorNode graphCreatorNode : graphCreatorNodes) {
			
				ArrayList<GraphCreatorNode> children = graphCreatorNode.getChildren();
				for(GraphCreatorNode child : children) {
					
					DefaultEdge edge = new DefaultEdge();
					edge.setSource(nodes.get(graphCreatorNode.getIdx()).getChildAt(0)); // see above 
					edge.setTarget(nodes.get(child.getIdx()).getChildAt(0));
			
					GraphConstants.setLineEnd(edge.getAttributes(), GraphConstants.ARROW_CLASSIC);
					GraphConstants.setEndFill(edge.getAttributes(), true);
					GraphConstants.setEditable(edge.getAttributes(), false);
					edges.add(edge);
				}
		}
		
		ArrayList<DefaultGraphCell> cells = new ArrayList<DefaultGraphCell>();
		cells.addAll(nodes);
		cells.addAll(edges);
				
		jGraph.getGraphLayoutCache().insert(cells.toArray());
		
		return jGraph;
	}	
	
	/**
		Create graph nodes with text, cost and relations with other nodes.
		Running OCR module.
		@param rawNodes : raw nodes from the ImageProcessor
	 */
	private void createGraphCreatorNodes(ArrayList<RawNode> rawNodes) {
		
		graphCreatorNodes = new ArrayList<GraphCreatorNode>();
		
		for(RawNode rawNode : rawNodes) {
			
			// OCR
			String text = "", cost = "";
			
			// Text
			if(rawNode.getTextImage() != null) {
				Highgui.imwrite("tmp\\temp_text.bmp", rawNode.getTextImage());
				File textFile = new File("tmp\\temp_text.bmp");
				try {
		            text = OCR.doOCR(textFile);
		            text = text.replaceAll("\n", " ").replaceAll("<", "(").replaceAll(">", ")"); 
		        } catch (TesseractException e) {
		            System.err.println(e.getMessage());
		        }
			}
			
			// Node
			if(rawNode.getCostImage() != null) {
				Highgui.imwrite("tmp\\temp_cost.bmp", rawNode.getCostImage());
				File costFile = new File("tmp\\temp_cost.bmp");
				try {
		            cost = OCR.doOCR(costFile);
		            cost = cost.replaceAll(" ", "").replaceAll("\n", "").replaceAll("<", "(").replaceAll(">", ")"); // filtering
		        } catch (TesseractException e) {
		            System.err.println(e.getMessage());
		        }
			}	        
	        
			GraphCreatorNode graphNode = new GraphCreatorNode(text, cost, rawNode, n++);
			graphCreatorNodes.add(graphNode);
		}
	}
	
	/**
		Connect graph nodes with edges.
		@param rawLines : raw edges from the ImageProcessor
	 */
	private void connectGraphCreatorNodes(ArrayList<RawLine> rawLines) {
		
		for(RawLine rawLine : rawLines) {
			
			Point srcPoint = rawLine.getSrcPoint();
			Point dstPoint = rawLine.getDstPoint();
			
			double minSrcDist = Double.MAX_VALUE, minDstDist = Double.MAX_VALUE;
			GraphCreatorNode minSrc = null, minDst = null;
			
			for(GraphCreatorNode graphNode : graphCreatorNodes) {
				
				double srcDist = ProcCores.dist(srcPoint, graphNode.getRawNode().getCenterPoint());
				double dstDist = ProcCores.dist(dstPoint, graphNode.getRawNode().getCenterPoint());
				
				if(srcDist < minSrcDist) {
					minSrcDist = srcDist;
					minSrc = graphNode;
				}
				
				if(dstDist < minDstDist && dstDist < srcDist) {
					minDstDist = dstDist;
					minDst = graphNode;
				}
			}
			
			if(minSrc != null) minSrc.addChild(minDst);
			if(minDst != null) minDst.addParent(minSrc);		
		}
	}
	
	/**
		Create a JGraph node.
		@param name : label for the node
		@param x : x position of the node
		@param y : y position of the node
		@param w : width of the node
		@param h : height of the node
	 */
	public static DefaultGraphCell createNode(String name, double x, double y, double w, double h) {

		DefaultGraphCell cell = new DefaultGraphCell(name);	

		GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(x, y, w, h));
		GraphConstants.setInset(cell.getAttributes(), 5);
		
		// Set colors		
		Color color = new Color(120,230,120);
		GraphConstants.setGradientColor(cell.getAttributes(), color);
		GraphConstants.setOpaque(cell.getAttributes(), true);			
		GraphConstants.setBorderColor(cell.getAttributes(), Color.GRAY);	
		
		// Not editable
		GraphConstants.setEditable(cell.getAttributes(), false);
		
		// Add a Floating Port
		cell.addPort();

		return cell;
	}	
	
	/**
		Create JGraph from a *.json file.
		@param graphFile : graph file to load
	 */
	public JGraph createJGraphFromJSON(File graphFile)	{
		
		// Graph and model
		GraphModel graphModel = new DefaultGraphModel();
		JGraph jGraph = new JGraph(graphModel);
		jGraph.setCloneable(true);
		jGraph.setInvokesStopCellEditing(true);
		jGraph.setJumpToDefaultPort(true);
					
		// Load json file
		String json = null;
		Scanner scan;
		try {
			scan = new Scanner(graphFile);
			json = new String();
		    while (scan.hasNext())
		        json += scan.nextLine();
		    scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Load nodes and edges from the json file   
		JSONObject obj = new JSONObject(json).getJSONObject("graph");
		
		// Adding nodes and edges to the jGraph
		HashMap<String, DefaultGraphCell> nodes = new HashMap<String, DefaultGraphCell>(n);
		ArrayList<DefaultGraphCell> edges = new ArrayList<DefaultGraphCell>();
		
		JSONArray arr = obj.getJSONArray("nodes");
		for (int i = 0; i < arr.length(); i++)	{
			
			JSONObject jNode = (JSONObject) arr.get(i);
			String posx = jNode.getString("x");
			String posy = jNode.getString("y");
			String id = jNode.getString("id");
			
			String nodeLabel = "<html>" + jNode.getString("text")  + "</html> " + posx + " " + posy 
					+ " " + id;	
			nodes.put(id, createNode(nodeLabel, Integer.parseInt(posx), Integer.parseInt(posy), 150, 100));
		}
		
		arr = obj.getJSONArray("edges");
		for (int i = 0; i < arr.length(); i++)	{
			JSONObject jEdge = (JSONObject) arr.get(i);
			
			String srcId = jEdge.getString("src");
			String dstId = jEdge.getString("dst");
			
			DefaultEdge edge = new DefaultEdge();
			edge.setSource(nodes.get(srcId).getChildAt(0)); // see above 
			edge.setTarget(nodes.get(dstId).getChildAt(0));
	
			GraphConstants.setLineEnd(edge.getAttributes(), GraphConstants.ARROW_CLASSIC);
			GraphConstants.setEndFill(edge.getAttributes(), true);
			GraphConstants.setEditable(edge.getAttributes(), false);
			edges.add(edge);	
		}

		// Insert cells
		ArrayList<DefaultGraphCell> cells = new ArrayList<DefaultGraphCell>();
		cells.addAll(nodes.values());
		cells.addAll(edges);		
		jGraph.getGraphLayoutCache().insert(cells.toArray());
		
		return jGraph;		
	}
	
	public static int getJGraphNodesNum(JGraph jGraph)	{
		
		Object[] cells = jGraph.getGraphLayoutCache().getCells(jGraph.getGraphLayoutCache().getAllViews());
		ArrayList<DefaultGraphCell> nodes = new ArrayList<DefaultGraphCell>();
		for(Object cell : cells)	{
			DefaultGraphCell defCell = (DefaultGraphCell) cell;
			if(!jGraph.getModel().isEdge(defCell) && !defCell.toString().isEmpty()) nodes.add(defCell); 		
		}
		
		return nodes.size();
	}
	
	// Testing
	public static void main(String[] args) {
		GraphCreator gc = new GraphCreator();
		gc.createJGraphFromJSON(new File("D:\\Photos\\test4.json"));
	}
}
