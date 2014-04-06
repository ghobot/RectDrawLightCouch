package test;

import org.lightcouch.CouchDbClient;

import com.google.gson.JsonObject;

public class LighcouchTest {
	static CouchDbClient dbClient = new CouchDbClient("couchdb-2.properties"); 
	
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		JsonObject project = new JsonObject();
		JsonObject slide_data = new JsonObject();
		JsonObject facesJsonObject = new JsonObject();
		
		slide_data.addProperty("show_air", "03-13-14");
		slide_data.addProperty("show_name", "revenge");
		slide_data.addProperty("img_width", 1280);
		slide_data.addProperty("img_height", 720);
		slide_data.add("faces", facesJsonObject);
		
		for (int i = 0; i < 3; i++) {
			facesJsonObject.addProperty("face number", i);
			facesJsonObject.addProperty("face_num", 2);
			facesJsonObject.addProperty("width", 12.0);
			facesJsonObject.addProperty("height", 12.0);
			facesJsonObject.addProperty("x", 45);
			facesJsonObject.addProperty("y", 195);
			facesJsonObject.addProperty("tags", "foo foo foo foo");
			
		}
		
		
		project.add("project_name", slide_data);
		dbClient.save(project);
	}

}
