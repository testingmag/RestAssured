package in.blog.utilities;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.util.HashMap;
import org.json.JSONObject;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class RestRequest {

	private Response response;

	public void getRequest(String getURL) {
		setResponse(given().when().get(getURL));
	}
	
	public void getRequestParam(String getURL, String param) {
		setResponse(given().urlEncodingEnabled(false).queryParam("slug", param).when().get(getURL));
	}
	public void searchRequest(String getURL, String param) {
		setResponse(given().urlEncodingEnabled(false).queryParam("search", param).when().get(getURL));
	}


	public void getRequest(String getURL, String jsonObject) {

		setResponse(
				given().contentType(ContentType.JSON).when().body(jsonObject).get(getURL).then().extract().response());
	}

	public void postRequest(String postURL, HashMap<String, Object> header, File file,
			HashMap<String, String> postDetails) {
		setResponse(given().headers(header).multiPart(file).formParams(postDetails).when().post(postURL).then()
				.extract().response());

	}

	public void postRequest(String postURL, HashMap<String, Object> header, HashMap<String, Object> postDetails,
			HashMap<String, Integer> postIntDetails) {
		setResponse(given().headers(header).formParams(postDetails).formParams(postIntDetails).when()
				.post(postURL).then().extract().response());

	}
	
	public void postRequest(String postURL, HashMap<String, Object> header, HashMap<String, Object> postDetails) {
		JSONObject json = new JSONObject(postDetails);
		setResponse(given().headers(header).contentType(ContentType.JSON).when().body(json.toString())
				.post(postURL));
		//.then().extract().response()

	}
	
	public void postRequest(String postURL, HashMap<String, Object> object, String verify) {
		
		if (verify.equalsIgnoreCase("verify")) {
		setResponse(given().headers(object).post(postURL));}
		else {
			JSONObject json = new JSONObject(object);
			setResponse(given().contentType(ContentType.JSON).when().body(json.toString()).post(postURL));
		}
		

	}
	
 public void postReqCookie(String postURL, HashMap<String, Object> object, String cookie) {
		
		
		setResponse(given().headers(object).cookie("__test",cookie).queryParam("i", 1).post(postURL));
		
		

	}


	public Response getResponse() {
		//System.out.println(this.response.asString());
		return this.response;
	}

	private void setResponse(Response response) {
		this.response = response;
	}

}
