import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

public class MainHandler extends ListenerAdapter {

    //Discord stuff
    private static final String TOKEN = "";
    private static JDABuilder builder;

    private final String PREFIX = "+";
    private HashMap<String, Message> messages;
    private APIHandler apiHandler;


    public static void main(String[] args) {
        builder = new JDABuilder(AccountType.BOT);
        builder.setToken(TOKEN);
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

        String message = event.getMessage().getContentDisplay().toLowerCase();

        if (!message.startsWith(this.PREFIX)) {
            return;
        }

        if (message.contains("+random image ")) {        // Voor random images
            int id = Integer.parseInt(message.substring(14));
            String memeURL = APIHandler.getRandomImage(id);
            event.getChannel().sendMessage(memeURL).queue();
            return;
        }

        if (message.contains("+temp ")) {       // Voor de temperatuur berichten
            String stad = message.substring(6);
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

        this.messages.put("+ping", new Message(MessageType.TextMessage, "pong"));
        this.messages.put("+temp ", new Message(MessageType.WeatherMessage, "In <city> is het <celsius> graden"));
        this.messages.put("+random image", new Message(MessageType.RandomMessage, "<ImageURL>"));


        /*
            - Help command
            - All commands command
         */
    }
}
