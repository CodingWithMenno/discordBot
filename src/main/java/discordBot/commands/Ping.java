package commands;

import net.dv8tion.jda.api.entities.TextChannel;

public class Ping extends Message {

    public Ping(String[] answerArray, String description) {
        super(answerArray, description);
    }

    @Override
    boolean doCommand(TextChannel textChannel) {
        sendMessage(textChannel, "", getAnswerString());
        return true;
    }
}
