package in.blog.autopost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/*import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;*/

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.blog.utilities.Constants;
import in.blog.utilities.RestRequest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class TagsCategories extends Main {
	private List<Integer> originalTags;
	private RestRequest restRequest;
	private Response response;
	private JsonPath jsonPath;
	private List<Integer> tagIds;
	private ArrayList<Integer> catIds;
	private Response tagResponse;
	private static final Logger logger = LogManager.getLogger(TagsCategories.class);

	public TagsCategories(int postID) {
		this.originalTags = new ArrayList<>();
		this.originalTags = super.getOriginalJson().get("find {it.id == " + postID + "}.tags");
		logger.info("Original Tgas are :" + this.originalTags);
		verifyTags();
		searchCategories();

	}

	public List<Integer> getTagIDs() {
		return this.tagIds;
	}

	public ArrayList<Integer> getCATIDs() {
		return this.catIds;
	}

	private void verifyTags() {
		restRequest = new RestRequest();

		this.tagIds = new ArrayList<Integer>();
		List<String> tagsToBeCreated = fetchTagSlugs();

		for (String slug : tagsToBeCreated) {
			String name = jsonPath.get("find {it.slug == \"" + slug + "\"}.name");
			logger.info("Tags created for Slug :" + slug);
			restRequest.postRequest(Constants.CT + Constants.TAGS, super.getHeader(), createBody(name, slug));
			response = restRequest.getResponse();
			JsonPath jPath = response.jsonPath();
			this.tagIds.add(jPath.getInt("id"));

		}
		logger.info("Tags to be added to post are :" + tagIds.toString());
	}

	private List<String> fetchTagSlugs() {

		String tagList = this.originalTags.toString();
		tagList = tagList.substring(1, tagList.indexOf("]"));

		logger.info("Fetch Slugs for all Tags from Original");

		restRequest.getRequest(Constants.FETCHURL + Constants.TAGS + "?include=" + tagList);
		tagResponse = restRequest.getResponse();
		jsonPath = tagResponse.jsonPath();
		List<String> list = jsonPath.getList("slug");
		tagList = list.toString();
		tagList = tagList.substring(1, tagList.indexOf("]"));
		list = compareTagSlugs(list, tagList);

		return list;

	}

	private List<String> compareTagSlugs(List<String> targetTagSlugs, String tagSlugList) {
		logger.info("Checking for Existing Tags");
		List<String> diff = new ArrayList<String>(targetTagSlugs);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.info(Level.FATAL,e);
		}
		restRequest.getRequest(Constants.CT + Constants.TAGS + "?slug=" + tagSlugList);
		Response response = restRequest.getResponse();
		JsonPath tagPath = response.jsonPath();
		List<Integer> existingIDS = tagPath.getList("id");
		if (!existingIDS.isEmpty()) {
			logger.info("Existing Tags found were :" + existingIDS.toString());
			tagIds = existingIDS;
		}

		diff.removeAll(tagPath.getList("slug"));
		logger.info("Tags to be created are :" + diff.toString());
		return diff;
	}

	private HashMap<String, Object> createBody(String name, String slug) {
		HashMap<String, Object> tagBody = new HashMap<String, Object>();
		tagBody.put("name", name);
		tagBody.put("slug", slug);
		return tagBody;
	}

	private void searchCategories() {
		String catSlug = Constants.SLUGCAT;
		logger.info("Checking for Existing Category");
		restRequest.getRequest(Constants.CT + Constants.CAT + "?slug=" + catSlug.toLowerCase());
		Response catResponse = restRequest.getResponse();

		JsonPath catPath = catResponse.jsonPath();
		catIds = catPath.get("id");
		if (catIds.isEmpty()) {
			logger.info("Existing Category Not Found");
			restRequest.postRequest(Constants.CT + Constants.CAT, super.getHeader(),
					createBody(catSlug, catSlug.toLowerCase()));
			catResponse = restRequest.getResponse();
			catPath = catResponse.jsonPath();
			catIds.add(catPath.getInt("id"));
		}

	}

}
