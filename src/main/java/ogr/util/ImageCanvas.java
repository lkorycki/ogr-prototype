package ogr.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.imgscalr.Scalr;

public class ImageCanvas extends JPanel {
	
	protected BufferedImage image;
	protected BufferedImage resized;
	protected int resizedWidth = 0;
	
	public void drawImage(File imageFile) {
    	try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
        	System.err.println("Exception ocurred trying to read image file at: " + imageFile.getPath());
            e.printStackTrace();
        }
    	resizeImage(resizedWidth);
    }
	
	public int getImageWidth() {
		if (image == null) return 0;
		return image.getWidth();
	}
	
	public void resizeImage(int resizedWidth) {
		this.resizedWidth = resizedWidth;
		if (image != null) {
			resized = Scalr.resize(image, Scalr.Method.QUALITY, resizedWidth, Scalr.OP_ANTIALIAS);
			repaint();
		}
	}
	
	public void resizeImageWH(int w, int h) {
		this.resizedWidth = w;
		if (image != null) {
			resized = Scalr.resize(image, w, h, Scalr.OP_ANTIALIAS);
			repaint();
		}
	}
	
	public void resetSize() {
		resizedWidth = 0;
		repaint();
	}
	
	public void clearImage() {
		image = resized = null;
		repaint();
	}

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null && resizedWidth > 0) {
        	g.drawImage(resized, 0, 0, this);
        	setPreferredSize(new Dimension(resized.getWidth(), resized.getHeight()));
        }
        else if (image != null) {
        	resized = Scalr.resize(image, Scalr.Method.QUALITY, resizedWidth, Scalr.OP_ANTIALIAS);
        	g.drawImage(image, 0, 0, this);
        	setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }
        else {
        	setPreferredSize(new Dimension());
        }
        revalidate(); // needs to be called so the scrollbar adjust itself immediately after photo change
    }
	
	protected int getAreaWidth() {
		if (resized == null) return 0;
		return resized.getWidth();
	}
	
	protected int getAreaHeight() {
		if (resized == null) return 0;
		return resized.getHeight();
	}
}
