package in.blog.autopost;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.blog.utilities.Constants;
import in.blog.utilities.RestRequest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class DetailsPosted extends Main {
	private String content;
	private ImagePosting imagePosting;
	private HashMap<String, Object> postBody;
	private Response response;
	private RestRequest restRequest;
	private JsonPath originalJsonPath;
	private JsonPath jsonPath;
	private TagsCategories tagCAT;
	private int originalPostID;
	private String originalSlug;
	private String originalModDate;
	private Date date;
	private static final Logger logger = LogManager.getLogger(DetailsPosted.class);

	public DetailsPosted(int postID) {

		this.originalPostID = postID;
		this.originalJsonPath = super.getOriginalJson();
		// long start = System.nanoTime();
		verifyPostsExists();
		// long end = System.nanoTime();
		// long elapsed = TimeUnit.MILLISECONDS.toMillis(end-start);
		// logger.info("Time took to post :" + elapsed);

	}

	private void verifyPostsExists() {
		originalSlug = this.originalJsonPath.get("find {it.id == " + this.originalPostID + "}.slug");
		restRequest = new RestRequest();
		logger.info("Searching for Slug :" + originalSlug);
		restRequest.getRequest(Constants.CT + Constants.POSTS + "?slug=" + originalSlug);
		response = restRequest.getResponse();
		jsonPath = response.jsonPath();
		List<String> modDat = jsonPath.get("modified");
		originalModDate = this.originalJsonPath.get("find {it.id == " + this.originalPostID + "}.modified");

		if (!modDat.isEmpty()) {

			if (date(originalModDate).compareTo(date(modDat.get(0))) > 0) {
				logger.info("Update available for post" + jsonPath.get("id"));
				updatePost();
			} else {
				logger.info("No Change in Post Details");
			}

		} else {
			logger.info("No Existing Posts Found");
			imagePosting = new ImagePosting(originalPostID);
			tagCAT = new TagsCategories(originalPostID);

			setPostDetails();

			postData(Constants.CT + Constants.POSTS);
		}

	}

	private void updatePost() {
		List<Integer> postIDS = jsonPath.get("id");
		this.postBody = new HashMap<String, Object>();
		this.content = originalJsonPath.get("find {it.id == " + this.originalPostID + "}.content.rendered");
		addAudio();
		logger.info("Updating post with ID :" + postIDS.get(0));
		postBody.put("content", this.content.replace("from naasongs", "from cinitunes")
				.replaceAll("Naa Songs", "Cinitunes").replaceAll("naasongs", "cinitunes"));
		postBody.put("date", originalJsonPath.get("find {it.id == " + this.originalPostID + "}.date").toString());
		// UPDATING POST WHEN THERE IS A MODIFICATION
		postData(Constants.CT + Constants.POSTS + "/" + postIDS.get(0));

	}

	private void postData(String url) {
		// POSTING POST WITH ALL DETAILS
		restRequest = new RestRequest();
		logger.info("Posting with Slug :" + originalSlug);
		restRequest.postRequest(url, super.getHeader(), this.postBody);

		// RESPONSE AFTER CREATING POST
		this.response = restRequest.getResponse();
		jsonPath = this.response.jsonPath();
		// logger.info(this.response.asString());
		logger.info("Status of Post is :" + jsonPath.get("status").toString());
		

	}

	private void setPostDetails() {

		this.postBody = new HashMap<String, Object>();
		postBody.put("title", originalJsonPath.get("find {it.id == " + this.originalPostID + "}.title.rendered"));
		postBody.put("slug", originalSlug);
		postBody.put("status", "publish");
		if (site.equals("naasongs.co")) {
			postBody.put("date", originalJsonPath.get("find {it.id == " + this.originalPostID + "}.date").toString());
			this.content = originalJsonPath.get("find {it.id == " + this.originalPostID + "}.content.rendered");
		} else {

		}

		addAudio();
		postBody.put("content", this.content.replace("from naasongs", "from cinitunes"));
		postBody.put("excerpt", originalJsonPath.get("find {it.id == " + this.originalPostID + "}.excerpt.rendered"));
		postBody.put("format", "standard");
		postBody.put("featured_media", imagePosting.getImageID());
		postBody.put("tags", tagCAT.getTagIDs());
		postBody.put("categories", tagCAT.getCATIDs());
	}

	private Date date(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("IST"));

		try {
			date = sdf.parse(str);

		} catch (ParseException e) {
			logger.info(Level.FATAL, e);
		}
		return date;

	}

	private void addAudio() {

		String[] l = org.apache.commons.lang3.StringUtils.substringsBetween(this.content, "<a href=\"", ".mp3");

		String[] b = org.apache.commons.lang3.StringUtils.substringsBetween(this.content, ".mp3", "</a>");
		if (l != null)
		for (int i = 0; i < l.length; i++) {	

			if (!this.content.contains(l[i] + b[i] + "</a>" + "\n<audio controls=\"\" src=\"" + l[i]
					+ ".mp3\" controlslist=\"nodownload\"></audio>"))
				this.content = this.content.replace(l[i] + ".mp3" + b[i] + "</a>", l[i] + b[i] + "</a>"
						+ "\n<audio controls=\"\" src=\"" + l[i] + ".mp3\" controlslist=\"nodownload\"></audio>");

		}
	}

}
