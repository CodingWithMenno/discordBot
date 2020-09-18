package discordBot.commands;

import discordBot.APIHandler;
import net.dv8tion.jda.api.entities.TextChannel;

public class Temperature extends Message {

    public Temperature(String answerString, String description) {
        super(answerString, description);
    }

    public void doCommand(TextChannel textChannel, String[] message, APIHandler apiHandler) {
        String stad = message[1];
        String goodCity = stad.substring(0, 1).toUpperCase() + stad.substring(1).toLowerCase();
        String weather = apiHandler.getWeatherFrom(goodCity);
        if (weather.equals("Temperature not Found")) {
            sendMessage(textChannel, "Error", weather);
        } else {
            sendMessage(textChannel, goodCity, weather);
        }
    }
}
