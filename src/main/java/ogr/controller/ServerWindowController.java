package ogr.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import ogr.MApplication;
import ogr.ServerClient;
import ogr.util.FileUtils;

public class ServerWindowController extends Controller {
	
	private ServerClient client;
	private String selectedimageFilename;
	Vector<String> photoList;

	public ServerWindowController(MApplication mapp) {
		super(mapp);
		client = new ServerClient();
		photoList = client.getPhotoList();
	}
	
	public ActionListener okButtonListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtils fu = new FileUtils(getMApp());
				fu.addFile(client.getRootPath(), selectedimageFilename);
			}
		};
	}
	
	public Vector<String> getPhotoList() {
		return photoList;
	}

	public File getPhoto(String photoName) {
		return client.getPhoto(photoName);
	}
	
	public void setSelectedFile(int idx) {
		selectedimageFilename = photoList.get(idx);
	}
}
