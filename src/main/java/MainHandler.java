import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

public class MainHandler extends ListenerAdapter {

    //Discord stuff
    private static final String DISCORDTOKEN = "";
    private static JDABuilder builder;

    private final String PREFIX = "gamer ";
    private HashMap<String, Message> messages;
    private APIHandler apiHandler;


    public static void main(String[] args) {
        builder = new JDABuilder(AccountType.BOT);
        builder.setToken(DISCORDTOKEN);
        MainHandler mainHandler = new MainHandler();
        builder.addEventListener(mainHandler);

        try {
            builder.buildAsync();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public MainHandler() {
        setMessages();
        this.apiHandler = new APIHandler();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {    // Tegen oneindige loops
            return;
        }

        System.out.println("Message received from: " + event.getAuthor().getName() + " : " + event.getMessage().getContentDisplay());

        String prefixMessage = event.getMessage().getContentDisplay().toLowerCase();

        if (!prefixMessage.startsWith(this.PREFIX)) {
            return;
        }

        String message = event.getMessage().getContentDisplay().toLowerCase().substring(this.PREFIX.length());

        handleMessages(event, message);
    }

    private void handleMessages(MessageReceivedEvent event, String message) {
        if (message.contains("image ")) {        // Voor random images
            int id = Integer.parseInt(message.substring(6));
            String memeURL = APIHandler.getRandomImage(id);
            event.getChannel().sendMessage(memeURL).queue();
            return;
        }

        if (message.contains("temp ")) {       // Voor de temperatuur berichten
            String stad = message.substring(5);
            String goodCity = stad.substring(0, 1).toUpperCase() + stad.substring(1);
            event.getChannel().sendMessage(APIHandler.getWeatherFrom(goodCity)).queue();
            return;
        }

        for (String key : this.messages.keySet()) {     // Voor alle niet-speciale berichten
            if (message.equals(key)) {
                event.getChannel().sendMessage(this.messages.get(key).getAnswerString()).queue();
                return;
            }
        }
    }

    private void setMessages() {
        this.messages = new HashMap<>();

        this.messages.put("ping", new Message(MessageType.TextMessage, "no :("));
        this.messages.put("temp ", new Message(MessageType.WeatherMessage, "In <city> is het <celsius> graden"));
        this.messages.put("image ", new Message(MessageType.RandomMessage, "<ImageURL>"));


        /*
            - Help command
            - All commands command
         */
    }
}
