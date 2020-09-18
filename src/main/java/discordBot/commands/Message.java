package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.Random;

public abstract class Message {

    private String answerString;
    private String[] answerArray;
    private String description;
    private Random random;

    public Message(String answerString, String description) {
        this.answerString = answerString;
        this.description = description;
        this.random = new Random();
    }

    public Message(String[] answerArray, String description) {
        this.answerArray = answerArray;
        this.description = description;
        this.random = new Random();
    }

    public String getDescription() {
        return description;
    }

    public String getAnswerString() {

        if (this.answerString != null) {
            return this.answerString;
        }

        return this.answerArray[this.random.nextInt(this.answerArray.length)];
    }

    public void sendMessage(TextChannel textChannel, String title, String message) {
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

    abstract boolean doCommand(TextChannel textChannel);
}
