import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import sun.rmi.transport.tcp.TCPConnection;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

public class MainHandler extends ListenerAdapter {

    //Discord stuff
    private static final String token = "";
    private static JDABuilder builder;

    private HashMap<String, Message> messages;
    private Weather weatherHandler;


    public static void main(String[] args) {
        builder = new JDABuilder(AccountType.BOT);
        builder.setToken(token);
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
        this.weatherHandler = new Weather();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {    //Tegen oneindige loops
            return;
        }

        System.out.println("Message received from: " + event.getAuthor().getName() + " : " + event.getMessage().getContentDisplay());

        String message = event.getMessage().getContentDisplay();

        String celcius = this.weatherHandler.getWeatherFrom(message);
        event.getChannel().sendMessage(celcius + " graden").queue();

//        for (String key : this.messages.keySet()) {
//            if (message.equals(key)) {
//                event.getChannel().sendMessage(this.messages.get(key).getAnswerString()).queue();
//            }
//        }
    }

    private void setMessages() {
        this.messages = new HashMap<>();

        this.messages.put("+ping", new Message(MessageType.TextMessage, "+ping", "pong"));
    }
}
