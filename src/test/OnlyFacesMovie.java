package test;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class OnlyFacesMovie extends PApplet{
	PGraphics faceBuffer;
	PImage faceRender;
	JSONObject jsonObject = loadJSONObject("http://127.0.0.1:5984/opencv_detect/_design/cbs/_view/url_and_faces");
	String path, fileName;
	int x, y, rectW, rectH;
	int counter = 0;

	public void setup() {
		size(1280,770);
		faceBuffer = createGraphics(1280, 720);
		frameRate(60);
		
		//grab json from db
	}

	public void draw() {

		JSONArray rows = jsonObject.getJSONArray("rows");
		
		
		for(int i=0; i < rows.size(); i++){
			faceBuffer.beginDraw();
			//faceBuffer.background(0);
			println("url:"+jsonObject.getJSONArray("rows").getJSONObject(i).getString("key"));

			String url  = jsonObject.getJSONArray("rows").getJSONObject(i).getString("key");
			String[] urlArray= url.split("/");
			String volume = "/Volumes/My Book for Mac/";
			//String volume = "/Volumes/USB_Storage/";
			String nestedArchive = urlArray[3]+"/"+urlArray[4]+"/"+urlArray[5]+"/"+urlArray[6]+"/"+urlArray[7]+"/"+urlArray[8]+"/"+urlArray[9];
			String fileLocation = volume + nestedArchive;
			PImage image = loadImage(fileLocation);
			fileName = urlArray[9];
			int faces = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").size();		
			
			
				image.resize(1280, 720);
				
				for (int j = 0; j < faces; j++) {
					int x = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(j).getInt("x");
					int y = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(j).getInt("y");
					int width = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(j).getInt("width");
					int height = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(j).getInt("height");
					
					PImage face = image.get(x, y, width, height);
					faceBuffer.image(face,x,y);			
				}

			faceBuffer.endDraw();
			faceRender = faceBuffer.get(0, 0,faceBuffer.width, faceBuffer.height); 
			image(faceRender,0,0);
			text(fileName, 10, height-20);
			faceRender.save(sketchPath+"/ouput/opencv_movie/02_cbs_facemovie_"+String.format("%04d", counter)+".png");
			counter++;
			
			//if (counter>15) exit();
		}
		
	}
}


