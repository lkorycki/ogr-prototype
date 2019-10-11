package ogr.util;

import java.io.File;
import java.util.Vector;

import org.jgraph.JGraph;

import ogr.engine.GraphCreator;
import ogr.model.Item;

public class DirectoryReader {
	
	private String path;
	
	public DirectoryReader(String path) {
		this.path = path;
	}
	
	/**
	 * Get all data in terms of Item objects.
	 */
	public Vector<Item> getItems() {
		Vector<Item> items = new Vector<Item>(); // initialize output vector
		
		// Iterate through all files in the folder
		for (File file : listFiles()) {
			String name = file.getName().replaceFirst("[.][^.]+$", ""); // remove file extension
			Item tmp = new Item(name, null, null);
			
			if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) {
				if (items.contains(tmp)) {
					int item_idx = items.indexOf(tmp);
					if (items.get(item_idx).getImage() == null) {
						items.get(item_idx).setImage(file);
					}
				}
				else items.add(new Item(name, file, null));
			}
			
			else if (file.getName().endsWith(".json")) {
				int item_idx = -1;
				if (items.contains(tmp)) {
					item_idx = items.indexOf(tmp);
					if (items.get(item_idx).getGraph() == null) {
						items.get(item_idx).setGraphFile(file);							
					}
				}
				else items.add(new Item(name, null, file));
				
				item_idx = items.indexOf(tmp);
				GraphCreator graphCreator = new GraphCreator();
				JGraph jGraph = graphCreator.createJGraphFromJSON(file);
				items.get(item_idx).setGraph(jGraph);
			}
		}
		return items;
	}
	
	/**
	 * List all files.
	 */
	public Vector<File> listFiles() {
		File folder = new File(path);
		Vector<File> files = new Vector<File>();
		
		// Array of files and directories
		File[] filesAndDirs = folder.listFiles();

		for (int i = 0; i < filesAndDirs.length; i++) {
			if (filesAndDirs[i].isFile())
				files.add(filesAndDirs[i]);
		}
		return files;
	}

	/**
	 * Only for testing purposes.
	 * @param args
	 */
	public static void main(String[] args) {
		DirectoryReader dirReader = new DirectoryReader("D:/Photos");
		for (File file : dirReader.listFiles()) {
			System.out.println(file.getName());
		}
	}

}
