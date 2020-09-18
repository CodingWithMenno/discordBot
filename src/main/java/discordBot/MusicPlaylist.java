package discordBot;

import java.io.Serializable;
import java.util.ArrayList;

public class MusicPlaylist implements Serializable {

    private String userName;
    private ArrayList<String> videoURLs;

    public MusicPlaylist(String userName, ArrayList<String> videoURLs) {
        this.userName = userName;
        this.videoURLs = videoURLs;
    }

    public boolean addURL(String url) {
        if (!this.videoURLs.contains(url)) {
            this.videoURLs.add(url);
            return true;
        }
        return false;
    }

    public boolean removeURL(String url) {
        if (this.videoURLs.contains(url)) {
            this.videoURLs.remove(url);
            return true;
        }
        return false;
    }

    public String getUserName() {
        return userName;
    }

    public ArrayList<String> getVideoURLs() {
        return videoURLs;
    }
}
