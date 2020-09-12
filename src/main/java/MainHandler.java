import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MainHandler extends ListenerAdapter {

    //Discord stuff
    private static final String DISCORDTOKEN = "";
    private static JDABuilder builder;

    private final String PREFIX = "gamer ";     // Start command voor de bot
    private HashMap<String, Message> messages;
    private APIHandler apiHandler;
    private PrintWriter printWriter;
    private ArrayList<String> blackListedWords;


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
        setBlacklistWords();
        this.apiHandler = new APIHandler();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {     // Word aangeroepen als een bericht binnenkomt

        if (event.getAuthor().isBot()) {        // Tegen oneindige loops
            return;
        }

        if (!event.getChannel().getName().equals("bot-commands")) {     // Hierdoor werkt de bot alleen in de bot-commands channel
            return;
        }

        System.out.println("Message received from: " + event.getAuthor().getName() + " : " + event.getMessage().getContentDisplay());

        String prefixMessage = event.getMessage().getContentDisplay().toLowerCase();

        if (!prefixMessage.startsWith(this.PREFIX)) {
            return;
        }

        String message = event.getMessage().getContentDisplay().toLowerCase().substring(this.PREFIX.length());

        handleMessage(event, message);
    }

    private void handleMessage(MessageReceivedEvent event, String message) {

        if (message.contains("suggestion ")) {    // Voor nieuwe command ideeën
            String filteredMessage = filterMessage(message.substring(11));
            saveToFile("src/main/resources/CommandSuggestions.txt", event.getAuthor().getName() + " : " + filteredMessage);
        }

        if (message.contains("image ")) {        // Voor random images
            String id = message.substring(6);
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
            if (message.contains(key)) {
                event.getChannel().sendMessage(this.messages.get(key).getAnswerString()).queue();
                return;
            }
        }
    }

    private String filterMessage(String message) {
        for (String blackListWord : this.blackListedWords) {
            if (message.contains(blackListWord)) {
                return "BLOCKED MESSAGE";
            }
        }
        return message;
    }

    private void saveToFile(String fileName, String text) {
        try {

            this.printWriter = new PrintWriter(new FileWriter(fileName, true));

            this.printWriter.append(text + "\n");
            this.printWriter.close();

            System.out.println("Wrote something to: " + fileName);

        } catch (IOException e) {
            System.out.println("Something went wrong while writing to the file: " + fileName);
        }
    }

    private void setMessages() {
        this.messages = new HashMap<>();        // Alle mogelijke commands


        this.messages.put("ping", new Message(MessageType.TextMessage, new String[]{"No :(", "Pong!", "Oke boomer"}, "Pong"));

        this.messages.put("temp ", new Message(MessageType.WeatherMessage, "In <city> is het <celsius> graden", "Returns the temperature of the chosen city (only in the Netherlands) by typing a city after the command"));

        this.messages.put("image ", new Message(MessageType.TextMessage, "<ImageURL>", "Returns a chosen/random image by typing a number or \"random\" behind the command"));

        this.messages.put("suggestion ", new Message(MessageType.SuggestionMessage, "Thanks for the suggestion :)", "Type a new command suggestion after this command and maybe it will be implemented"));

        String munt = "https://www.budgetgift.nl/604/0/0/1/ffffff00/441842e6/a19c84e94684a0c00a7658d0be40c75b26e02e700df22c7459be5c4cfcd6438b/1-euro-munt.png";
        String kop = "https://lh3.googleusercontent.com/proxy/b6R7JJIRDsHo93TPOCtRovf1Ia26S899o_a6qVAB2dqxLNebwYkHUU3_GlDsas1PhBh5kJuD008gDtzQn-clQGhNDusN9T18reReGM9s-bO6VVijn8-eV6aL7xiX3LxulscR66R2TTyZGpHG";
        String kant = "https://upload.wikimedia.org/wikipedia/commons/6/67/1_oz_Vienna_Philharmonic_2017_edge.png";
        this.messages.put("coin flip", new Message(MessageType.TextMessage, new String[]{munt, munt, munt, munt, munt, kop, kop, kop, kop, kop, kant}, "Returns heads or tails"));


        String allCommands = "**ALL COMMANDS**\n----------------------------------------------------------------------------\n<Activation keyword = \"" + this.PREFIX +"\">\n\n";
        for (String activationString : this.messages.keySet()) {
            allCommands += activationString.toUpperCase() + "   =   " + this.messages.get(activationString).getDescription() + "\n";
        }
        this.messages.put("all commands", new Message(MessageType.TextMessage, allCommands, "Shows all the commands"));

        /** TO DO:
         *  -Easter eggs toevoegen
         *  -Readme.md verbeteren
         *  -Message systeem verbeteren (Message abstract maken en meerdere message klassen deze laten overerven) (messagetype enum verwijderen)
         */
    }

    private void setBlacklistWords() {
        this.blackListedWords = new ArrayList<>();

        this.blackListedWords.add("kanker");
        this.blackListedWords.add("homo");
        this.blackListedWords.add("tyfus");
    }
}
