import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APIHandler {

    private static final String WeatherAPIKey = "7b1198570c69d548131bb8b6e07024c1";

    private OkHttpClient client;

    public APIHandler() {
        this.client = new OkHttpClient();
    }

    public static String getWeatherFrom(String city) {
        try {
            JSONObject jObject = getAPI("http://api.openweathermap.org/data/2.5/weather?q=" + city + ",nl&APPID=" + WeatherAPIKey);

            JSONObject mainJson = jObject.getJSONObject("main");
            double weather = ((double) mainJson.get("temp") - 273.15);
            String rounded = String.format("%.1f", weather);

            return "In " + city + " is het " + rounded + " graden";

        } catch (Exception e) {
            return "Temperature not Found";
        }
    }

    public static String getRandomImage(int id) {
        try {

            JSONObject jObject = getAPI("http://alpha-meme-maker.herokuapp.com/memes/" + id + "/");
            JSONObject dataJson = jObject.getJSONObject("data");
            String imageURL = String.valueOf(dataJson.get("image"));
            return imageURL;

        } catch (Exception e) {
            return "Random meme not found";
        }
    }

    private static JSONObject getAPI(String url) {
        try {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response responses = null;

            responses = client.newCall(request).execute();

            String jsonData = null;
            jsonData = responses.body().string();

            JSONObject jsonObject = new JSONObject(jsonData);
            System.out.println(jsonObject);
            return jsonObject;

        } catch (Exception e) {
            return null;
        }
    }
}
