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

public class Weather {

    private final String APIKey = "7b1198570c69d548131bb8b6e07024c1";

    private OkHttpClient client;

    public Weather() {
        this.client = new OkHttpClient();
    }

    public String getWeatherFrom(String city) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://api.openweathermap.org/data/2.5/weather?q="+ city + ",nl&APPID=" + APIKey)
                .build();
        Response responses = null;

        try {
            responses = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonData = null;
        try {
            jsonData = responses.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject Jobject = new JSONObject(jsonData);
        JSONObject mainJson = Jobject.getJSONObject("main");
        double weather = ((double) mainJson.get("temp") - 273.15);
        String rounded = String.format("%.1f", weather);
        return rounded;
    }
}
