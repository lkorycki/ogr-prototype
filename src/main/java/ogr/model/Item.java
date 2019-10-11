package ogr.model;

import java.io.File;

import org.jgraph.JGraph;

public class Item {
	private String name;
	private File image;
	private File graph;
	private JGraph jGraph;
	
	public Item(String name, File image, File graph) {
		this.name = name;
		this.image = image;
		this.graph = graph;
	}
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public File getImage() { return image; }
	public void setImage(File image) { this.image = image; }

	public File getGraphFile() { return graph; }
	public void setGraphFile(File graph) { this.graph = graph; }
	public JGraph getGraph() { return this.jGraph; }
	public void setGraph(JGraph jGraph)	{ this.jGraph = jGraph; }
	
	public void rename(String newName) {
		name = newName;
		
		File newImage = new File("D:/Photos/" + newName + ".jpg");
		File newGraph = new File("D:/Photos/" + newName + ".json");
		
		if(image != null) {
			image.renameTo(newImage); 
			image = newImage;
		}
		if(graph != null) {
			graph.renameTo(newGraph);
			graph = newGraph;
		}
		
		System.out.println(image);
	}
	
	public void delete() {
		if(image != null) image.delete();
		if(graph != null) graph.delete();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Item) {
			Item that = (Item) other;
			if (this.getName().equals(that.getName())) {
				result = true; // compares by name (name should be unique)
			}
		}
		return result;
	}
}
