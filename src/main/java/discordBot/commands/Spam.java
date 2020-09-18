package discordBot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Spam extends Message {

    public Spam(String answerString, String description) {
        super(answerString, description);
    }

    public void doCommand(MessageReceivedEvent event, String[] message) {
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
    }
}
