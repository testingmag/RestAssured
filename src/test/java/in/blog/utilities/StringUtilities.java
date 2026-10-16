package in.blog.utilities;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringUtilities {

	private static final Logger logger = LogManager.getLogger(StringUtilities.class);

	public static Matcher patternMatcher(String format, String content) {

		Pattern pattern = Pattern.compile(format, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		return matcher;
	}

	public static String contentReplace(String content, String original, String replaceTo) {
		return content.replace(original, replaceTo);
		 
	}

	public static Boolean patternCheck(String format, String content) {

		Boolean find = Pattern.compile(format, Pattern.CASE_INSENSITIVE).matcher(content).find();
		logger.info("Pattern Check: {}", find);
		return find;
	}

	public static String encodingString(List<String> original) {

		StringJoiner encodedListJoiner = new StringJoiner(",");
		for (String rawSlug : original) {
			// Encode spaces/digits/special characters per slug
			String encodedSlug = URLEncoder.encode(rawSlug, StandardCharsets.UTF_8);
			encodedListJoiner.add(encodedSlug);
		}

		// 2. Combine them into the final list string
		String encodedList = encodedListJoiner.toString();

		return encodedList;
	}

	public static String encodingString(String original) {

		StringJoiner encodedListJoiner = new StringJoiner(",");

		String encodedSlug = URLEncoder.encode(original, StandardCharsets.UTF_8);
		encodedListJoiner.add(encodedSlug);

		String encodedList = encodedListJoiner.toString();

		return encodedList;
	}
}
