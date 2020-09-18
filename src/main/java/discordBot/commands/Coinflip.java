package discordBot.commands;

import net.dv8tion.jda.api.entities.TextChannel;

public class Coinflip extends Message {

    public Coinflip(String[] answerArray, String description) {
        super(answerArray, description);
    }

    public void doCommand(TextChannel textChannel) {
        sendMessage(textChannel, "", getAnswerString());
    }
}
