package in.blog.autopost;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import in.blog.utilities.Constants;
import in.blog.utilities.RestRequest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class ImagePosting extends Main {

	private static File file;
	private int imageID;
	private Response response;
	private HashMap<String, String> imageDetails;
	private String imageName;
	private String imageurl;
	private RestRequest restRequest;
	private URL url;
	private InputStream is;
	private OutputStream os;
	private static final Logger logger = LogManager.getLogger(ImagePosting.class);

	public ImagePosting(int postID) {

		imageID = super.getOriginalJson().get("find {it.id == " + postID + "}.featured_media");
		if (imageID > 0) {
			restRequest = new RestRequest();
			restRequest.getRequest(Constants.FETCHURL + Constants.MEDIA + "/" + imageID);
			this.response = restRequest.getResponse();

			getImageDetails();

			if (searchImage() == -1) {
				setImageFile();
				uploadImage();
			}
		}

	}

	public int getImageID() {
		return this.imageID;
	}

	private void uploadImage() {
		HashMap<String, Object> header = super.getHeader();
		header.put("Content-Disposition", "attachment");
		header.put("ContentType", "multipart/form-data");

		logger.info("Uploading Image with Name :" + this.imageName);
		logger.info("File size: "+file.getAbsoluteFile().length());
		restRequest.postRequest(Constants.CT + Constants.MEDIA, header, file, imageDetails);
		deleteBufferedImage();

		try {
		this.response = restRequest.getResponse();
		//logger.info(this.response.asString());
	
		this.imageID = 	response.jsonPath().get("id");
		if(this.imageID < 0);
		return;
		}
		catch (NullPointerException je) {
			logger.info(Level.FATAL,je);
		}

	}

	private void getImageDetails() {

		this.imageurl = this.response.jsonPath().get("link");
		this.imageurl = this.imageurl.replace("\\?", "-");
		// this.imageurl = this.imageurl.replace("?1", "-1");
		if (Constants.FETCHURL.equals("https://naasongs.co")) {
			this.imageurl = this.imageurl.replace("http://naa0.getingup.com", Constants.FETCHURL);
			this.imageurl = this.imageurl.replace("secure.", "");

		}
		this.imageurl.trim();
		JsonPath jpe = response.jsonPath();
		this.imageDetails = new HashMap<String, String>();
		imageDetails.put("media_type", jpe.get("media_type").toString());
		imageDetails.put("mime_type", jpe.get("mime_type").toString());
		imageDetails.put("date", jpe.get("date").toString());
		imageDetails.put("date_gmt", jpe.get("date").toString());
		imageDetails.put("description", jpe.get("alt_text").toString().replace(" - Naa Songs", ""));
		imageDetails.put("alt_text", jpe.get("alt_text").toString().replace(" - Naa Songs", ""));

	}

	private void setImageName() {
		
		  String[] str = new String(this.response.jsonPath().get("guid.rendered").toString()).split("/");
		  
		  int i = str.length; 
		  this.imageName = str[i - 1];
		 	
	}

	private void setImageFile() {
		BufferedImage img;
		try {
			setImageName();
			logger.info("URL of the image is :" + this.imageurl);
			url = new URL(this.imageurl.replace("http:", "https:"));
			img = ImageIO.read(url);
			file = new File(this.imageName);
			ImageIO.write(img, "png", file);
			img.flush();

		} catch (IOException io) {
			otherMethodForFile();
			logger.info(Level.FATAL, io);
		}

		if (file.length() <= 0)
			otherMethodForFile();

	}

	private void otherMethodForFile() {

		String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

		try {
			// This socket type will allow to set user_agent
			URLConnection con = url.openConnection();
			// Setting the user agent
			con.setRequestProperty("User-Agent", USER_AGENT);
			// Getting content Length
			int contentLength = con.getContentLength();
			logger.info("File contentLength = " + contentLength + " bytes");

			is = url.openStream();

			os = new FileOutputStream(this.imageName);
			byte[] buffer = new byte[2048];
			int length;

			while ((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);

			}
			is.close();
			os.close();
		} catch (FileNotFoundException e1) { // TODO Auto-generated catch block
			logger.info(Level.FATAL, e1);
		} catch (IOException io) {
			logger.info(Level.FATAL, io);
		}

		file = new File(this.imageName).getAbsoluteFile();
		logger.info(file.exists());

	}

	private void deleteBufferedImage() {
		try {
			logger.info("Deleting file created at temp location :" + file.getPath().toString());
			Files.delete(file.toPath());
		} catch (IOException e) {

			logger.info(Level.FATAL, e);
		}

	}

	private int searchImage() {
		
		setImageName();
		logger.info("Checking for Existing Image");
		restRequest.getRequest(Constants.CT + Constants.MEDIA + "?slug="
				+ this.imageName.toLowerCase().substring(0, this.imageName.indexOf(".")));
		Response imageResponse = restRequest.getResponse();

		this.imageID = -1;
		JsonPath tagPath = imageResponse.jsonPath();
		List<Integer> imageIDs = tagPath.get("id");
		if (!imageIDs.isEmpty()) {
			
			this.imageID = imageIDs.get(0);
			logger.info("Existing Image Found: "+this.imageID);
		} else {
			logger.info("Image not found :" + this.imageName);
		}
		return imageID;
	}
}
