package test;

import gab.opencv.OpenCV;

import java.awt.Rectangle;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

//this uses different cascades of opencv and writes the results to a database

@SuppressWarnings("serial")
public class CVdraw extends PApplet{
	Rectangle[] faces;
	private JFileChooser fc = new JFileChooser();
	static CouchDbClient dbClient = new CouchDbClient("couchdb-2.properties");
	Response resp;
	OpenCV cv;
	PImage currentImage , render;
	String currentFrameString = "";
	int resizeW = 1280, resizeH = 720;
	int frameInterval = 1;
	PGraphics pGraphics = new PGraphics();

	
		
	public void setup() {
		size(1280,720);
		pGraphics = createGraphics(width, height);
		
	try { 	
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
	} catch (Exception e) { 
		e.printStackTrace();  
	}
		
	//open file picker, choose  first file
	
	filePicker(); //make current image
	cv = new OpenCV(this, currentImage);
	cv.loadCascade(OpenCV.CASCADE_FRONTALFACE);  
		
	frameRate(1);	
	

	}	

	public void draw() {
		cvDetect();
		image(render, 0, 0);
		
	}
	
	public void cvDetect() {
		//scan with openCV
			faces = cv.detect();
			
			pGraphics.beginDraw();
			pGraphics.background(0);
			pGraphics.image(currentImage,0,0);
			pGraphics.fill(0,0);
			pGraphics.stroke(0,255,0);
			pGraphics.strokeWeight(4);
			for (int i = 0; i < faces.length; i++) {
				pGraphics.rect(faces[i].x, faces[i].y, faces[i].width, faces[i].height);
			}
			pGraphics.endDraw();
			render = pGraphics.get(0, 0, pGraphics.width, pGraphics.height);
			
		//post rect results to db
			if (faces.length > 0) {
				faceDataToDB();
			}
		//increment
		incrementImage(currentFrameString, frameInterval);	
		
		
		
	}
	
	public void faceDataToDB() {
	
		JsonObject frameData = new JsonObject();
		JsonArray facesArray = new JsonArray();
		String corpus = "corpus_1_fps";	
		frameData.addProperty("corpus", corpus);
		frameData.addProperty("project", "openCV_detect");
		frameData.addProperty("tv_show", "test");
		frameData.addProperty("airdate", "03-13-2014");
		frameData.addProperty("station", "cbs");
		
		String frameNumberString= currentFrameString;
		int last = frameNumberString.lastIndexOf(".");
		int first = frameNumberString.lastIndexOf("_");			
		frameData.addProperty("frame_number", frameNumberString.substring(first, last) );
		frameData.addProperty("imageURL", currentFrameString);
		frameData.addProperty("frame_interval", frameInterval);
		frameData.addProperty("img_height", resizeH);
		frameData.addProperty("img_width", resizeW);
		
		if (faces.length>0){
			for (int i = 0; i < faces.length; i++) {
			// gather all the rect data					
				float rectX = faces[i].x;
				float rectY = faces[i].y;
				float rectWidth = abs(faces[i].width);
				float rectHeight = abs(faces[i].height);
				String _tags = "";
				
				JsonObject rectData = new JsonObject();
				
				rectData.addProperty("face_id", i);
				rectData.addProperty("x", rectX);
				rectData.addProperty("y", rectY);
				rectData.addProperty("width", rectWidth);
				rectData.addProperty("height", rectHeight);
				rectData.addProperty("tags", _tags);

				facesArray.add(rectData);
			}	
		} 
		
		if(faces.length<1){
			frameData.add("faces", null);
		} else {
			frameData.add("faces", facesArray);
		}
		
		try
		{
		resp = dbClient.save(frameData);

		}
		catch(org.lightcouch.DocumentConflictException e)
		{
		//if we insert something that already exists
		//we get Exception in thread main org.lightcouch.DocumentConflictException: << Status: 409 (Conflict)
		}
	}
	
	
	public void filePicker(){	
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG PNG & TIFF Images", "jpg", "tiff", "png");
		File thesisDir = new File("/Volumes/USB_Storage/thesis/dataface_corpus/");
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(thesisDir);
		int returnVal = fc.showOpenDialog(this); 

		if (returnVal == JFileChooser.APPROVE_OPTION) { 
			File file = fc.getSelectedFile(); 
			if (file.getName().endsWith("jpg") || file.getName().endsWith("jpeg") || file.getName().endsWith("png")) { 
				// load the image using the given file path
				currentImage = loadImage(file.getPath());
				currentImage.resize(resizeW, resizeH);
				String textfieldString = file.getPath();
				println("file picker - current image: " + textfieldString);
				
			} else { 
				// just print the contents to the console 
				// note: loadStrings can take a Java File Object too 
			
			} 
		} else { 
		}
		
	}
	public void incrementImage(String filepath, int incrementNum) { //filepath + file of first item from settings, incrementNumber from settings
		//take current filename, find current file number, add the incrementnum, load that file

		//int fileNumberStart = filepath.lastIndexOf("_");
		//int fileNumberEnd = filepath.lastIndexOf(".");
		int incrementPadding = 4;
		String frameNumberString = frameNumberString(filepath, incrementPadding);
		int frameNumberInt = parseInt(frameNumberString);
		int newFrameInt = frameNumberInt + incrementNum;
		String newLeadingZeroString = String.format("%0"+incrementPadding +"d", newFrameInt);
		String extension = getExt(filepath);
		String filesplitString[] = filepath.split("_\\d{"+ incrementPadding +"}[." + extension + "]");
		String filePrefixString = filesplitString[0];
		println("newFrameInt: "+ newFrameInt,"prefix: " + filePrefixString, "newLeadingZeroString: "+newLeadingZeroString, "extension: "+extension);
		String _newFrameString = filePrefixString + "_" + newLeadingZeroString + "." + extension;
		currentFrameString = _newFrameString;
		currentImage = loadImage(_newFrameString);
		println("current image: " + currentFrameString);
	}
	public static String getExt(String name) {
		String ext=null;

		int pos=name.lastIndexOf('.');
		if(pos>0) ext=name.substring(pos+1).toLowerCase();

		return ext;
	}
	
	public static String frameNumberString (String fileName , int incrementPadding){ //returns file number string of a frame in a sequence
		String search = ".";
		int index = fileName.lastIndexOf(search);
		int indexOfNumber = index-4;
		String fileNumberString = fileName.substring(indexOfNumber,index);
		return fileNumberString;
	}

}
