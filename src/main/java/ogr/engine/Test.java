package ogr.engine;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import ogr.engine.ImageProcessor.FilterColor;

import org.jgraph.JGraph;


public class Test {

	public static void main(String[] args) 
	{	
		System.out.println("# OGR : Image Processing Test.\n");
		ImageProcessor IP = new ImageProcessor("src\\test\\resources\\test_images\\test4.jpg", FilterColor.GREEN, null);
		
		System.out.println("	Processing...");
		IP.startProcessing();
		System.out.println("	Finished!");
		
		System.out.println("	Creating the graph model...");
		GraphCreator graphCreator = new GraphCreator(IP.getImageSize());
		JGraph graph = graphCreator.createJGraph(IP.getRawNodes(), IP.getRawLines());
		System.out.println("	Finished!");
		
		// Show in Frame
		
		JFrame frame = new JFrame();
		frame.setPreferredSize(new Dimension(860, 640));
		frame.getContentPane().add(new JScrollPane(graph));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		
		System.out.println("\n# OGR : Created model.\n");
		graphCreator.display();
	}
}
