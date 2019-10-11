package ogr.util;

import java.awt.Component;
import java.nio.file.*;

import javax.swing.JOptionPane;

import ogr.MApplication;

public class FileUtils {
	
	private MApplication mapp;
	private Component parentWindow;
	private String localPath = "D:/Photos/";
	
	public FileUtils(MApplication mapp) {
		this.mapp = mapp;
		parentWindow = (Component)mapp.getMainWindow();
	}

	public void addFile(String path, String filename) {
		if (!path.endsWith("/")) path = path.concat("/");
		Path src = Paths.get(path + filename);
		Path dst = Paths.get(localPath + filename);
		try {
			Files.copy(src, dst);
		} catch (FileAlreadyExistsException e) {
			JOptionPane.showMessageDialog(parentWindow,
					"There is already a file named:\n" + filename + "\nin your workspace folder.",
					"Copy error", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(parentWindow,
					"Failed to copy local image file.",
					"Copy error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		mapp.getItemListModel().updateAll(); // refresh list
	}
	
}
