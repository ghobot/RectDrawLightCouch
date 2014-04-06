package test;

import java.awt.Rectangle;
import java.io.File;
import java.util.UUID;

import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;

import javax.swing.*; 
import javax.swing.filechooser.FileNameExtensionFilter;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import controlP5.*;

public class rectDrawCouchLight extends PApplet {

	Rectangle currentRect;
	Rectangle[] facesRectangles = new Rectangle[0];
	int x1, y1, x2, y2, c1, c2, frameNum=0, frameInterval = 0, frameStart = 0, smallestArea = 50, bottomSpacer = 50, white = color(255), tagCounter = 0, buttonWidth = 200, resizeW = 1280, resizeH = 720; // smallest size of dragged rect; // mouse position data

	PGraphics overlay , settingsGraphics; // rect data drawn to buffer
	PImage render ,currentImage, settingsPImage; // image drawn from buffer

	// for info and UI
	ControlP5 cP5, tagsControlP5, settingsPanelcpP5;
	controlP5.Button clearButton, nullFaces;
	controlP5.Toggle lockButton, settingsButton;
	controlP5.Textfield imageSeqLoc, frameIntervalcp5;
	boolean validArea = false, //boolean to check if clicked in valid area 
			lock = true, //boolean to turn of rect drawing behavior
			settings = true //boolean to show settings panel
			; 
	File [] fileNames;
	String fileName = "", projectName = "",  imageURL, defaultImageURL = "/Volumes/Storage/ITP/Classes/09 spring 2013/left_behind/src/faces/test/cheerios023 copy.png", textValue = "", showName="",  airdate="", tvShowString="" ,database = "", currentFrameString="";
	File imageDir; 
	static CouchDbClient dbClient = new CouchDbClient("couchdb.properties"); 

	Response resp;

	public void setup() {
		size(1280, 770);
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) { 
			e.printStackTrace();  
		}

