package lab3;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import spark.utils.IOUtils;

//class to send request and receive response
public class HttpUtil {

	// send HTTP request
	// http://localhost:8080/search
	public static Response request(String method, String path) {
		try {
			URL url = new URL(path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setDoOutput(true);
			connection.connect();
			String body = IOUtils.toString(connection.getInputStream());
			return new Response(connection.getResponseCode(), body);
		} catch (IOException e) {
			//e.printStackTrace();
			return null;
		}
	}

	public static class Response {

		public final String body;
		public final int status;

		public Response(int status, String body) {
			this.status = status;
			this.body = body;
		}

		public Map<String,Object> json() {
			return new Gson().fromJson(body, HashMap.class);
		}
	}
}
