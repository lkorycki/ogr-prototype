package ogr.engine;

import java.util.ArrayList;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class RawNode {
	
	boolean disp = true;
	
	/* For OCR */
	private Mat textImage;
	private Mat costImage;
	
	/* For the further pre-processing */
	private Mat rectImage;
	private Mat circleImage;
	private MatOfPoint circleContour;
	private Mat binRect;
	private static int num = 0; // number of raw nodes
	
	private Point centerPoint; // in the src image (!)
	private int d = 10; // cropping margin for circles (depends on edges width)
	private int amp = 2; // for circles
	private int CIRCLE_MIN_AREA = 1000;
	private int CIRCLE_MAX_AREA = 15000;
	
	public RawNode() {
	}
	
	public RawNode(Mat _rectImage, Point _centerPoint) {
		
		num++;
		rectImage = _rectImage;
		centerPoint = _centerPoint;
		circleImage = null;
	    circleContour = null;
	    
	    int height = rectImage.rows(), width = rectImage.cols();
	    CIRCLE_MIN_AREA = (int) (0.02*height*0.02*width);
	    CIRCLE_MAX_AREA = (int) (0.5*height*0.5*width);
		
		// Extract the circle and text
		extractCircle();
		extractText();
	}
	
	public Point getCenterPoint() {
		return centerPoint;
	}
	
	public Mat getTextImage() {
		return textImage;
	}
	
	public Mat getCostImage() {
		return costImage;
	}
	
	/**
 		Extract the circle with the cost value for this node.
	 */
	private void extractCircle() {
		
		binRect = new Mat();
		
		// Prepare images
		Mat grayRect = new Mat(), blurRect = new Mat(), binAmplifiedRect = new Mat();
		Imgproc.cvtColor(rectImage, grayRect, Imgproc.COLOR_BGR2GRAY); // to the gray-scale
        Imgproc.GaussianBlur(grayRect, blurRect, new Size(19,19), 0); // filter with blurring           
        Imgproc.adaptiveThreshold(blurRect, binRect, 255, 
        		Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,31,3); // to the binary image
        Imgproc.medianBlur(binRect, binRect, 3); // for salt and pepper noise
        
        // Amplify shapes 
        binAmplifiedRect = ImageProcessor.amplifyShapes(binRect, amp);
        
        // Find the contours in the image with amplified edges 
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>(); // clear the array
	    Imgproc.findContours(binAmplifiedRect.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);  
                
        MatOfPoint2f approx = new MatOfPoint2f(); // approximated curve
		int k = -1;
		double outerArea = Double.MIN_VALUE;
		
        for (int i = 0; i < contours.size(); i++) {
        	
        	// Approximate contour with accuracy proportional to the contour parameter
			Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx, 
					Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true)*0.02, true);
			MatOfPoint contour = contours.get(i);
			
			double contourArea = Imgproc.contourArea(contour);
			if (contourArea < CIRCLE_MIN_AREA || contourArea > CIRCLE_MAX_AREA) continue; // noise
						
			// Determine the shape of the contour 	
			int approxLen = approx.toArray().length;
			if(approxLen > 6) {
				
				// Detect circles (costs)
				Rect circleBound = Imgproc.boundingRect(contour);
				int radius = circleBound.width / 2;

				if (Math.abs(1 - ((double)circleBound.width / circleBound.height)) <= 0.35 && // circle similarity measure
				    Math.abs(1 - (contourArea / (Math.PI * Math.pow(radius, 2)))) <= 0.35) 
				{								
					if(contourArea > outerArea)	{ // get the outer circle
						outerArea = contourArea;
					    Mat _circleImage = rectImage.submat(circleBound); // get the part of the image
					    circleImage = _circleImage; 
					    circleContour = contour;
					    k = i; // temp
					}
				}
			}		
		}  
        Highgui.imwrite(ImageProcessor.test_output + "\\nodes\\amplifiedBinRect" + num + ".bmp", binAmplifiedRect); 
		Highgui.imwrite(ImageProcessor.test_output + "\\nodes\\binRect" + num + ".bmp", binRect);
        if(k != -1)	{
	        Mat temp = rectImage.clone();
			Imgproc.drawContours(temp, contours, k, new Scalar(255,0,0), 2); 
			ProcCores.log(temp, "", 50);
			
			if(disp) {
				//Highgui.imwrite(ImageProcessor.test_output + "\\nodes\\amplifiedBinRect" + num + ".bmp", binAmplifiedRect); 
				//Highgui.imwrite(ImageProcessor.test_output + "\\nodes\\binRect" + num + ".bmp", binRect);
				Highgui.imwrite(ImageProcessor.test_output + "\\nodes\\rect_circ" + num + ".bmp", temp);      
				Highgui.imwrite(ImageProcessor.test_output + "circs\\circ" + num + ".bmp", circleImage);
			}
        }
	}
	
	/**
 		Extract text from the node and cost from the circle.
	 */
	private void extractText() {
		
		// Filter out text in green color
		textImage = new Mat();
		if(rectImage != null) textImage = ProcCores.textFilter(rectImage, ImageProcessor.getFilterColor());
		else textImage = null;
		
		// Crop the circle with cost
		if(circleImage != null){
			Rect rect = Imgproc.boundingRect(circleContour);
			Core.rectangle(textImage, new Point(rect.x - d, rect.y - d), 
					new Point(rect.x + rect.width + d, rect.y + rect.height + d), new Scalar(255,255,255), -1);
			ProcCores.log(textImage, "", 50);
		
		// Prepare cost image
		costImage = ProcCores.textFilter(circleImage, ImageProcessor.getFilterColor());	        
		}
		else costImage = null;
			
		if(disp) {
			if(textImage != null) Highgui.imwrite(ImageProcessor.test_output + "nodes\\textImage" + num + ".bmp", textImage);
			if(costImage != null) Highgui.imwrite(ImageProcessor.test_output + "\\circs\\costImage" + num + ".bmp", costImage);		
		}
	}
}