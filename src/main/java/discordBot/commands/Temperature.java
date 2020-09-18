package discordBot.commands;

import discordBot.APIHandler;
import net.dv8tion.jda.api.entities.TextChannel;

public class Temperature extends Message {

    private APIHandler apiHandler;

    public Temperature(String answerString, String description) {
        super(answerString, description);
        this.apiHandler = new APIHandler();
    }

    public void doCommand(TextChannel textChannel, String[] message) {
        String stad = message[1];
        String goodCity = stad.substring(0, 1).toUpperCase() + stad.substring(1);
        String weather = this.apiHandler.getWeatherFrom(goodCity);
        if (weather.equals("Temperature not Found")) {
            sendMessage(textChannel, "Error", weather);
        } else {
            sendMessage(textChannel, goodCity, weather);
        }
    }
}
