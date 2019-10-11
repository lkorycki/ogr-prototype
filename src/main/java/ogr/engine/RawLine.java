package ogr.engine;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class RawLine {
	
	@SuppressWarnings("unused")
	private MatOfPoint lineContour;
	private Point srcPoint;
	private Point dstPoint;
	
	public RawLine()	{
		
	}
	
	public RawLine(MatOfPoint _lineContour, Point _srcPoint, Point _dstPoint)	{
		lineContour = _lineContour;
		srcPoint = _srcPoint;
		dstPoint = _dstPoint;
	}
	
	public Point getSrcPoint()
	{
		return srcPoint;
	}
	
	public Point getDstPoint()
	{
		return dstPoint;
	}
}
