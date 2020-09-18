package commands;

import net.dv8tion.jda.api.entities.TextChannel;

public class Temperature extends Message {

    public Temperature(String answerString, String description) {
        super(answerString, description);
    }

    @Override
    boolean doCommand(TextChannel textChannel) {
        return false;
    }
}