		cP5 = new ControlP5(this); //submit button UI
		tagsControlP5 = new ControlP5(this);
		settingsPanelcpP5 = new ControlP5(this);
		//currentRect = new Rectangle();
		overlay = createGraphics(width, height); //rectangle overlay
		settingsGraphics = createGraphics(width, height); //rectangle overlay
		controls();
		overlayDraw();	
		settingsPanel();
		filePicker();
		currentFrameString = imageSeqLoc.getText();
	}

	public void draw() {
		PFont font = createFont("arial", 20, true);
		background(0);

		if (currentImage != null) { 
			// size the window and show the image 
			//size(currentImage.width,currentImage.height); 
			//image(currentImage,0,0); 
			currentImage.resize(resizeW, resizeH);
			image(currentImage, 0, 0);
		}
		projectName = settingsPanelcpP5.get(Textfield.class,"Project Name").getText();
		showName = settingsPanelcpP5.get(Textfield.class,"Show Name").getText();
		frameInterval = parseInt(settingsPanelcpP5.get(Textfield.class , "Frame Interval").getText());
		//bottom ui
		pushMatrix();
		noStroke();
		fill(40);
		rect(0, height - bottomSpacer, width, height);
		fill(255);
		textSize(12);
		text(projectName+" | "+ showName, 10, height - 20);
		text(parseInt(frameInterval), 10, height-60);

		textFont(font);			
		textSize(25);
		text(frameNum, 10 ,height-75);
		if (lock) text("LOCKED", width - 5*buttonWidth, height - 15);
		popMatrix();
		pushMatrix();
		image(render, 0, 0); //rect overlay
		popMatrix();

		if (settings) {
			pushMatrix();
			image(settingsPImage, 0, 0);
			popMatrix();
			settingsPanelcpP5.show();
			lock = true;
		} else {
			settingsPanelcpP5.hide();
		}
	}

	public static void tearDownClass() {
		dbClient.shutdown();
	}

	public void filePicker(){
		final JFileChooser fc = new JFileChooser(); 
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"JPG PNG & TIFF Images", "jpg", "tiff", "png");

		File thesisDir = new File("/Volumes/USB_Storage/thesis/dataface_corpus/");
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(thesisDir);
		int returnVal = fc.showOpenDialog(this); 

		if (returnVal == JFileChooser.APPROVE_OPTION) { 
			File file = fc.getSelectedFile(); 

			if (file.getName().endsWith("jpg") || file.getName().endsWith("jpeg") || file.getName().endsWith("png")) { 
				// load the image using the given file path
				currentImage = loadImage(file.getPath());
				String textfieldString = file.getPath();
				imageSeqLoc.setText(textfieldString);
				
			} else { 
				// just print the contents to the console 
				// note: loadStrings can take a Java File Object too 
				String lines[] = loadStrings(file); 
				for (int i = 0; i < lines.length; i++) { 
					println(lines[i]);  
					imageSeqLoc.setText(defaultImageURL);
					currentImage = loadImage(defaultImageURL);
				} 
			} 
		} else { 
			println("Open command cancelled by user."); 
			imageSeqLoc.setText(defaultImageURL);
			currentImage = loadImage(defaultImageURL);
		}
	}

	public static int frameNumberInt(String filename, int incrementPadding) { //name of file and leading Zeros in file name
		String frameNumString = frameNumberString (filename, incrementPadding);
		int frameNumberInt = parseInt(frameNumString);
		return frameNumberInt;
	}

	public static String frameNumberString (String fileName , int incrementPadding){ //returns file number string of a frame in a sequence
		String search = ".";
		int index = fileName.lastIndexOf(search);
		int indexOfNumber = index-incrementPadding;
		String fileNumberString = fileName.substring(indexOfNumber,index);
		return fileNumberString;
	}

	public static String getExt(String name) {
		String ext=null;

		int pos=name.lastIndexOf('.');
		if(pos>0) ext=name.substring(pos+1).toLowerCase();

		return ext;
	}

	public void incrementImage(String filepath, int incrementNum) { //filepath + file of first item from settings, incrementNumber from settings
		//take current filename, find current file number, add the incrementnum, load that file

		int fileNumberStart =filepath.lastIndexOf("_");
		int fileNumberEnd = filepath.lastIndexOf(".");
		int incrementPadding = fileNumberEnd-fileNumberStart - 1;
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

	}

	public void settingsPanel(){
		//draw settings fields to buffer
		PFont pfont = createFont("arial", 20, true);
		settingsGraphics.beginDraw();
		settingsGraphics.background(0, 200);
		settingsGraphics.textSize(50);
		settingsGraphics.text("settings", width-300, height/2);
		settingsGraphics.endDraw();
		settingsPImage = settingsGraphics.get(0, 0, settingsGraphics.width, settingsGraphics.height);

		Textfield projectNameTextfield = settingsPanelcpP5.addTextfield("Project Name")
				.setPosition(100,100)
				.setSize(500,50)
				.setFont(pfont)
				.setFocus(true)
				.setColor(color(255))
				.setId(-10)
				.setValue(projectName)
				;

		Textfield databaseteTextfield = settingsPanelcpP5.addTextfield("database")
				.setPosition(100,200)
				.setSize(500,50)
				.setFont(pfont)
				.setColor(color(255))
				.setId(-11)
				.setValue(database)
				;
		Textfield tvShowtTextfield = settingsPanelcpP5.addTextfield("Show Name")
				.setPosition(100,300)
				.setSize(500,50)
				.setFont(pfont)
				.setColor(color(255))
				.setId(-12)
				.setValue(showName)
				;
		settingsPanelcpP5.addTextfield("air date")
		.setPosition(100,400)
		.setSize(500,50)
		.setFont(pfont)
		.setColor(color(255))
		.setId(-13)
		.setText("03_13_14")
		;

		imageSeqLoc = settingsPanelcpP5.addTextfield("Image Sequence Location")
		.setPosition(100,500)
		.setSize(500,50)
		.setFont(pfont)
		.setColor(color(255))
		.setId(-14)
		;

		frameIntervalcp5 = settingsPanelcpP5.addTextfield("Frame Interval")
		.setPosition(700,100)
		.setSize(100,50)
		.setFont(pfont)
		.setColor(color(255))
		.setId(-15)
		.setInputFilter(ControlP5.INTEGER)
		.setText("10")
		;

		settingsPanelcpP5.addTextfield("Frame Start")
		.setPosition(700,200)
		.setSize(100,50)
		.setFont(pfont)
		.setColor(color(255))
		.setId(-16)
		.setInputFilter(ControlP5.INTEGER)
		.setValue(frameStart)
		.setText("0")
		;
	}

	public void controls() {
		PFont pfont = createFont("arial", 20, true);
		ControlFont controlFont = new ControlFont(pfont,241);
		// int buttonHeight=buttonWidth/2;

		cP5.addButton("Save Data")
		.setPosition(width - buttonWidth, height - bottomSpacer)
		.setSize(buttonWidth, bottomSpacer)		
		.setColorBackground(color(0,200,150))
		.setId(0)
		;

		clearButton = cP5.addButton("Clear Screen")
				.setPosition(width - 2*buttonWidth, height - bottomSpacer)
				.setSize(buttonWidth, bottomSpacer)
				.setId(-2)
				.setColorBackground(color(255,0,0))
				;
		nullFaces = cP5.addButton("No Faces")
				.setPosition(width - 3*buttonWidth, height - bottomSpacer)
				.setSize(buttonWidth, bottomSpacer)
				.setId(-3)
				;
		lockButton = cP5.addToggle("lock")
				.setPosition(width - 4*buttonWidth + buttonWidth/2, height - bottomSpacer)
				.setSize(buttonWidth/2, bottomSpacer)
				.setId(-4)
				.setColorBackground(color(125))
				;

		settingsButton = cP5.addToggle("settings")
				.setPosition(width - 4*buttonWidth, height - bottomSpacer)
				.setSize(buttonWidth/2, bottomSpacer)
				.setId(-5)
				.setColorBackground(color(255,255,0))
				.setCaptionLabel("Settings")
				.setMoveable(true)
				;

		cP5.getController("Save Data")
		.getCaptionLabel()
		.setFont(controlFont)
		.toUpperCase(true)
		.setSize(25)
		.setColor(white)
		.setText(" Save Data")
		;

		clearButton.getCaptionLabel()
		.setFont(controlFont)
		.setSize(25)
		.toUpperCase(true)
		.setText(" clear")
		.setColor(white)
		;

		nullFaces.getCaptionLabel()
		.setFont(controlFont)
		.setSize(25)
		.toUpperCase(true)
		.setText(" no faces")
		.setColor(white)
		;		
	}


	public void controlEvent(ControlEvent theEvent) {
		//println("got a control event from controller with id "+theEvent.getController().getId());

		if (theEvent.isFrom(settingsPanelcpP5.getController(""))) {
			println("Save Data is triggered...");
		}
		int incrementNum = parseInt(settingsPanelcpP5.get(Textfield.class , "Frame Interval").getText());
		switch(theEvent.getController().getId()) {
		
		
		case(0): //send the rect info and tags to database
		String filepath = settingsPanelcpP5.get(Textfield.class,"Image Sequence Location").getText();
		//faceDataToDB();
		println("currentFramestring: " + currentFrameString);
		incrementImage(currentFrameString, incrementNum);

		break;
		case(-2): //clear button
			validArea = false;
		resetRect();
		break;
		case(-3): //submit null faces //advance to next image
//			JSONObject nullFaces = new JSONObject();
//		nullFaces.setJSONObject("faces", null);	
		incrementImage(currentFrameString, incrementNum);
		break;
		case(-4):
			//println(theEvent.getController().getStringValue());
			//println("lock: " + lock);
		break;
		}
	}

	/*	public void faceDataToDB() {

			JSONObject slideNumber = new JSONObject();
			JSONArray faceArray = new JSONArray();

			for (int i = 0; i < facesRectangles.length; i++) {
				// gather all the rect data					
				float rectX = facesRectangles[i].x;
				float rectY = facesRectangles[i].y;
				float rectWidth = abs(facesRectangles[i].width);
				float rectHeight = abs(facesRectangles[i].height);
				String tags = tagsControlP5.get(Textfield.class,"tags "+i).getText();	
				JSONObject rectData = new JSONObject();
				rectData.setInt("face_id", i);
				rectData.setFloat("x", rectX);
				rectData.setFloat("y", rectY);
				rectData.setFloat("width", rectWidth);
				rectData.setFloat("height", rectHeight);
				rectData.setString("tags", tags);

				//faceArray.setJSONObject(i, rectData);
				slideNumber.setJSONArray("faces", faceArray);
				slideNumber.setJSONObject("faces", rectData); 

			}	

			slideNumber.setString("project", projectName);
			slideNumber.setString("tv_show", tvShow);
			slideNumber.setString("airdate", airdate);
			slideNumber.setString("slide#", fileName);
			slideNumber.setString("imageURL", imageURL);
			slideNumber.setInt("img_height", resizeH);
			slideNumber.setInt("img_width", resizeW);	
			//	slideNumber.setString("_id", generateUUID());
			try
			{
			resp = dbClient.save(slideNumber);

			}
			catch(org.lightcouch.DocumentConflictException e)
			{
			//if we insert something that already exists
			//we get Exception in thread ÒmainÓ org.lightcouch.DocumentConflictException: << Status: 409 (Conflict)
			}
		}
	 */		

	/*		public void faceDataToDB() {

			JsonObject project = new JsonObject();
			JsonObject frameData = new JsonObject();
			JsonObject facesJsonObject = new JsonObject();

			frameData.addProperty("airdate", airdate);
			frameData.addProperty("show_name", showName);
			frameData.addProperty("img_width", 1280);
			frameData.addProperty("img_height", 720);
			frameData.addProperty("project_name", projectName);
			frameData.addProperty("frame#", frameNum);
			frameData.add("faces", facesJsonObject);

			for (int i = 0; i < facesRectangles.length; i++) {
				// gather all the rect data					
				float rectX = facesRectangles[i].x;
				float rectY = facesRectangles[i].y;
				float rectWidth = abs(facesRectangles[i].width);
				float rectHeight = abs(facesRectangles[i].height);
				String tags = tagsControlP5.get(Textfield.class,"tags "+i).getText();	
				JSONObject rectData = new JSONObject();
				rectData.setInt("face_id", i);
				rectData.setFloat("x", rectX);
				rectData.setFloat("y", rectY);
				rectData.setFloat("width", rectWidth);
				rectData.setFloat("height", rectHeight);
				rectData.setString("tags", tags);

				//faceArray.setJSONObject(i, rectData);
				slideNumber.setJSONArray("faces", faceArray);
				slideNumber.setJSONObject("faces", rectData); 

			}	

			slideNumber.setString("project", projectName);
			slideNumber.setString("tv_show", showName);
			slideNumber.setString("airdate", airdate);
			slideNumber.setString("slide#", fileName);
			slideNumber.setString("imageURL", imageURL);
			slideNumber.setInt("img_height", resizeH);
			slideNumber.setInt("img_width", resizeW);	
			//	slideNumber.setString("_id", generateUUID());
			try
			{
			resp = dbClient.save(slideNumber);

			}
			catch(org.lightcouch.DocumentConflictException e)
			{
			//if we insert something that already exists
			//we get Exception in thread ÒmainÓ org.lightcouch.DocumentConflictException: << Status: 409 (Conflict)
			}
		}
	 */		
	public void saveData(int theValue) {
		//println("a button event from saveData: " + theValue);
		c1 = c2;
		c2 = color(0, 160, 100);
	}

	public void overlayDraw(boolean reset) {
		if (reset){
			facesRectangles = new Rectangle[0];
			overlay.beginDraw();
			for (int i = 0; i < facesRectangles.length; i++) {
				overlay.noFill();
				overlay.stroke(0, 255, 0);
				overlay.strokeWeight(3);
				overlay.rect(facesRectangles[i].x, facesRectangles[i].y,
						facesRectangles[i].width, facesRectangles[i].height);
			}
			overlay.background(0,0);
			overlay.endDraw();
			//println("number of tags: " + tagCounter);
			render = overlay.get(0, 0, overlay.width, overlay.height);
			for (int i = 0; i < tagCounter; i++) {
				tagsControlP5.remove("tags " + i);
			}
			tagCounter=0;		
		}
	}

	public void overlayDraw() {
		overlay.beginDraw();
		for (int i = 0; i < facesRectangles.length; i++) {
			overlay.noFill();
			overlay.stroke(0, 255, 0);
			overlay.strokeWeight(3);
			overlay.rect(facesRectangles[i].x, facesRectangles[i].y,
					facesRectangles[i].width, facesRectangles[i].height);

		}
		overlay.endDraw();
		render = overlay.get(0, 0, overlay.width, overlay.height);

	}

	public void addTags(float posX, float posY){
		PFont font = createFont("arial",20);	
		String tag = "tags " + tagCounter;
		tagsControlP5.addTextfield(tag)
		.setPosition(posX,posY)
		.setSize(200,40)
		.setFont(font)
		.setFocus(true)
		.setColor(color(255))
		.setId(tagCounter)
		;
		tagCounter++;
		background(0);
		fill(255);
		//	text(cP5.get(Textfield.class,"input").getText(), 360,130);
		//	text(textValue, 360,180);
	}


	public void mousePressed() {
		// grab the first rectangle
		if (mouseY < height - bottomSpacer) {
			validArea = true;
		} else{
			validArea = false;
		}
		currentRect = new Rectangle();
		Rectangle _rectangle = currentRect;
		if (validArea && lock==false) {
			x1 = mouseX;
			y1 = mouseY;			
			_rectangle.x = x1;
			_rectangle.y = y1;
			stroke(255);
		}
	}

	public void mouseDragged() {

		// rect update when dragging
		stroke(255, 0, 0);
		strokeWeight(2);
		fill(255, 50);
		rect(x1, y1, currentRect.width, currentRect.height);

		// mouse must not be in UI area

		if (validArea && lock==false) {
			x2 = mouseX;
			y2 = mouseY;
			// if(x2>width) x2 = width;
			// int constrainY = x2-x1;
			fill(255, 50);
			strokeWeight(3);
			stroke(0, 255, 0);
			currentRect.setSize(x2 - x1, x2 - x1);
			fill(255);
			textSize(14);
			//println("x:" + rectangle.x);
			// println("width:" + rectangle.width);
			text(abs(currentRect.width), x1 + 20, y1 + 20);
		} 
	}

	public void mouseReleased() {

		if (abs(currentRect.width) > smallestArea  && validArea && !lock) {
			Rectangle _rectangle = currentRect;
			facesRectangles = (Rectangle[]) append(facesRectangles, _rectangle);

			print("mouseReleased: ");
			printArray(facesRectangles);

			//Rectangle[] facesRectangles2 = (Rectangle[]) append(facesRectangles, rectangle);
			//println("rect X: " + rectangle.getX());
			// println(rectangle.);

			overlayDraw(); // update rect buffered image
			addTags(x1, y1);
			x2 = 0; // reset size of square
			y2 = 0;
			//printArray(facesRectangles);
		}
	}

	public void resetRect() {
		overlayDraw(true);
	}

	private static String generateUUID() { 
		return UUID.randomUUID().toString().replace("-", "");
	}
}


