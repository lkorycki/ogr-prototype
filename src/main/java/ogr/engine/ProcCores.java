package ogr.engine;

import java.io.File;

import ogr.controller.ProcessingWindowController;
import ogr.engine.ImageProcessor.FilterColor;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

class ProcCores {
	
	/**
 		Get the center of a contour (mass).
 		@param contour : contour to analyze
	 */
	public static Point centroid(MatOfPoint contour)
	{
		Moments p = Imgproc.moments(contour, false);
        int x = (int) (p.get_m10() / p.get_m00());
        int y = (int) (p.get_m01() / p.get_m00());
        
        return (new Point(x,y));
	}
	
	/**
		Get distance between two points (2D).
		@param p1: first point
		@param p2: second point
	 */
	public static double dist(Point pt1, Point pt2)
	{
		return Math.sqrt((pt1.x-pt2.x)*(pt1.x-pt2.x) + (pt1.y-pt2.y)*(pt1.y-pt2.y));
	}
	
	/**
		Filter a text from the node using selected color: GREEN/RED.
		@param in: image to be filtered
		@param color: text color
	 */
	public static Mat textFilter(Mat in, FilterColor color)	{
		
		Mat out = new Mat(in.rows(), in.cols(), in.type());
		
		double add[] = {0,0,0}; // black
		double rmv[] = {255,255,255}; // white
				     		        
		for (int i = 0; i < in.rows(); i++)
		{
		    for (int j = 0; j < in.cols(); j++)
		    {		    	
		        double[] pixel = in.get(i, j);
		        
		        // leave color text
		        if(color == FilterColor.GREEN)	{
		        	if((pixel[1] > 1.05*pixel[0]) && (pixel[1] > 1.05*pixel[2])) out.put(i, j, add); 
		        	else out.put(i, j, rmv);	
		        }
		        else if(color == FilterColor.RED){
		        	if((pixel[2] > 100) && (pixel[2] > 1.2*pixel[0]) && (pixel[2] > 1.2*pixel[1])) out.put(i, j, add); 
		        	else out.put(i, j, rmv);	
		        }
		    }
		}
		
		Imgproc.medianBlur(out, out, 5);
		
		return out;
	}
	
	/**
		Logger. Uses ProcessingWindowController.updateStatus()
		@param status : text for the logger
		@param image : actual processing step
		@param step : value for the progress bar
	 */
	public static void log(Mat image, String status, int step)	{
		if(image != null) {
			Highgui.imwrite("tmp\\temp_proc.jpg", image); // temp
			ProcessingWindowController.updateStatus(status, new File("tmp\\temp_proc.jpg"), step);
		}
		else ProcessingWindowController.updateStatus(status, null, step);
	}
}
