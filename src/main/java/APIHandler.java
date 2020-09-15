import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.util.Random;

public class APIHandler {

    private static final String WeatherAPIKey = "";

    private OkHttpClient client;
    private static Random random;

    public APIHandler() {
        this.client = new OkHttpClient();
        this.random = new Random();
    }

    public String getWeatherFrom(String city) {
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

    public String getRandomImage(String id) {
        try {

            int number;

            if (id.equals("random")) {
                number = random.nextInt(259);
            } else {
                number = Integer.parseInt(id);
            }

            JSONObject jObject = getAPI("http://alpha-meme-maker.herokuapp.com/memes/" + number + "/");
            JSONObject dataJson = jObject.getJSONObject("data");
            String imageURL = String.valueOf(dataJson.get("image"));
            return imageURL;

        } catch (Exception e) {
            return "Random image not found";
        }
    }

    public String getRandomImage() {    // Maakt gebruik van API van stan
        try {
            JSONObject jObject = getAPI("http://h2892166.stratoserver.net/api/meme/?subs=funny");
            String imageURL = String.valueOf(jObject.get("result"));
            return imageURL;

        } catch (Exception e) {
            return "Random image not found";
        }
    }

    private JSONObject getAPI(String url) {
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
