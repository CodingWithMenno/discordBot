import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class MainHandler extends ListenerAdapter {

    private final String token = "NzUzNjI3MjI0NDEzODMxMzQx.X1o8DA.iI8S9wzI9s01Ky1aKjtkRsFYp34";
    private JDABuilder builder;

    public static void main(String[] args) {
        MainHandler mainHandler = new MainHandler();
    }

    public MainHandler() {
        this.builder = new JDABuilder(AccountType.BOT);
        this.builder.setToken(this.token);

        try {
            this.builder.buildAsync();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {    //Tegen oneindige loops
            return;
        }

        System.out.println("Message received from: " + event.getAuthor().getName() + " : " + event.getMessage().getContentDisplay());

        if (event.getMessage().getContentRaw().equals("ping")) {
            event.getChannel().sendMessage("OKE").queue();
        }
    }
}
