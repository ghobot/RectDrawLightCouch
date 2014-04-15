package test;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

@SuppressWarnings("serial")
public class ExtractFacesOpenCV extends PApplet{


	JSONObject jsonObject = loadJSONObject("http://127.0.0.1:5984/opencv_detect/_design/face_size/_view/url_and_face");
	String path;
	int x, y, rectW, rectH;
	int counter = 0;

	public void setup() {
		JSONArray rows = jsonObject.getJSONArray("rows");
		for(int i=0; i < rows.size(); i++){
			println("url:"+jsonObject.getJSONArray("rows").getJSONObject(i).getString("key"));

			String url  = jsonObject.getJSONArray("rows").getJSONObject(i).getString("key");
			PImage image;
			image	= loadImage(url);
			
			if (image != null) {
			image.resize(1280, 720);
						
			JSONObject faces = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONObject("value");
			//println(faces);

			int x = faces.getInt("x");
			int y = faces.getInt("y");
			int width = faces.getInt("width");
			int height = faces.getInt("height");

			PImage face = image.get(x, y, width, height);

			face.save(sketchPath+"/ouput/opencv/image_opencv_all_"+String.format("%04d", counter)+".png");

			counter++;
			}
		}
		//		println("x:"+jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(0).getInt("x"));
		//		println("y:"+jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(0).getInt("y"));
		//		println("width:"+jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(0).getInt("width"));
		//		println("height:"+jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(0).getInt("height"));
	

	exit();

}

}
