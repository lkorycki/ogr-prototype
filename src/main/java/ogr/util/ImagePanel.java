package ogr.util;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Rect;

public class ImagePanel extends JPanel {
	
	private ImageSelectionCanvas canvas;
	private JScrollPane scrollPane;
	
	private JCheckBox fitToWidthCheckbox;
	private JSlider slider;
	
	public ImagePanel() {
		super();
		setLayout(new BorderLayout());
		canvas = new ImageSelectionCanvas();
		scrollPane = new JScrollPane(canvas);
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		fitToWidthCheckbox = new JCheckBox("Fit to width");
		fitToWidthCheckbox.setSelected(true);
		fitToWidthCheckbox.addActionListener(getFitToWidthCheckboxListener());
		panel.add(fitToWidthCheckbox);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		panel.add(horizontalGlue);
		
		slider = new JSlider(10, 100, 100);
		slider.addChangeListener(getSliderListener());
		panel.add(slider);
	}
	
	public void setImage(File imageFile) {
		if (imageFile == null) {
			canvas.clearImage();
		}
		else {
			canvas.drawImage(imageFile);
			canvas.resizeImage((int) scrollPane.getSize().getWidth());
			repaint();
		}
	}
	
	private ActionListener getFitToWidthCheckboxListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int imageWidth = canvas.getImageWidth();
				if (fitToWidthCheckbox.isSelected()) {
					double scrollWidth = scrollPane.getSize().getWidth();
					slider.setValue((int)(scrollWidth/imageWidth*100));
				}
				else {
					slider.setValue(imageWidth);
				}
			}
		};
	}
	
	private ChangeListener getSliderListener() {
		return new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider src = (JSlider)e.getSource();
			    if (!src.getValueIsAdjusting()) {
			    	double zoom = src.getValue()/100.0;
			    	int width = canvas.getImageWidth();
			    	canvas.resizeImage((int)(width*zoom));
			    }
			}
		};
	}
	
	public Rect getSelection() {
		return canvas.getSelection();
	}
}
