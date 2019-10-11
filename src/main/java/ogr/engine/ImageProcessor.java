package ogr.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ImageProcessor {
	
	boolean disp = true; 
	static public String test_output = "src\\test\\resources\\test_output\\";
	
	// Images
	private int height, width;
	private static Mat srcImage;
	private Mat binImage;
	private Mat binAmplifiedImage;
	private Mat segImage;
	
	// Extracted shapes
	private ArrayList<RawNode> rawNodes;
	private ArrayList<RawLine> rawLines;
	
	// Rectangles, circles and lines detection
	static int d; // cropping margin (depend on edges width)
	static int amp; // amplification parameter
	
	static int MAX_AREA;
	static int MIN_AREA;
	static int RECT_MAX_AREA;
	static int RECT_MIN_AREA;
	static int LINE_MAX_AREA;
	static int LINE_MIN_AREA;

	// Inner/outer 
	static int CP_E; // max distance between related figures
	
	// Color filter
	public enum FilterColor { GREEN, RED };
	static public FilterColor color; // for text
	
	/**
	 	Initialize images.
	 	@param srcPath : path to the source image
	 	@param _color : text filter color
	*/
	public ImageProcessor(String srcPath, FilterColor _color, Rect selection) {	
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		color = _color;
		
		// Init work images 
		srcImage = Highgui.imread(srcPath, Imgproc.COLOR_BGR2GRAY);
		if (selection != null) {
			srcImage = new Mat(srcImage, selection);
		}
		height = srcImage.cols();
		width = srcImage.rows();
	    binImage = new Mat(srcImage.size(), Core.DEPTH_MASK_ALL);
	    binAmplifiedImage = new Mat(srcImage.size(), Core.DEPTH_MASK_ALL);
	    segImage = srcImage.clone(); // for visualization only
	}
		
	public void startProcessing() {
		
		prepareImages(srcImage);
		setParameters();
		
		// Detecting shapes 
		binAmplifiedImage = amplifyShapes(binImage, amp);	
		HashMap<String, ArrayList<MatOfPoint>> shapesGroups = new HashMap<String, ArrayList<MatOfPoint>>();		
	    ProcCores.log(binAmplifiedImage, "\tContours amplification.", 15);
		if(disp) Highgui.imwrite(test_output + "3_test_AMP.bmp", binAmplifiedImage);
		
		// Raw nodes
		ArrayList<MatOfPoint> nodesContours = recognizeNodes(binAmplifiedImage);
		shapesGroups.put("Nodes", nodesContours);
		
		// Raw lines
		ArrayList<MatOfPoint> linesContours = detectLines(binAmplifiedImage);
		shapesGroups.put("Lines", linesContours);
		
		// Creating raws objects 
		createRaws(shapesGroups);
	}
	
	public ArrayList<RawNode> getRawNodes()	{
		return rawNodes;
	}
	
	public ArrayList<RawLine> getRawLines()	{
		return rawLines;
	}
	
	public Size getImageSize() {
		return srcImage.size();
	}
	
	public static void setFilterColor(FilterColor _color){
		color = _color;
	}
	
	public static FilterColor getFilterColor() {
		return color;
	}
	
	/**
 		Set static parameters for image processing.
	*/
	private void setParameters()	{
		
		MAX_AREA = (int) (0.8*height*0.8*width);
		MIN_AREA = (int) (0.04*height*0.04*width);
		RECT_MAX_AREA = (int) (0.5*height*0.5*width);
		RECT_MIN_AREA = (int) (0.05*height*0.05*width);
		LINE_MAX_AREA = (int) (0.02*height*0.5*width);
		LINE_MIN_AREA = (int) (0.01*height*0.05*width);
		
		d = (int) (0.015*width);
		CP_E = (int) (2.5*d);
		amp = (int) (0.25*d);
	}

	/**
	 	Do basic transformations.
	 	@param srcImage : the source image
	*/
	private void prepareImages(Mat srcImage) {	
		
		Mat grayImage = new Mat(srcImage.size(), Core.DEPTH_MASK_8U);
	    Mat blurImage = new Mat(srcImage.size(), Core.DEPTH_MASK_8U);
	    Imgproc.cvtColor(srcImage, grayImage, Imgproc.COLOR_BGR2GRAY); // to the gray-scale
	    Imgproc.GaussianBlur(grayImage, blurImage, new Size(5,5), 0); // filter with blurring    
	    Imgproc.adaptiveThreshold(blurImage, binImage, 255, 
	    		Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,21,3); // to the binary image
	    Imgproc.medianBlur(binImage, binImage, 5); // for salt and pepper noise
	    	    
	    if(disp) {
		    Highgui.imwrite(test_output + "1_test_BLUR.bmp",blurImage);
	    	Highgui.imwrite(test_output + "2_test_BIN.bmp",binImage);
	    }
	    ProcCores.log(binImage, "\tBinarization.", 10);
	}
	
	/**
	 	Detect basic shapes and amplify their contours. 
	 	@param binImage : the bin image to be amplified
	 	@param binAmplifiedImage : the amplified bin image
	 	@param amp : amplification parameter
	*/
	public static Mat amplifyShapes(Mat binImage, int amp) {	
		
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();   
			
		// Find the contours in the raw image		
		Imgproc.findContours(binImage.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE); 
		Mat binAmplifiedImage = binImage.clone();  
		
		// Amplify them
	    for(int i = 0; i < contours.size(); i++) {
	    	double contourArea = Imgproc.contourArea(contours.get(i));
	    	if (contourArea < MIN_AREA || contourArea > MAX_AREA) continue; // noise
			Imgproc.drawContours(binAmplifiedImage, contours, i, new Scalar(0,0,0), amp); // amplify the contours
		}
	    
	    return binAmplifiedImage;
	}
	
	/**
	 	Recognize rectangles from the array of contours and extract them.
	 	(!) Warning: this method crops rectangles from the amplified binary image.
	 	@param binAmplifiedImage : the amplified bin image with rectangles to be cropped
	*/
	private ArrayList<MatOfPoint> recognizeNodes(Mat binAmplifiedImage)	{	
		
	    // Find the contours in the image with amplified edges
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>(); // clear the array
	    Imgproc.findContours(binAmplifiedImage.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);  
	    
		// Contours of classified shapes 	
		ArrayList<MatOfPoint> rects = new ArrayList<MatOfPoint>();		
	    MatOfPoint2f approx = new MatOfPoint2f(); // approximated curve
	        		
	    for (int i = 0; i < contours.size(); i++) {
	    	
	    	// Approximate contour with accuracy proportional to the contour parameter 
			Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx, 
					Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true)*0.02, true);
			
	        // Skip small objects  
			double contourArea = Imgproc.contourArea(contours.get(i));
			if (contourArea < RECT_MIN_AREA || contourArea > RECT_MAX_AREA) continue; // noise
						
			/* Determine the shape of the contour */	
			int approxLen = approx.toArray().length;
			int vtc = approxLen;		
			if (vtc == 4) 	{
				Imgproc.drawContours(segImage, contours, i, new Scalar(0,0,255), 5);				
				rects.add(contours.get(i));
		
				// And crop them from the image
				Rect rect = Imgproc.boundingRect(contours.get(i));
				Core.rectangle(binAmplifiedImage, new Point(rect.x - d, rect.y - d), 
						new Point(rect.x + rect.width + d, rect.y + rect.height + d), new Scalar(255,255,255), -1);
			}			
		}   
	    
	    ProcCores.log(srcImage, "", 25);
	    ProcCores.log(segImage, "\tRectangles recognized.", 25);
	    
	    if(disp) {
		    Highgui.imwrite(test_output + "4a_test_SEG1.bmp", segImage);
		    Highgui.imwrite(test_output + "4b_test_CROP.bmp", binAmplifiedImage);
	    }
	    
	    return rects;
	}
	
	/**
		Find lines.
		@param binAmplifiedImage : amplified bin image without cropped rectangles - only lines and noise left
	*/
	private ArrayList<MatOfPoint> detectLines(Mat binAmplifiedImage) {	
		
		// Amplify arrow lines 
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(binAmplifiedImage.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE); 
			
	    for(int i = 0; i < contours.size(); i++) {
	    	double contourArea = Imgproc.contourArea(contours.get(i));
	    	if (contourArea < 0.05*LINE_MIN_AREA || contourArea > LINE_MAX_AREA) continue; // noise
			Imgproc.drawContours(binAmplifiedImage, contours, i, new Scalar(0,0,0), 25); 
		}
	    
	    // Detect their contours 
	    contours = new ArrayList<MatOfPoint>();
	    Imgproc.findContours(binAmplifiedImage.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE); 
	    
	    // Classified lines
	    ArrayList<MatOfPoint> lines = new ArrayList<MatOfPoint>();    
	    for(int i = 0; i < contours.size(); i++)	
	    {
	    	double contourArea = Imgproc.contourArea(contours.get(i));
	    	if (contourArea < 3*LINE_MIN_AREA || contourArea > LINE_MAX_AREA) continue; // noise
	    	
	    	lines.add(contours.get(i));
	    	        
	        // visualize the line's contour
			Imgproc.drawContours(segImage, contours, i, new Scalar(255,0,0), 2); 
		}
    	
	    ProcCores.log(segImage, "\tEdges recognized.", 35);
	    if(disp) {
		    Highgui.imwrite(test_output + "5a_test_AMP_LINES.bmp", binAmplifiedImage);
		    Highgui.imwrite(test_output + "5b_test_SEG2.bmp", segImage);
	    }
	    			
		return (ArrayList<MatOfPoint>) lines;
	}
	
	/**
	 	Create raw objects from which graph nodes will be created.
	 	@param shapesGroups : HashMap with rectangles and lines contours
	*/
	private void createRaws(HashMap<String, ArrayList<MatOfPoint>> shapesGroups)	
	{
		// Raw nodes 
		ArrayList<MatOfPoint> nodes = shapesGroups.get("Nodes"); // contours
		rawNodes = createRawNodes(nodes);
		ProcCores.log(null, "\tRaw rectangles processed.", 50);
		
		// Raw lines 
		ArrayList<MatOfPoint> lines = shapesGroups.get("Lines"); // contours
		rawLines = createRawLines(lines);			
		ProcCores.log(null, "\tRaw lines processed.", 60);
	}
	
	/**
 		Create a collection of raw nodes.
 		@param contours : list of rectangles contours
	 */
	private ArrayList<RawNode> createRawNodes(ArrayList<MatOfPoint> contours) {
		
		ArrayList<RawNode> rawNodes = new ArrayList<RawNode>();
		boolean inner = true;
		
		for (int i = 0; i < contours.size(); i++) {		
			
			MatOfPoint contour = contours.get(i);
			Point contourCP = ProcCores.centroid(contour); // central point of the node
			double contourArea = Imgproc.contourArea(contour); // area of the node
			inner = true;
			
			// Check if it is not an outer rectangle         
			for(int j = 0; j < contours.size(); j++) {
				
				if(i == j) continue;
				MatOfPoint otherContour = contours.get(j);
				Point otherContourCP = ProcCores.centroid(otherContour); // as above
				double otherContourArea = Imgproc.contourArea(otherContour);
				
				if(ProcCores.dist(contourCP, otherContourCP) < CP_E) { // related rectangles        	
	            	if(contourArea > otherContourArea) { // get the outer rectangle
	            		inner = false;
	            		break; // outer
	            	}
	            }
			}
			
			if(inner) {		
				
				Rect rectBound = Imgproc.boundingRect(contour); 
			    Mat rectImage = srcImage.submat(rectBound); // get the part of the image
			    RawNode rawNode = new RawNode(rectImage, contourCP); // create a raw node
			    rawNodes.add(rawNode);
			   			    
			    if(disp) Highgui.imwrite(test_output + "nodes\\rect" + i + ".bmp", rectImage);
			}
		}
		
		return rawNodes;
	}
	
	/**
		Create a collection of raw lines.
		@param contours : list of lines contours
	 */
	private ArrayList<RawLine> createRawLines(ArrayList<MatOfPoint> contours) {
		
		ArrayList<RawLine> rawLines = new ArrayList<RawLine>();
		Mat linesCentroids = srcImage.clone();
		
		for (MatOfPoint lineContour : contours) {
			
			double lineContourArea = Imgproc.contourArea(lineContour);
			if (lineContourArea < LINE_MIN_AREA || lineContourArea > LINE_MAX_AREA) continue; // noise
			
			// Find centroid (to determine a direction)
	        Point centerPoint = ProcCores.centroid(lineContour);
	        Core.circle(linesCentroids, centerPoint, 15, new Scalar(255,0,0), -1);
	        
	        // Find start and end point (approximation)
	        RotatedRect boundRect = Imgproc.minAreaRect(new MatOfPoint2f(lineContour.toArray())); // min rotated rectangle
	        Point rectPoints[] = new Point[4];
	        boundRect.points(rectPoints);
	        
	        for(int j = 0; j < 4; j++) {
	            Core.line(linesCentroids, rectPoints[j], rectPoints[(j+1)%4], new Scalar(255,0,0));
	        }
	        
	        Point linePoint1, linePoint2;
	        if(ProcCores.dist(rectPoints[0], rectPoints[1]) <= ProcCores.dist(rectPoints[0], rectPoints[3])) {
	        	linePoint1 = new Point((rectPoints[0].x + rectPoints[1].x)/2, (rectPoints[0].y + rectPoints[1].y)/2);
	        	linePoint2 = new Point((rectPoints[2].x + rectPoints[3].x)/2, (rectPoints[2].y + rectPoints[3].y)/2);
	        }
	        else {
	        	linePoint1 = new Point((rectPoints[1].x + rectPoints[2].x)/2, (rectPoints[1].y + rectPoints[2].y)/2);
	        	linePoint2 = new Point((rectPoints[0].x + rectPoints[3].x)/2, (rectPoints[0].y + rectPoints[3].y)/2);
	        }
	        
	        // Determine direction of the arrow and set parameters
	        Point srcPoint, dstPoint;
	        if(ProcCores.dist(linePoint1, centerPoint) < ProcCores.dist(linePoint2, centerPoint)) {
	        	dstPoint = linePoint1;
	        	srcPoint = linePoint2;
	        }
	        else {
	        	dstPoint = linePoint2;
	        	srcPoint = linePoint1;
	        }
	        
	        RawLine rawLine = new RawLine(lineContour, srcPoint, dstPoint); // create a raw line
	        rawLines.add(rawLine);
	        
	        Core.circle(linesCentroids, srcPoint, 10, new Scalar(0,0,255), -1);
	        Core.circle(linesCentroids, dstPoint, 10, new Scalar(0,255,0), -1);		
		}
		
		if(disp) Highgui.imwrite(test_output + "5c_test_LINES.bmp", linesCentroids);
		ProcCores.log(srcImage, "", 60);
	    ProcCores.log(linesCentroids, "\tDirections of the edges determined.", 65);
	    
	    return rawLines;
	}	
}
