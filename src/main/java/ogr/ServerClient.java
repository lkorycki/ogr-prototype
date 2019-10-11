package ogr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

public class ServerClient {
	
	private String rootPath = "src\\test\\resources\\ClientFolder\\";
	private static String serverPort = "4567";
	private static String serverHost = "http://localhost";
	
	HttpClient client;
	
	public ServerClient() {
		
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		client = HttpClientBuilder.create().build();
		
	}
	
	public static String getHost() {
		return serverHost;
	}
	
	public static String getPort() {
		return serverPort;
	}
	
	public static void setHost(String newHost) {
		serverHost = newHost;
	}
	
	public static void setPort(String newPort) {
		serverPort = newPort;
	}
	
	public String getRootPath() {
		return rootPath;
	}
	
	/**
		Get a photo from the server by name.
		@param photoName name of the photo to get (with extension)
	 */
	public File getPhoto(String photoName)	{
		
		String url = serverHost + ":" + serverPort + "/getPhoto/" + photoName;
		 
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet(url);
 
		HttpResponse response = null;
		try {
			response = httpClient.execute(getRequest);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
 
		String filePath = rootPath + photoName;
		File file = new File(filePath);
		
        HttpEntity entity = response.getEntity();        
        if (entity != null) {
            try {
				BufferedInputStream bInputStream = new BufferedInputStream(entity.getContent());
				BufferedOutputStream bOutputStream = new BufferedOutputStream(new FileOutputStream(file));
				
				int inByte;
				while((inByte = bInputStream.read()) != -1) bOutputStream.write(inByte);
				
				bInputStream.close();
				bOutputStream.close();
				
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
        }
        
        return file;
	}
	
	/**
		Get the list of photos on the server.
	 */
	public Vector<String> getPhotoList() {
		
		Vector<String> photoList = new Vector<String>();
		String url = serverHost + ":" + serverPort + "/getList";
		HttpGet getRequest = new HttpGet(url);
		
		HttpResponse response = null;
		try {
			response = client.execute(getRequest);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Could not make a connection with the server:\n"
					+ serverHost + ":" + serverPort,
					"Connection error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String result = "";
			String line = "";
			while ((line = bufferedReader.readLine()) != null)
				photoList.add(line);
			
			System.out.println(result.toString());
			
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
	
		return photoList;
	}
	
	/**
		Send a photo on the server.
		@param photoName name of the photo to send (with extension)
	 */
	public void sendPhoto(String photoName)	{
		
	    HttpPost postRequest = new HttpPost(serverHost + ":" + serverPort + "/addPhoto/" + photoName);
		    
		File file = new File(rootPath + photoName);
		HttpEntity httpEntity = MultipartEntityBuilder.create()
		    	    .addBinaryBody("imgFile", file, ContentType.create("image/jpeg"), "test_up.jpg")
		    	    .build();		    
		postRequest.setEntity(httpEntity);
		
		try {
			client.execute(postRequest);
		} catch (IOException e) {
				e.printStackTrace();
		}		 		    
	}
	
	public static void main(String[] args) {
		
		ServerClient client = new ServerClient();
		//client.getPhoto("test_down.jpg");
		//client.getPhotoList();
		client.sendPhoto("test1.jpg");
	}

}
