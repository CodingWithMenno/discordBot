package discordBot;

import discordBot.commands.Ping;
import discordBot.commands.Spam;
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

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class MainHandler extends ListenerAdapter {


    /** TO DO:
     *  -Easter eggs toevoegen
     *  -User can make custom commands
     *  -Een game toevoegen (trivia bv)
     *  -Custom status toevoegen voor de bot
     *  -Een line van rik invoegen
     *  -Volume control
     *  -admin commands maken (te doen met Guild class)
     *
     *  -Readme.md verbeteren
     */


    //Discord stuff
    private static final String DISCORDTOKEN = "";
    private static JDA jda;

    public static final String PREFIX = "gamer ";     // Start command voor de bot
    private HashMap<String, Message> messages;

    private APIHandler apiHandler;
    private ArrayList<String> blackListedWords;
    private MusicHandler musicHandler;

    private ArrayList<MusicPlaylist> customPlaylists;


    public static void main(String[] args) {
        try {
            MainHandler mainHandler = new MainHandler();

            jda = JDABuilder.createDefault(DISCORDTOKEN)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .addEventListeners(mainHandler)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                    .build();
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

        System.out.println("discordBot.Message received from: " + event.getAuthor().getName() + " : " + event.getMessage().getContentDisplay());

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

        if (message[0].equals("commands")) {    // Voor het All comannds command
            String command = "commands";
            event.getTextChannel().sendMessage(this.messages.get(command).getAnswerString()).queue();
            return;
        }

        if (message[0].equals("spam")) {    // Voor het spammen van users

            boolean hasPermission = false;
            List<Role> roles = event.getMember().getRoles();
            for (Role role : roles) {
                if (role.getName().equals("Spammer")) {
                    hasPermission = true;
                }
            }

            if (!hasPermission) {
                sendMessage(event.getTextChannel(), "Error", "Sorry, you can only use this command when having the role \"Spammer\"");
                return;
            }

            String unluckyPerson = message[1];
            List<Member> members = event.getGuild().getMembersByName(unluckyPerson, true);

            for (Member member : members) {
                for (int i = 0; i < 10; i++) {
                    event.getTextChannel().sendMessage("<@" + member.getUser().getId() + ">").queue();
                }
                return;
            }

            sendMessage(event.getTextChannel(), "Error", "Person not found");

            return;
        }

        if (message[0].equals("jeff")) {    // Voor jeff command
            this.musicHandler.emptyQeue(event.getTextChannel());
            this.musicHandler.skip(event.getTextChannel());
            this.musicHandler.loadAndPlay(event.getTextChannel(), "https://www.youtube.com/watch?v=_nce9A5S5uM");
            try {
                Thread.sleep(2800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.musicHandler.leaveChannel(event.getTextChannel());
            return;
        }

        if (message[0].equals("suggestion")) {    // Voor nieuwe command ideeën
            String suggestion = "";
            for (int i = 1; i < message.length; i++) {
                suggestion += message[i] + " ";
            }

            String filteredMessage = filterMessage(suggestion);
            saveTextToFile("src/main/resources/CommandSuggestions.txt", event.getAuthor().getName() + " : " + filteredMessage);
            sendMessage(event.getTextChannel(), "", "Thanks for the suggestion :)");
            return;
        }

        if (message[0].equals("image")) {        // Voor random images
            String[] meme = this.apiHandler.getRandomImage();
            if (meme == null) {
                sendMessage(event.getTextChannel(), "Error", "Random image not found");
                return;
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(new Color(50,205,50));
            embed.setTitle(meme[0], meme[1]);
            embed.setImage(meme[2]);
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }

        if (message[0].equals("temp")) {       // Voor de temperatuur berichten
            String stad = message[1];
            String goodCity = stad.substring(0, 1).toUpperCase() + stad.substring(1);
            sendMessage(event.getTextChannel(), goodCity, this.apiHandler.getWeatherFrom(goodCity));
            return;
        }

        for (String key : this.messages.keySet()) {     // Voor alle niet-speciale berichten
            if (message[0].equals(key)) {
                sendMessage(event.getTextChannel(), "", this.messages.get(key).getAnswerString());
                return;
            }
        }
        sendMessage(event.getTextChannel(), "Error", "I did not recognise this command, type **\"gamer commands\"** to see all my commands");
    }

    private boolean handleMusicCommands(MessageReceivedEvent event, String[] message) {
        if (message[0].equals("m")) {   // Voor muziek commands
            if (message[1].equals("play")) {
                this.musicHandler.loadAndPlay(event.getTextChannel(), message[2]);
            } else if (message[1].equals("skip")) {
                this.musicHandler.skipTrack(event.getTextChannel());
            } else if (message[1].equals("leave")) {
                this.musicHandler.emptyQeue(event.getTextChannel());
                this.musicHandler.skip(event.getTextChannel());
                this.musicHandler.leaveChannel(event.getTextChannel());
            }

            else if (message[1].equals("p")) {   // Voor de custom playlists
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
                    sendMessage(event.getTextChannel(), "", "A custom playlist has been created for: " + username);
                }

                else if (message[2].equals("play")) {

                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        sendNoPlaylist(event);
                        return true;
                    }

                    for (String url : userPlaylist.getVideoURLs()) {
                        this.musicHandler.loadAndPlay(event.getTextChannel(), url);
                    }
                }

                else if (message[2].equals("shuffle")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        sendNoPlaylist(event);
                        return true;
                    }

                    ArrayList<String> shuffledList = userPlaylist.getVideoURLs();
                    Collections.shuffle(shuffledList);
                    for (String url : shuffledList) {
                        this.musicHandler.loadAndPlay(event.getTextChannel(), url);
                    }
                }

                else if (message[2].equals("add")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        sendNoPlaylist(event);
                        return true;
                    }

                    boolean added = userPlaylist.addURL(message[3]);

                    if (added) {
                        sendMessage(event.getTextChannel(), "", "Added a new song to your playlist");
                        saveCustomPlaylists();
                    } else {
                        sendMessage(event.getTextChannel(), "Error", "You already have this song in your playlist");
                    }
                }

                else if (message[2].equals("remove")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        sendNoPlaylist(event);
                        return true;
                    }

                    boolean removed = userPlaylist.removeURL(message[3]);

                    if (removed) {
                        sendMessage(event.getTextChannel(), "", "The song has been removed from your playlist");
                        saveCustomPlaylists();
                    } else {
                        sendMessage(event.getTextChannel(), "Error", "You already did not have this song in your playlist");
                    }
                }

                else if (message[2].equals("delete")) {
                    MusicPlaylist playlist = isInPlaylists(username);

                    if (playlist == null) {
                        sendMessage(event.getTextChannel(), "Error", "You did not have a playlist you dumb dumb");
                        return true;
                    } else {
                        this.customPlaylists.remove(playlist);
                        sendMessage(event.getTextChannel(), "", "Successfully removed your custom playlist");
                    }
                }

                else if (message[2].equals("show")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        sendNoPlaylist(event);
                        return true;
                    }

                    sendMessage(event.getTextChannel(), "", "Video's in your playlist:");
                    for (String playlist : userPlaylist.getVideoURLs()) {
                        event.getChannel().sendMessage(playlist).queue();
                    }
                } else {
                    sendMessage(event.getTextChannel(), "Error", "I did not recognise this command, type **\"gamer commands\"** to see all my commands");
                }

            } else {
                sendMessage(event.getTextChannel(), "Error", "I did not recognise this command, type **\"gamer commands\"** to see all my commands");
            }

            return true;
        }
        return false;
    }

    private void sendNoPlaylist(MessageReceivedEvent event) {
        sendMessage(event.getTextChannel(), "Error", "You have no custom playlist yet, make one with the command: **" + this.PREFIX + "m playlist create <song URL1> <song URL2> ...**");
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

        this.messages.put("temp <city>", new Message("In <city> is het <celsius> graden", "Shows the temperature of the city (only in the Netherlands) by typing a city after the command"));

        this.messages.put("image", new Message("<ImageURL>", "Returns a random image from the subreddet r/funny"));

        this.messages.put("suggestion <suggestion>", new Message("Thanks for the suggestion :)", "Type a new command suggestion after this command and maybe it will be implemented"));

        this.messages.put("spam <name>", new Message("@<name> @<name> @<name> ...", "This command will spam a person so he/she will not ignore you anymore :) (Only for \"Spammers\")"));

        this.messages.put("jeff", new Message("My name is Jeff", "My name is Jeff (Only works when in voice channel)"));

        this.messages.put("m play <URL>", new Message("*Plays song*", "Type a youtube url after this command to play this video"));

        this.messages.put("m skip", new Message("*Skips a song*", "Skips the current playing video and plays the next one"));

        this.messages.put("m leave", new Message("*Leaves channel", "The bot will leave the channel its in"));

        this.messages.put("m p create <URL1> ...", new Message("Custom playlist created", "Creates a custom playlist saves it with the users name"));

        this.messages.put("m p delete", new Message("Deleted your custom playlist", "Deletes your custom playlist"));

        this.messages.put("m p play", new Message("Playing your custom playlist", "This command will play your custom saved playlist"));

        this.messages.put("m p shuffle", new Message("Shuffles and plays your custom playlist", "This command will shuffle and play your custom playlist"));

        this.messages.put("m p add <URL>", new Message("New song added to your custom playlist", "Adds the url behind the command to your playlist"));

        this.messages.put("m p remove <URL>", new Message("Removed a url from your custom playlist", "Removes a url from your custom playlist"));

        this.messages.put("m p show", new Message("*Shows all songs in your custom playlist*", "Shows all songs in your custom playlist"));

        String allCommands = "```**ALL COMMANDS**\n----------------------------------------------------------------------------\n<Activation keyword = \"" + this.PREFIX +"\">\n\n";
        for (String activationString : this.messages.keySet()) {
            allCommands += activationString.toUpperCase();
            int totalSpaces = 32 - activationString.length();
            for (int i = 0; i < totalSpaces; i++) {
                allCommands += " ";
            }
            allCommands += "=   " + this.messages.get(activationString).getDescription() + "\n";
        }
        allCommands += "```";
        this.messages.put("commands", new Message(allCommands, "Shows all the commands"));
    }

    private synchronized void saveCustomPlaylists() {
        ObjectOutputStream outputStream = null;

        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(new File("src/main/resources/CustomPlaylists.txt")));

            outputStream.writeObject(this.customPlaylists);

            System.out.println("Saved the custom playlists");

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
            inputStream = new ObjectInputStream(new FileInputStream(new File("src/main/resources/CustomPlaylists.txt")));

            this.customPlaylists = (ArrayList<MusicPlaylist>) inputStream.readObject();

            if (this.customPlaylists == null) {
                this.customPlaylists = new ArrayList<>();
            }

        } catch (Exception e) {
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
