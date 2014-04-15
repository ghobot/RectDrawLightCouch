package test;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

@SuppressWarnings("serial")
public class ExtractFacesCVandTurk extends PApplet{


	JSONObject jsonObject = loadJSONObject("http://127.0.0.1:5984/opencv_detect/_design/face_size/_view/url_and_face");
	JSONObject turk = loadJSONObject("http://127.0.0.1:5984/gregturk/_design/faces/_view/cbs");
	String path;
	int x, y, rectW, rectH;
	int counter = 0;

	public void setup() {

		JSONArray rows = turk.getJSONArray("rows");
		JSONArray rows2= jsonObject.getJSONArray("rows");
		for(int i=0; i < rows.size(); i++){

			String url  = rows.getJSONObject(i).getString("key");
			
			PImage image;
			image	= loadImage(url);
			image.resize(1280, 720);
			//println("i = "+i, url);
			
			for (int j = 0; j < rows2.size(); j++) {

				String url2 = rows2.getJSONObject(j).getString("key");
				
				if (url2.contains(url)) {
					println("url:"+url);
					println("key: " + url2);
					
					JSONObject faces = jsonObject.getJSONArray("rows").getJSONObject(j).getJSONObject("value");
					//println(faces);

					int x = faces.getInt("x");
					int y = faces.getInt("y");
					int width = faces.getInt("width");
					int height = faces.getInt("height");

					PImage face = image.get(x, y, width, height);

					face.save(sketchPath+"/ouput/opencv_turk/image_opencv_turk_"+String.format("%04d", counter)+".png");

					counter++;
					//if (counter>5)exit();
				}
			}
		}

		exit();

	}

}
