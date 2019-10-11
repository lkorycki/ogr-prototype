package ogr.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ogr.engine.GraphCreator;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;

import com.google.gson.stream.JsonWriter;

/* It generates files from a jGraph */

public class GraphFileConverter {
	
	public GraphFileConverter()	{		
	}
	
	/**
		Generate a .json file.
		@param jGraph : jGraph to export
		@param name : name (must be the same as the image's name)
	 */
	public File generateGraphJSON(JGraph jGraph, String name)	{
		
		// Get all cells - nodes and edges
		Object[] cells = jGraph.getGraphLayoutCache().getCells(jGraph.getGraphLayoutCache().getAllViews());
		ArrayList<DefaultGraphCell> nodes = new ArrayList<DefaultGraphCell>();
		ArrayList<DefaultEdge> edges = new ArrayList<DefaultEdge>();
		
		// Group them
		for(Object cell : cells)	{
			DefaultGraphCell defCell = (DefaultGraphCell) cell;
			if(!jGraph.getModel().isEdge(defCell) && !defCell.toString().isEmpty()) nodes.add(defCell); 
			else if(jGraph.getModel().isEdge(defCell)) edges.add((DefaultEdge)defCell);			
		}
		
		JsonWriter jsonWriter = null;
		try {
			jsonWriter = new JsonWriter(new FileWriter("D:\\Photos\\" + name + ".json"));
			
			jsonWriter.beginObject();
		    jsonWriter.name("graph");	    
		    
			// Nodes
		    jsonWriter.beginObject();
		    jsonWriter.name("nodes");
		    jsonWriter.beginArray();
		    
			for(DefaultGraphCell node : nodes)	{
				String[] params = TextParser.getParams(node.toString()).split(" ");
				
				jsonWriter.beginObject();
				
				jsonWriter.name("id");
				jsonWriter.value(TextParser.getNodeId(node.toString()));
				jsonWriter.name("x");
				jsonWriter.value(params[0]);
				jsonWriter.name("y");
				jsonWriter.value(params[1]);
				jsonWriter.name("text");
				jsonWriter.value(TextParser.parseText(node.toString(), true));
				
				jsonWriter.endObject();
			}
			jsonWriter.endArray();	
			
			// Edges	
			jsonWriter.name("edges");
			jsonWriter.beginArray();
			
			for(DefaultEdge edge : edges)	{
							
				DefaultGraphCell srcCell = (DefaultGraphCell) jGraph.getModel().getParent(jGraph.getModel().getSource(edge));
				DefaultGraphCell dstCell = (DefaultGraphCell) jGraph.getModel().getParent(jGraph.getModel().getTarget(edge));
				String srcId = TextParser.getNodeId(srcCell.toString());
				String dstId = TextParser.getNodeId(dstCell.toString());
				
				jsonWriter.beginObject();
				
				jsonWriter.name("src");
				jsonWriter.value(srcId);
				jsonWriter.name("dst");
				jsonWriter.value(dstId);
				
				jsonWriter.endObject();

			}
			jsonWriter.endArray();
			jsonWriter.endObject();
			
			jsonWriter.endObject(); // graph
			jsonWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new File("D:\\Photos\\" + name + ".json");
	}
		
	/**
		Load a .gph file and create JGraph.
		@param graphFile : graph file to load
	 */
	public JGraph loadGphFile(File graphFile)	{
		GraphCreator graphCreator = new GraphCreator(); // use GraphCreator
		JGraph jGraph = graphCreator.createJGraphFromJSON(graphFile);	
		return jGraph;
	}
	
	// Testing
	public static void main(String[] args) {
		GraphFileConverter gc = new GraphFileConverter();
		gc.loadGphFile(new File("D:\\Photos\\test4.gph"));
	}
}
