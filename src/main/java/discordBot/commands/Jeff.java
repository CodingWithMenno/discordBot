package discordBot.commands;

import discordBot.music.MusicHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Jeff extends Message {

    public Jeff(String answerString, String description) {
        super(answerString, description);
    }

    public void doCommand(MessageReceivedEvent event, MusicHandler musicHandler) {
        musicHandler.emptyQeue(event.getTextChannel());
        musicHandler.skip(event.getTextChannel());
        musicHandler.loadAndPlay(event.getTextChannel(), "https://www.youtube.com/watch?v=_nce9A5S5uM", event.getMember().getUser().getName());
        try {
            Thread.sleep(2800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        musicHandler.leaveChannel(event.getTextChannel());
    }
}
