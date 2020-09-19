package discordBot.commands;

import discordBot.MainHandler;
import discordBot.MusicPlaylist;
import discordBot.music.MusicHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class Music extends Message {

    private ArrayList<MusicPlaylist> customPlaylists;

    public Music(String answerString, String description) {
        super(answerString, description);
        initCustomPlaylists();
    }

    private void sendNoPlaylist(MessageReceivedEvent event) {
        sendMessage(event.getTextChannel(), "Error", "You have no custom playlist yet, make one with the command: **" + MainHandler.PREFIX + "m playlist create <song URL1> <song URL2> ...**");
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

    public void doCommand(MessageReceivedEvent event, String[] message, MusicHandler musicHandler) {
        if (message[0].equals("m")) {   // Voor muziek commands
            if (message[1].equals("play")) {
                if (!message[2].startsWith("https")) {
                    sendMessage(event.getTextChannel(), "Error", "This is not a URL");
                    return;
                }

                musicHandler.loadAndPlay(event.getTextChannel(), message[2], event.getMember().getUser().getName());
            } else if (message[1].equals("skip")) {
                musicHandler.skipTrack(event.getTextChannel());
            } else if (message[1].equals("leave")) {
                musicHandler.emptyQeue(event.getTextChannel());
                musicHandler.skip(event.getTextChannel());
                musicHandler.leaveChannel(event.getTextChannel());
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
                        if (message[i].startsWith("https") && !videoURLs.contains(message[i])) {
                            videoURLs.add(message[i]);
                        }
                    }

                    this.customPlaylists.add(new MusicPlaylist(username, videoURLs));
                    saveCustomPlaylists();
                    sendMessage(event.getTextChannel(), "", "A custom playlist has been created for: " + username);
                }

                else if (message[2].equals("play")) {

                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        sendNoPlaylist(event);
                        return;
                    }

                    for (String url : userPlaylist.getVideoURLs()) {
                        musicHandler.loadAndPlay(event.getTextChannel(), url, event.getMember().getUser().getName());
                    }
                }

                else if (message[2].equals("shuffle")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        sendNoPlaylist(event);
                        return;
                    }

                    ArrayList<String> shuffledList = userPlaylist.getVideoURLs();
                    Collections.shuffle(shuffledList);
                    for (String url : shuffledList) {
                        musicHandler.loadAndPlay(event.getTextChannel(), url, event.getMember().getUser().getName());
                    }
                }

                else if (message[2].equals("add")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        sendNoPlaylist(event);
                        return;
                    }

                    if (!message[3].startsWith("https")) {
                        sendMessage(event.getTextChannel(), "Error", "This is not a URL");
                        return;
                    }

                    boolean added = false;
                    try {
                        added = userPlaylist.addURL(message[3]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        sendMessage(event.getTextChannel(), "Error", "No song included in command");
                        return;
                    }


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
                        return;
                    }

                    boolean removed = false;
                    try {
                        removed = userPlaylist.removeURL(message[3]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        sendMessage(event.getTextChannel(), "Error", "No song included in command");
                        return;
                    }

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
                        return;
                    } else {
                        this.customPlaylists.remove(playlist);
                        sendMessage(event.getTextChannel(), "", "Successfully removed your custom playlist");
                    }
                }

                else if (message[2].equals("show")) {
                    MusicPlaylist userPlaylist = isInPlaylists(username);

                    if (userPlaylist == null) {
                        sendNoPlaylist(event);
                        return;
                    }

                    sendMessage(event.getTextChannel(), "", "Video's in your playlist:");
                    for (String playlist : userPlaylist.getVideoURLs()) {
                        event.getChannel().sendMessage(playlist).queue();
                    }
                } else {
                    sendMessage(event.getTextChannel(), "Error", "I did not recognise this command, type **\"" + MainHandler.PREFIX + " commands\"** to see all my commands");
                }

            } else {
                sendMessage(event.getTextChannel(), "Error", "I did not recognise this command, type **\"" + MainHandler.PREFIX + " commands\"** to see all my commands");
            }
        }
    }
}
