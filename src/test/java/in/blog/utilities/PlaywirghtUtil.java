package in.blog.utilities;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;


public class PlaywirghtUtil {
	
	private static final String base = Constants.CT;
	
	private static String testCookie;
	private static String userAgent;
	
	static {

        try (Playwright pw = Playwright.create()) {
            Browser browser = pw.chromium().launch();
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate(base + "/");
            page.waitForFunction("document.cookie.includes('__test=')");

            for (Cookie c : context.cookies()) {
                if (c.name.equals("__test")) 
                	testCookie = c.value;
            }
            userAgent = (String) page.evaluate("navigator.userAgent");
        }
        if (testCookie == null) {
            throw new IllegalStateException("__test cookie was not found. The host may have changed its challenge.");
        }
       
	}
	
	public String getCookie() {
		return testCookie;
	}
	
	public static String getUserAgent() {
        return userAgent;
    }

}
