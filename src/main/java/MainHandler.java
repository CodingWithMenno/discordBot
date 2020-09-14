import music.MusicHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MainHandler extends ListenerAdapter {


    /** TO DO:
     *  -Easter eggs toevoegen
     *  -Readme.md verbeteren
     *  -User can make custom commands
     *  -All commands command mooier maken
     */


    //Discord stuff
    private static final String DISCORDTOKEN = "";
    private static JDA jda;

    private final String PREFIX = "gamer ";     // Start command voor de bot
    private HashMap<String, Message> messages;
    private APIHandler apiHandler;
    private ArrayList<String> blackListedWords;
    private MusicHandler musicHandler;


    public static void main(String[] args) {
        try {
            jda = JDABuilder.createDefault(DISCORDTOKEN).build();
            MainHandler mainHandler = new MainHandler();
            jda.addEventListener(mainHandler);
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public MainHandler() {
        setMessages();
        setBlacklistWords();
        this.apiHandler = new APIHandler();
        this.musicHandler = new MusicHandler();
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

        String message = event.getMessage().getContentDisplay().substring(this.PREFIX.length());

        handleMessage(event, message);
    }

    private void handleMessage(MessageReceivedEvent event, String message) {

        if (message.contains("m ")) {   // Voor muziek commands
            if (message.contains("play ")) {
                this.musicHandler.loadAndPlay(event.getTextChannel(), message.substring(7));
            } else if (message.contains("skip")) {
                this.musicHandler.skipTrack(event.getTextChannel());
            } else if (message.contains("leave")) {
                this.musicHandler.leaveChannel(event.getTextChannel().getGuild().getAudioManager());
            }
            return;
        }

        if (message.contains("suggestion ")) {    // Voor nieuwe command ideeën
            String filteredMessage = filterMessage(message.substring(11));
            saveToFile("src/main/resources/CommandSuggestions.txt", event.getAuthor().getName() + " : " + filteredMessage);
        }

        if (message.contains("image ")) {        // Voor random images
            String id = message.substring(6);
            String memeURL = this.apiHandler.getRandomImage(id);
            event.getChannel().sendMessage(memeURL).queue();
            return;
        }

        if (message.contains("temp ")) {       // Voor de temperatuur berichten
            String stad = message.substring(5);
            String goodCity = stad.substring(0, 1).toUpperCase() + stad.substring(1);
            event.getChannel().sendMessage(this.apiHandler.getWeatherFrom(goodCity)).queue();
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

            PrintWriter printWriter = new PrintWriter(new FileWriter(fileName, true));

            printWriter.append(text + "\n");
            printWriter.close();

            System.out.println("Wrote something to: " + fileName);

        } catch (IOException e) {
            System.out.println("Something went wrong while writing to the file: " + fileName);
        }
    }

    private void setMessages() {
        this.messages = new HashMap<>();        // Alle mogelijke commands


        this.messages.put("ping", new Message(new String[]{"No :(", "Pong!", "Oke boomer"}, "Pong"));

        this.messages.put("temp ", new Message("In <city> is het <celsius> graden", "Returns the temperature of the chosen city (only in the Netherlands) by typing a city after the command"));

        this.messages.put("image ", new Message("<ImageURL>", "Returns a chosen/random image by typing a number or \"random\" behind the command"));

        this.messages.put("suggestion ", new Message("Thanks for the suggestion :)", "Type a new command suggestion after this command and maybe it will be implemented"));

        this.messages.put("m play ", new Message("*Plays song*", "Type a youtube url after this command to play this video"));

        this.messages.put("m skip", new Message("*Skips a song*", "Skips the current playing video and plays the next one"));

        this.messages.put("m leave", new Message("*Leaves channel", "The bot will leave the channel its in"));

        String munt = "https://www.budgetgift.nl/604/0/0/1/ffffff00/441842e6/a19c84e94684a0c00a7658d0be40c75b26e02e700df22c7459be5c4cfcd6438b/1-euro-munt.png";
        String kop = "https://external-preview.redd.it/hA47uRmiVowkZy1_3435QpN0h82gh2cLdXdy-Bc5-7Y.gif?format=png8&s=9841751c47a1e8a24b92974a70a1ba7354db789a";
        String kant = "https://upload.wikimedia.org/wikipedia/commons/6/67/1_oz_Vienna_Philharmonic_2017_edge.png";
        this.messages.put("coin flip", new Message(new String[]{munt, munt, munt, munt, munt, munt, munt, munt, kop, kop, kop, kop, kop, kop, kop, kop, kant}, "Returns heads or tails"));


        String allCommands = "**ALL COMMANDS**\n----------------------------------------------------------------------------\n<Activation keyword = \"" + this.PREFIX +"\">\n\n";
        for (String activationString : this.messages.keySet()) {
            allCommands += activationString.toUpperCase() + "   =   " + this.messages.get(activationString).getDescription() + "\n";
        }
        this.messages.put("all commands", new Message(allCommands, "Shows all the commands"));
    }

    private void setBlacklistWords() {
        this.blackListedWords = new ArrayList<>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileReader("src/main/resources/BlackListedWords.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Something went wrong while reading the blacklist file");
        }

        while (scanner.hasNextLine()) {
            this.blackListedWords.add(scanner.nextLine());
        }

        scanner.close();
    }
}
