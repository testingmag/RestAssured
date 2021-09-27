package in.blog.autopost;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import in.blog.utilities.Constants;
import in.blog.utilities.MainExt;
import in.blog.utilities.RestRequest;
import in.blog.utilities.SeleniumBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class Main {
	
	private HashMap<String, Object> header;
	private static JsonPath jsonPath;
	protected static final Logger logger = LogManager.getLogger(Main.class);
	protected static String order, orderBY;
	private int postNumber;
	protected static int maxPages;
	protected static int postsPerPage;
	protected static int startPage;
	protected static Properties prop = new Properties();
	protected static SeleniumBuilder sb;
	private int lastID;
	protected static String site;
	protected static int newlastID;
	protected static InputStream input;
	protected MainExt ext;

	@BeforeTest
	public void checkpreviousID() {
		ext = new MainExt();
		ext.beforTest();
		ext.readProp();
		ext.verifyToken();
		site = Constants.FETCHURL.substring(Constants.FETCHURL.indexOf("//") + 2);

		lastID = Integer.parseInt(prop.getProperty(site));
		
		logger.info("Last Run First ID :" + lastID);
		
		if(!site.equalsIgnoreCase("naasongs.co"))
			sb = new SeleniumBuilder();
	}

	@Test
	public void getPosts() {

		postNumber = 1;
		newlastID = 0;

		for (int i = startPage; i <= maxPages; i++) {
			if (startPage > maxPages)
				break;
			logger.info(
					"\n-------------------------------------Posting started for New Page-----------------------------------");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("page", i);
			logger.info("Page Number is :" + i);
			jsonObject.put("per_page", postsPerPage);
			jsonObject.put("order", order);
			jsonObject.put("orderby", orderBY);
			RestRequest restRequest = new RestRequest();
			logger.info("URL: " + Constants.FETCHURL + Constants.POSTS);
			logger.info("JSON: " + Constants.FETCHURL + jsonObject.toString());
			restRequest.getRequest(Constants.FETCHURL + Constants.POSTS, jsonObject.toString());

			Response response = restRequest.getResponse();
			jsonPath = response.jsonPath();
			String message = getCodeMessage();

			if (message.equalsIgnoreCase("rest_post_invalid_page_number")) {
				logger.info("There are no more Posts to Fetch, Happy Blogging");
				break;
			}

			List<Integer> ids = jsonPath.get("id");
			logger.info("First lot of Posts :" + ids);
			loopPosts(ids);

		}
		
		
	}

	private void loopPosts(List<Integer> ids) {

		for (int id : ids) {
			logger.info(
					"\n-------------------------------------Posting started for New Post-----------------------------------");
			logger.info("Serial # of current Post posting is :" + this.postNumber);
			logger.info("Current POST: " + id);
			if (orderBY.equalsIgnoreCase("id") && lastID < id) {

				if (newlastID < id) {
					newlastID = id;
					ext.writeProp(site,String.valueOf(newlastID));
				}

			} else if (orderBY.equalsIgnoreCase("id")) {
				startPage = maxPages + 1;
				logger.info("No new posts to post");
				break;
			}

			new DetailsPosted(id);
			this.postNumber = this.postNumber + 1;
		}

	}

	public JsonPath getOriginalJson() {
		return jsonPath;
	}

	private String getCodeMessage() {
		String codeMessgae;
		try {

			codeMessgae = jsonPath.get("code");
		} catch (ClassCastException cce) {

			codeMessgae = "";

		}
		return codeMessgae;
	}

	public HashMap<String, Object> getHeader() {
		this.header = new HashMap<String, Object>();
		this.header.put("User-Agent", "RestAssured");
		this.header.put("Authorization", prop.get("CTToken"));
		return this.header;
	}

}
