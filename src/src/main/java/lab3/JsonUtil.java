package lab3;

import com.google.gson.Gson;
import spark.ResponseTransformer;

//class to convert response to json object
public class JsonUtil {
	public static String toJson(Object object) {
		return new Gson().toJson(object);
	}

	public static ResponseTransformer json() {
		return JsonUtil::toJson;
	}
}
