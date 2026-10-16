package in.blog.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import in.blog.autopost.Main;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class MainExt extends Main {
	private static File file;
	private RestRequest restRequest;
	private Response response;
	private JsonPath jsonPath;

	public void beforTest() {
		Constants.FETCHURL = System.getProperty("exeURL");
		logger.info("Fetching Posts from :" + Constants.FETCHURL);
		Constants.SLUGCAT = System.getProperty("exeCAT");
		logger.info("Category to be posted :" + Constants.SLUGCAT);
		maxPages = Integer.parseInt(System.getProperty("exePages"));
		logger.info("Maximum Pages :" + maxPages);
		postsPerPage = Integer.parseInt(System.getProperty("exePPP"));
		logger.info("Posts per Page :" + postsPerPage);
		order = System.getProperty("order");
		logger.info("order :" + order); 
		orderBY = System.getProperty("orderby");
		logger.info("Order By :" + orderBY);
		startPage = Integer.parseInt(System.getProperty("startPage"));
		logger.info("Start Page :" + startPage);
		if(!System.getProperty("postSite").isEmpty()  && System.getProperty("postSite") !=null)
		Constants.CT = System.getProperty("postSite");
		logger.info("Start Page :" + Constants.CT);

	}

	public void readProp() {
		try {
		
			input = Main.class.getResourceAsStream("/properties/prop.xml");

			prop.loadFromXML(input);
			logger.info("Property file read Succesfully");
			input.close();

		} catch (IOException | NullPointerException e) {

			logger.catching(Level.FATAL, e);

		}

	}

	public void verifyToken() {
		this.restRequest = new RestRequest();
		restRequest.postRequest(Constants.CT + "/wp-json/jwt-auth/v1/token/validate", super.getHeader(),"verify");
		this.response = restRequest.getResponse();
		this.jsonPath = response.jsonPath();
		logger.info("Token Status: " + jsonPath.get("statusCode").toString());
		 
		if (jsonPath.getInt("statusCode") != 200)
			
			generateNewToken();
		

	}
	
	public void refreshToken() {
		HashMap<String, Object> postBody = new HashMap<String, Object>();
		postBody.put("refresh_token", prop.get("RFToken"));
		
		this.restRequest.postRequest(Constants.CT + "/wp-json/jwt-auth/v1/token/refresh", postBody,"");
		this.response = restRequest.getResponse();
		//logger.info(this.response.asString());
		String token = "Bearer " + this.response.jsonPath().get("data.token");
		logger.info("Token Refreshed: "+token);
		prop.setProperty("CTToken", token);
		writeProp("CTToken", token);
	}
	

	private void generateNewToken() {
		HashMap<String, Object> postBody = new HashMap<String, Object>();
		postBody.put("username", "cinitunes");
		postBody.put("password", "!SaiKamala4#");
		this.restRequest.postRequest(Constants.CT + "/wp-json/jwt-auth/v1/token", postBody,"");
		this.response = restRequest.getResponse();
		//logger.info(this.response.asString());
		String rfToken = response.getCookie("refresh_token");
		String token = "Bearer " + this.response.jsonPath().get("data.token");
		logger.info("New Token Generated: "+token);
		prop.setProperty("CTToken", token);
		writeProp("CTToken", token);
		writeProp("RFToken", rfToken);

	}

	public void writeProp(String key, String propValue) {

		Properties propUpdate = new Properties();

		propUpdate.putAll(prop);
		propUpdate.setProperty(key, propValue);

		FileOutputStream out;
		try {

			file = new File(System.getProperty("user.dir") + "/src/test/resources/properties/prop.xml");
			out = new FileOutputStream(file);
			logger.info("Property file opened Succesfully");
			propUpdate.storeToXML(out, "ID Updated on "+ LocalDate.now());
			out.close();
		}

		catch (IOException e) {
			logger.catching(Level.FATAL, e);
		}

	}

}
