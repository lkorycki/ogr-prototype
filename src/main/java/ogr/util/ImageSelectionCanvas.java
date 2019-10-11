package ogr.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import org.opencv.core.Rect;

public class ImageSelectionCanvas extends ImageCanvas implements MouseMotionListener, MouseListener {

	private Rectangle selection;
	private Point anchor;
    private BufferedImage prevImage;
    
    public ImageSelectionCanvas() {
    	super();
    	addMouseListener(this);
        addMouseMotionListener(this);
        prevImage = image;
    }
	
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != prevImage) {
        	selection = null;
        	prevImage = image;
        }
        if (selection != null){
            Graphics2D g2d = (Graphics2D)g;
            g2d.draw(selection);
        }
    }
    
    public void mousePressed(MouseEvent e) {
    	anchor = e.getPoint();
    	if (getAreaWidth() <= anchor.getX() || getAreaHeight() <= anchor.getY()) {
    		anchor = null;
    		selection = null;
    	} else
    		selection = new Rectangle(anchor);
    }
    public void mouseDragged(MouseEvent e) {
    	if (anchor == null || selection == null) return;
    	int width = getRectWidth(e);
    	int height = getRectHeight(e);
    	if (width > getAreaWidth() - getRectX(e)) width = getAreaWidth() - getRectX(e);
    	else if (e.getX() < anchor.getX() && width > anchor.getX()) width = (int)anchor.getX();
    	if (height > getAreaHeight() - getRectY(e)) height = getAreaHeight() - getRectY(e);
    	else if (e.getY() < anchor.getY() && height > anchor.getY()) height = (int)anchor.getY();
    	selection.setBounds( getRectX(e), getRectY(e), width, height);
    	repaint();
    }
    public void mouseReleased(MouseEvent e) {
    	if (anchor != null && selection != null) {
	    	if (getRectWidth(e) == 0 || getRectHeight(e) == 0) {
	    		selection = null;
	    	}
    	}
    	repaint();
    }
    // unused methods
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	
	private int getRectX(MouseEvent e) {
		int x = Math.min(anchor.x, e.getX());
		return Math.max(x, 0);
    }
	
	private int getRectY(MouseEvent e) {
		int y = Math.min(anchor.y, e.getY());
		return Math.max(y, 0);
    }
	
	private int getRectWidth(MouseEvent e) {
		int x = Math.max(e.getX(), 0);
    	return Math.abs(x - anchor.x);
    }
	
	private int getRectHeight(MouseEvent e) {
		int y = Math.max(e.getY(), 0);
    	return Math.abs(y - anchor.y);
    }
	
	public Rect getSelection() {
		if (selection == null) return null;
		double scale = (double)getAreaWidth()/getImageWidth();
		int x = (int)(selection.getX()/scale);
		int y = (int)(selection.getY()/scale);
		int width = (int)(selection.getWidth()/scale);
		int height = (int)(selection.getHeight()/scale);
		return new Rect(x, y, width, height);
	}
	
}
