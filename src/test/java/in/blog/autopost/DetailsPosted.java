package in.blog.autopost;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.blog.utilities.Constants;
import in.blog.utilities.RestRequest;
import in.blog.utilities.StringUtilities;
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

		originalSlug = StringUtilities.encodingString(originalSlug);

		restRequest.searchRequest(Constants.CT + Constants.POSTS, originalSlug);
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
		postBody.put("content", this.content.replace("sensongsmp3.live", "cinitunes.in")
				.replaceAll("Naa Songs", "Cinitunes").replaceAll(" naasongs", " cinitunes"));
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
		String title = originalJsonPath.get("find {it.id == " + this.originalPostID + "}.title.rendered");
		postBody.put("title", title);
		postBody.put("slug", originalSlug);
		postBody.put("status", "publish");
		if (site.equals("naasongs.co")) {
			postBody.put("date", originalJsonPath.get("find {it.id == " + this.originalPostID + "}.date").toString());
			this.content = originalJsonPath.get("find {it.id == " + this.originalPostID + "}.content.rendered");
		} else {
			postBody.put("date", originalJsonPath.get("find {it.id == " + this.originalPostID + "}.date").toString());
			this.content = originalJsonPath.get("find {it.id == " + this.originalPostID + "}.content.rendered");
		}

		imgInPost();
		replaceNonMP3URL(title);
		addAudio();
		postBody.put("content",this.content);
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

		Matcher matcher = StringUtilities.patternMatcher("https?://[^\\s]+", this.content);

		List<String> urls = new ArrayList<>();
		while (matcher.find()) {
			String url = matcher.group();

			url = url.replaceFirst("</a>.*$", "</a>");
			// logger.info("URL : {}", url);

			urls.add(url);

		}

		for (String url : urls) {
			if (!this.content.contains(url + "\n<audio controls") && url.contains(".mp3\"")) {
				String mp3Link = url.replaceFirst("mp3\".*", "mp3\"");

				this.content = StringUtilities.contentReplace(this.content, url,
						url + "\n<audio controls=\"\" src=\"" + mp3Link + "controlslist=\"nodownload\"></audio>");

			}

		}

		logger.info("Audio Controls Added");
	}

	private void imgInPost() {

		String[] l = org.apache.commons.lang3.StringUtils.substringsBetween(this.content, "<img loading=\"lazy\"",
				"/>");

		if (l != null)
			for (int i = 0; i < l.length; i++) {

				this.content = StringUtilities.contentReplace(this.content, "<img loading=\"lazy\"" + l[i]+"/>", "");
				// logger.info(this.content);
			}
		logger.info("Post Image Replace");
	}

	private void replaceNonMP3URL(String value) {

		Matcher matcher = StringUtilities.patternMatcher("https?://[^\\s\"]+", this.content);

		List<String> urls = new ArrayList<>();
		while (matcher.find()) {
			urls.add(matcher.group());

		}

		for (String url : urls) {

			if (!url.trim().toLowerCase().endsWith(".mp3")) {
				logger.info("URL to be replaced: {}", url);
				this.content = StringUtilities.contentReplace(this.content, "sensongsmp3.live", "cinitunes.in");
				if (!value.isBlank())
					this.content = StringUtilities.contentReplace(this.content, "telugump3s.*.>",
							"tag/telugu-" + value.charAt(0) + "\">Download</a>");

				logger.info("URL's replaced: {}", url);
			}
		}
		logger.info("Non Mp3 URL's Replace");

	}

}
