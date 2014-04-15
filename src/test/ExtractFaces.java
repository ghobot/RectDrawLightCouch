package test;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

@SuppressWarnings("serial")
public class ExtractFaces extends PApplet{

	JSONObject jsonObject = loadJSONObject("http://127.0.0.1:5984/gregturk/_design/faces/_view/cbs");
	
	String path;
	int x, y, rectW, rectH;
	int counter = 0;
	
	public void setup() {
		JSONArray rows = jsonObject.getJSONArray("rows");
		for(int i=0; i < rows.size(); i++){
		println("url:"+jsonObject.getJSONArray("rows").getJSONObject(i).getString("key"));
		
		String url  = jsonObject.getJSONArray("rows").getJSONObject(i).getString("key");
		PImage image = loadImage(url);
		image.resize(1280, 720);
		int faces = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").size();
		//println(faces);
		for (int j = 0; j < faces; j++) {
			int x = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(j).getInt("x");
			int y = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(j).getInt("y");
			int width = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(j).getInt("width");
			int height = jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(j).getInt("height");
			
			PImage face = image.get(x, y, width, height);
			if (counter>589){
			face.save(sketchPath+"/ouput/image"+String.format("%03d", counter)+".png");
			}
			counter++;
		}
//		println("x:"+jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(0).getInt("x"));
//		println("y:"+jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(0).getInt("y"));
//		println("width:"+jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(0).getInt("width"));
//		println("height:"+jsonObject.getJSONArray("rows").getJSONObject(i).getJSONArray("value").getJSONObject(0).getInt("height"));
		}
		
		exit();

	}
	
}
