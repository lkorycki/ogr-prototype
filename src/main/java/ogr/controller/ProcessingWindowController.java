package ogr.controller;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.jgraph.JGraph;
import org.opencv.core.Rect;

import ogr.MApplication;
import ogr.engine.GraphCreator;
import ogr.engine.ImageProcessor;
import ogr.engine.ImageProcessor.FilterColor;
import ogr.model.Item;
import ogr.util.GraphFileConverter;
import ogr.view.ProcessingWindowView;

public class ProcessingWindowController extends Controller {
	
	private static ProcessingWindowView parentFrame;
	
	public ProcessingWindowController(MApplication mapp) {
		super(mapp);		
	}
	
	/**
		Save the reference to the parent frame - its components are needed.
		@param _parentFrame : parent frame
	 */
	@SuppressWarnings("static-access")
	public void setParentView(ProcessingWindowView parentFrame)	{
		this.parentFrame = parentFrame;
	}
		
	/**
		Update progress shown in the window.
		@param status : text for the logger
		@param image : actual processing step
		@param step : value for the progress bar
	 */
	public static void updateStatus(String status, File image, int step)	{
		StyledDocument doc = parentFrame.getTextPane().getStyledDocument();
		try {		
			if(!status.isEmpty()) doc.insertString(doc.getLength(), status + "\n", null );
			parentFrame.getProgressBar().setValue(step);
			if(image != null) {
				parentFrame.getImage().drawImage(image);
				parentFrame.getImage().resizeImageWH(600, 500);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		if(status.equals("Finished!")) parentFrame.getOkButton().setEnabled(true);
	}
	
	/**
		Start image processing and parallel reporting.
	 */
	public void startProcessing()	{
			
		new Thread()
		{
		    public void run() {
		    	parentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    	String path = getMApp().getCurrentImage().getAbsolutePath();
		    	FilterColor filterColor = getMApp().getColor();
		    	Rect selection = getMApp().getSelection();
		    	
		    	updateStatus("[1/4] Image " + path + " loaded.", new File(path), 5);
		    	ImageProcessor imageProcessor = new ImageProcessor(path, filterColor, selection);			
				imageProcessor.startProcessing();
				updateStatus("[2/4] Image processed.", null, 70);
				
				GraphCreator graphCreator = new GraphCreator(imageProcessor.getImageSize());
				updateStatus("[3/4] Graph model created.", null, 85);	
				JGraph jGraph = graphCreator.createJGraph(imageProcessor.getRawNodes(), imageProcessor.getRawLines());
				updateStatus("[4/5] JGraph created.", null, 95);	
				
				Item item = getMApp().getCurrentItem();
				getMApp().getMainPanel().getGraphPanel().getController().setGraph(jGraph);				
				GraphFileConverter fg = new GraphFileConverter();
				item.setGraphFile(fg.generateGraphJSON(jGraph, item.getName()));
				item.setGraph(jGraph);
				updateStatus("[5/5] JGraph shown.", null, 100);	
				updateStatus("Finished!", null, 100);
				
				parentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			    getMApp().getMainPanel().getController().changeTabbedPane(1);
		    }
		}.start();
	}
	
	public ActionListener getOkButtonListener() {		
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parentFrame.setVisible(false);
			}
		};
	}
}
