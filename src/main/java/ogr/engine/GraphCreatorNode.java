package ogr.engine;

import java.util.ArrayList;

public class GraphCreatorNode {
	
	private String text;
	private String cost;
	private ArrayList<GraphCreatorNode> parents, children;
	
	private int idx;
	private RawNode rawNode;
	
	public GraphCreatorNode() {
		
		text = "";
		cost = "";
		parents = null;
		children = null;
		idx = -1;
	}
	
	public GraphCreatorNode(String _text, String _cost, RawNode _rawNode, int _idx) {
		
		parents = new ArrayList<GraphCreatorNode>();
		children = new ArrayList<GraphCreatorNode>();
		text = _text;
		cost = _cost;
		rawNode = _rawNode;
		idx = _idx;
	}
	
	public void addParent(GraphCreatorNode parent) {
		parents.add(parent);
	}
	
	public void addChild(GraphCreatorNode child) {
		children.add(child);
	}
	
	public String getText() {
		return text;
	}
	
	public String getCost() {
		return cost;
	}
	
	public ArrayList<GraphCreatorNode> getParents() {
		return parents;
	}
	
	public ArrayList<GraphCreatorNode> getChildren() {
		return children;
	}
	
	public int getIdx() {
		return idx;
	}
	
	public RawNode getRawNode() {
		return rawNode;
	}
	
	public void display() {
		System.out.println("-- GraphNode [" + idx + "] --\nText: " + text + "\nCost: " + cost);
		
		System.out.print("	# Parents: "); 
		for(GraphCreatorNode parent : parents)
		{
			System.out.print(parent.getIdx() + ", ");
		}
		
		System.out.print("\n	# Children: "); 
		for(GraphCreatorNode child : children)
		{
			System.out.print(child.getIdx() + ", ");
		}
		
		System.out.println("\n---");
		
	}
}
