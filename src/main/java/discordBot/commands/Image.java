package discordBot.commands;

import discordBot.APIHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Image extends Message {

    private APIHandler apiHandler;

    public Image(String answerString, String description) {
        super(answerString, description);
        this.apiHandler = new APIHandler();
    }

    public void doCommand(TextChannel textChannel) {
        String[] meme = this.apiHandler.getRandomImage();

        if (meme == null) {
            sendMessage(textChannel, "Error", "Random image not found");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(50,205,50));
        embed.setTitle(meme[0], meme[1]);
        embed.setImage(meme[2]);
        textChannel.sendMessage(embed.build()).queue();
    }
}
