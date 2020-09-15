import music.MusicHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.*;

public class MainHandler extends ListenerAdapter {


    /** TO DO:
     *  -Easter eggs toevoegen
     *  -Readme.md verbeteren
     *  -User can make custom commands
     *  -All commands command mooier maken
     *  -Een game toevoegen (trivia bv)
     */


    //Discord stuff
    private static final String DISCORDTOKEN = "";
    private static JDA jda;

    private final String PREFIX = "gamer ";     // Start command voor de bot
    private HashMap<String, Message> messages;

    private APIHandler apiHandler;
    private ArrayList<String> blackListedWords;
    private MusicHandler musicHandler;

    private ArrayList<MusicPlaylist> customPlaylists;


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
        initCustomPlaylists();
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

        String[] message = event.getMessage().getContentDisplay().substring(this.PREFIX.length()).split(" ");

        handleMessage(event, message);
    }

    private void handleMessage(MessageReceivedEvent event, String[] message) {
        boolean handled = handleMusicCommands(event, message);    // Voor alle muziek commands
        if (handled) {
            return;
        }

        if (message[0].equals("suggestion")) {    // Voor nieuwe command ideeën
            String filteredMessage = filterMessage(message[1]);
            saveTextToFile("src/main/resources/CommandSuggestions.txt", event.getAuthor().getName() + " : " + filteredMessage);
        }

        if (message[0].equals("image")) {        // Voor random images
            String memeURL = this.apiHandler.getRandomImage();
            event.getChannel().sendMessage(memeURL).queue();
            return;
        }

        if (message[0].equals("temp")) {       // Voor de temperatuur berichten
            String stad = message[1];
            String goodCity = stad.substring(0, 1).toUpperCase() + stad.substring(1);
            event.getChannel().sendMessage(this.apiHandler.getWeatherFrom(goodCity)).queue();
            return;
        }

        for (String key : this.messages.keySet()) {     // Voor alle niet-speciale berichten
            if (message[0].equals(key)) {
                event.getChannel().sendMessage(this.messages.get(key).getAnswerString()).queue();
                return;
            }
        }
    }

    private boolean handleMusicCommands(MessageReceivedEvent event, String[] message) {
        if (message[0].equals("m")) {   // Voor muziek commands
            if (message[1].equals("play")) {
                this.musicHandler.loadAndPlay(event.getTextChannel(), message[2]);
            } else if (message[1].equals("skip")) {
                this.musicHandler.skipTrack(event.getTextChannel());
            } else if (message[1].equals("leave")) {
                this.musicHandler.leaveChannel(event.getTextChannel());
            }

            else if (message[1].equals("playlist")) {   // Voor de custom playlists
                String username = event.getAuthor().getName();

                if (message[2].equals("create")) {
                    MusicPlaylist oldPlaylist = isInPlaylists(username);

                    if (oldPlaylist != null) {
                        this.customPlaylists.remove(oldPlaylist);
                    }

                    ArrayList<String> videoURLs = new ArrayList<>();
                    for (int i = 3; i < message.length; i++) {
                        videoURLs.add(message[i]);
                    }

                    this.customPlaylists.add(new MusicPlaylist(username, videoURLs));
                    saveCustomPlaylists();
                    event.getChannel().sendMessage("A custom playlist had been created for: " + username).queue();
                }

                else if (message[2].equals("play")) {

                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        event.getChannel().sendMessage("You have no custom playlist yet, make one with the command: " + this.PREFIX + "m playlist create <song URL1> <song URL2> ...").queue();
                        return true;
                    }

                    for (String url : userPlaylist.getVideoURLs()) {
                        this.musicHandler.loadAndPlay(event.getTextChannel(), url);
                    }
                }

                else if (message[2].equals("add")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        event.getChannel().sendMessage("You have no custom playlist yet, make one with the command: " + this.PREFIX + "m playlist create <song URL1> <song URL2> ...").queue();
                        return true;
                    }

                    boolean added = userPlaylist.addURL(message[3]);

                    if (added) {
                        event.getChannel().sendMessage("Added a new song to your playlist").queue();
                        saveCustomPlaylists();
                    } else {
                        event.getChannel().sendMessage("You already have this song in your playlist").queue();
                    }
                }

                else if (message[2].equals("remove")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        event.getChannel().sendMessage("You have no custom playlist yet, make one with the command: " + this.PREFIX + "m playlist create <song URL1> <song URL2> ...").queue();
                        return true;
                    }

                    boolean removed = userPlaylist.removeURL(message[3]);

                    if (removed) {
                        event.getChannel().sendMessage("Removed the song from your playlist").queue();
                        saveCustomPlaylists();
                    } else {
                        event.getChannel().sendMessage("You did not have this song in your playlist").queue();
                    }
                }

                else if (message[2].equals("delete")) {
                    MusicPlaylist playlist = isInPlaylists(username);

                    if (playlist == null) {
                        event.getChannel().sendMessage("You did not have a playlist you dumb dumb").queue();
                        return true;
                    } else {
                        this.customPlaylists.remove(playlist);
                        event.getChannel().sendMessage("Successfully removed your custom playlist").queue();
                    }
                }

                else if (message[2].equals("show")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        event.getChannel().sendMessage("You have no custom playlist yet, make one with the command: " + this.PREFIX + "m playlist create <song URL1> <song URL2> ...").queue();
                        return true;
                    }

                    event.getChannel().sendMessage("*Video's in your playlist:*").queue();
                    for (String playlist : userPlaylist.getVideoURLs()) {
                        event.getChannel().sendMessage(playlist).queue();
                    }
                }

            }

            return true;
        }
        return false;
    }

    private MusicPlaylist isInPlaylists(String username) {
        MusicPlaylist customPlaylist = null;

        if (this.customPlaylists.isEmpty()) {
            return null;
        }

        for (MusicPlaylist playlist : this.customPlaylists) {
            if (playlist.getUserName().equals(username)) {
                customPlaylist = playlist;
            }
        }
        return customPlaylist;
    }

    private String filterMessage(String message) {
        for (String blackListWord : this.blackListedWords) {
            if (message.contains(blackListWord)) {
                return "BLOCKED MESSAGE";
            }
        }
        return message;
    }

    private void saveTextToFile(String fileName, String text) {

        PrintWriter printWriter = null;
        try {

            printWriter = new PrintWriter(new FileWriter(fileName, true));

            printWriter.append(text + "\n");

            System.out.println("Wrote something to: " + fileName);

        } catch (IOException e) {
            System.out.println("Something went wrong while writing to the file: " + fileName);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    private void setMessages() {
        this.messages = new LinkedHashMap<>();        // Alle mogelijke commands

        String munt = "https://www.budgetgift.nl/604/0/0/1/ffffff00/441842e6/a19c84e94684a0c00a7658d0be40c75b26e02e700df22c7459be5c4cfcd6438b/1-euro-munt.png";
        String kop = "https://external-preview.redd.it/hA47uRmiVowkZy1_3435QpN0h82gh2cLdXdy-Bc5-7Y.gif?format=png8&s=9841751c47a1e8a24b92974a70a1ba7354db789a";
        String kant = "https://upload.wikimedia.org/wikipedia/commons/6/67/1_oz_Vienna_Philharmonic_2017_edge.png";
        this.messages.put("coinflip", new Message(new String[]{munt, munt, munt, munt, munt, munt, munt, munt, kop, kop, kop, kop, kop, kop, kop, kop, kant}, "Returns heads or tails"));

        this.messages.put("ping", new Message(new String[]{"No :(", "Pong!", "Oke boomer"}, "Pong"));

        this.messages.put("temp", new Message("In <city> is het <celsius> graden", "Returns the temperature of the chosen city (only in the Netherlands) by typing a city after the command"));

        this.messages.put("image", new Message("<ImageURL>", "Returns a random image from the subreddet r/funny"));

        this.messages.put("suggestion", new Message("Thanks for the suggestion :)", "Type a new command suggestion after this command and maybe it will be implemented"));

        this.messages.put("m play", new Message("*Plays song*", "Type a youtube url after this command to play this video"));

        this.messages.put("m skip", new Message("*Skips a song*", "Skips the current playing video and plays the next one"));

        this.messages.put("m leave", new Message("*Leaves channel", "The bot will leave the channel its in"));

        this.messages.put("m playlist create", new Message("Custom playlist created", "Creates a custom playlist saves it with the users name"));

        this.messages.put("m playlist delete", new Message("Deleted your custom playlist", "Deletes your custom playlist"));

        this.messages.put("m playlist play", new Message("Playing your custom playlist", "This command will play your custom saved playlist"));

        this.messages.put("m playlist add", new Message("New song added to your custom playlist", "Adds the url behind the command to your playlist"));

        this.messages.put("m playlist remove", new Message("Removed a url from your custom playlist", "Removes a url from your custom playlist"));

        this.messages.put("m playlist show", new Message("*Shows all songs in your custom playlist*", "Shows all songs in your custom playlist"));

        String allCommands = "**ALL COMMANDS**\n----------------------------------------------------------------------------\n<Activation keyword = \"" + this.PREFIX +"\">\n\n";
        for (String activationString : this.messages.keySet()) {
            allCommands += activationString.toUpperCase() + "   =   " + this.messages.get(activationString).getDescription() + "\n";
        }
        this.messages.put("allcommands", new Message(allCommands, "Shows all the commands"));
    }

    private synchronized void saveCustomPlaylists() {
        ObjectOutputStream outputStream = null;

        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(new File("src/main/resources/CustomPlaylists")));

            outputStream.writeObject(this.customPlaylists);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initCustomPlaylists() {
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(new File("src/main/resources/CustomPlaylists")));

            this.customPlaylists = (ArrayList<MusicPlaylist>) inputStream.readObject();

            if (this.customPlaylists == null) {
                this.customPlaylists = new ArrayList<>();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Something went wrong while reading the custom playlists");
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
