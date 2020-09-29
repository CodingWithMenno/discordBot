package discordBot;

import discordBot.commands.*;
import discordBot.commands.Image;
import discordBot.music.MusicHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class MainHandler extends ListenerAdapter {


    /** TO DO:
     *  -Een game toevoegen (trivia bv)
     *  -Custom status toevoegen voor de bot
     *  -Pauze command voor muziek
     *  -Vertellen wat het nieuwe lied is wanneer een nieuw lied start
     *  -admin commands maken (te doen met Guild class)
     *
     *  -AI learning toepassen: https://github.com/gunthercox/ChatterBot
     *  -Toevoegen aan de bot dat je alleen aan de bot mag zitten als je in hetzelfde kanaal als be bot zit
     *  -Readme.md verbeteren
     */


    // Discord stuff
    private static final String DISCORDTOKEN = "";
    private static JDA jda;

    public static final String PREFIX = "gamer ";     // Start command voor de bot
    private HashMap<String, String> messages;

    private APIHandler apiHandler;
    private ArrayList<String> blackListedWords;
    private MusicHandler musicHandler;


    // De connecties naar elk command
    private Ping pingCommand;
    private Coinflip coinflipCommand;
    private Image imageCommand;
    private Jeff jeffCommand;
    private Music musicCommands;
    private Spam spamCommand;
    private Suggestion suggestionCommand;
    private Temperature temperatureCommand;


    public static void main(String[] args) {
        try {
            MainHandler mainHandler = new MainHandler();

            jda = JDABuilder.createDefault(DISCORDTOKEN)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .addEventListeners(mainHandler)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                    .setActivity(Activity.watching("hentai"))
                    .build();
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

    private void sendMessage(TextChannel textChannel, String title, String message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(50,205,50));

        if (title.isEmpty()) {
            if (message.startsWith("https")) {
                embed.setImage(message);
            } else {
                embed.setDescription(message);
            }
            textChannel.sendMessage(embed.build()).queue();
            return;
        }

        if (message.startsWith("https")) {
            embed.setImage(message);
        } else {
            embed.setDescription(message);
        }

        embed.setTitle(title);
        textChannel.sendMessage(embed.build()).queue();
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

        if (!prefixMessage.startsWith(PREFIX)) {
            return;
        }

        String[] message = event.getMessage().getContentDisplay().substring(PREFIX.length()).split(" ");

        handleMessage(event, message);
    }

    private void handleMessage(MessageReceivedEvent event, String[] message) {
        String commando = message[0].toLowerCase();

        switch (commando) {
            case "commands":    String command = "commands";
                                event.getTextChannel().sendMessage(this.messages.get(command)).queue();
                                return;
            case "ping":        this.pingCommand.doCommand(event.getTextChannel()); return;
            case "coinflip":    this.coinflipCommand.doCommand(event.getTextChannel()); return;
            case "image":       this.imageCommand.doCommand(event.getTextChannel(), this.apiHandler); return;
            case "jeff":        this.jeffCommand.doCommand(event, this.musicHandler); return;
            case "m":           this.musicCommands.doCommand(event, message, this.musicHandler); return;
            case "spam":        this.spamCommand.doCommand(event, message); return;
            case "suggestion":  this.suggestionCommand.doCommand(event, message, this.blackListedWords); return;
            case "temp":        this.temperatureCommand.doCommand(event.getTextChannel(), message, this.apiHandler); return;
        }

        sendMessage(event.getTextChannel(), "Error", "I did not recognise this command, type **\"" + PREFIX + " commands\"** to see all my commands");
    }

    private void setMessages() {
        String munt = "https://www.budgetgift.nl/604/0/0/1/ffffff00/441842e6/a19c84e94684a0c00a7658d0be40c75b26e02e700df22c7459be5c4cfcd6438b/1-euro-munt.png";
        String kop = "https://external-preview.redd.it/hA47uRmiVowkZy1_3435QpN0h82gh2cLdXdy-Bc5-7Y.gif?format=png8&s=9841751c47a1e8a24b92974a70a1ba7354db789a";
        String kant = "https://upload.wikimedia.org/wikipedia/commons/6/67/1_oz_Vienna_Philharmonic_2017_edge.png";


        // Connectie naar alle commands
        this.pingCommand = new Ping(new String[]{"No :(", "Pong!", "Oke boomer"}, "Pong");
        this.coinflipCommand = new Coinflip(new String[]{munt, munt, munt, munt, munt, munt, munt, munt, kop, kop, kop, kop, kop, kop, kop, kop, kant}, "Returns heads or tails");
        this.imageCommand = new Image("<ImageURL>", "Returns a random image from the subreddet r/funny");
        this.jeffCommand = new Jeff("My name is Jeff", "My name is Jeff (Only works when in voice channel)");
        this.musicCommands = new Music("Returns a music answer", "All the commands for the the music");
        this.spamCommand = new Spam("@<name> @<name> @<name> ...", "This command will spam a person so he/she will not ignore you anymore :) (Only for \"Spammers\")");
        this.suggestionCommand = new Suggestion("Thanks for the suggestion :)", "Type a new command suggestion after this command and maybe it will be implemented");
        this.temperatureCommand = new Temperature("In <city> is het <celsius> graden", "Shows the temperature of the city (only in the Netherlands) by typing a city after the command");


        // Voor all commands command
        this.messages = new LinkedHashMap<>();


        this.messages.put("coinflip", "Returns heads or tails");

        this.messages.put("ping", "Pong");

        this.messages.put("temp <city>", "Shows the temperature of the city (only in the Netherlands) by typing a city after the command");

        this.messages.put("image", "Returns a random image from the subreddet r/funny");

        this.messages.put("suggestion <suggestion>", "Type a new command suggestion after this command and maybe it will be implemented");

        this.messages.put("spam <name>", "This command will spam a person so he/she will not ignore you anymore :) (Only for \"Spammers\")");

        this.messages.put("jeff", "My name is Jeff (Only works when in voice channel)");

        this.messages.put("m play <URL>", "Type a youtube url after this command to play this video");

        this.messages.put("m current", "Shows the current playing song");

        this.messages.put("m skip", "Skips the current playing video and plays the next one");

        this.messages.put("m vol <number>", "Sets the volume of the bot between 0-1000");

        this.messages.put("m loop <true/false>", "Will set the looping mode of the bot to true or false");

        this.messages.put("m leave", "The bot will leave the channel its in");

        this.messages.put("m p create <URL1> <URL2> ...", "Creates a custom playlist and saves it with the user's name");

        this.messages.put("m p delete", "Deletes your custom playlist");

        this.messages.put("m p play", "This command will play your custom saved playlist");

        this.messages.put("m p shuffle", "This command will shuffle and play your custom playlist");

        this.messages.put("m p add <URL>", "Adds the url behind the command to your custom playlist");

        this.messages.put("m p remove <URL>", "Removes a url from your custom playlist");

        this.messages.put("m p show", "Shows all songs in your custom playlist");

        String allCommands = "```ALL COMMANDS\n----------------------------------------------------------------------------\n<Activation keyword = \"" + PREFIX +"\">\n\n";
        for (String activationString : this.messages.keySet()) {
            allCommands += activationString.toUpperCase();
            int totalSpaces = 32 - activationString.length();
            for (int i = 0; i < totalSpaces; i++) {
                allCommands += " ";
            }
            allCommands += "=   " + this.messages.get(activationString) + "\n";
        }
        allCommands += "\n----------------------------------------------------------------------------\nMade by: Menno Bil```";
        this.messages.put("commands", allCommands);
    }

    private void setBlacklistWords() {
        this.blackListedWords = new ArrayList<>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileReader("src/main/resources/BlackListedWords.txt"));

            while (scanner.hasNextLine()) {
                this.blackListedWords.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Something went wrong while reading the blacklist file");
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}
