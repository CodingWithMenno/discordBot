package commands;

import net.dv8tion.jda.api.entities.TextChannel;

public class Coinflip extends Message {

    public Coinflip(String[] answerArray, String description) {
        super(answerArray, description);
    }

    @Override
    public boolean doCommand(TextChannel textChannel) {
        sendMessage(textChannel, "", getAnswerString());
        return true;
    }
}
