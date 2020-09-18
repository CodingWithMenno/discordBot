package discordBot.commands;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Suggestion extends Message {

    public Suggestion(String answerString, String description) {
        super(answerString, description);
    }

    public void doCommand(MessageReceivedEvent event, String[] message, ArrayList<String> blackListedWords) {
        String suggestion = "";
        for (int i = 1; i < message.length; i++) {
            suggestion += message[i] + " ";
        }

        String filteredMessage = filterMessage(suggestion, blackListedWords);
        saveTextToFile("src/main/resources/CommandSuggestions.txt", event.getAuthor().getName() + " : " + filteredMessage);
        sendMessage(event.getTextChannel(), "", "Thanks for the suggestion :)");
    }

    private String filterMessage(String message, ArrayList<String> blackListedWords) {
        for (String blackListWord : blackListedWords) {
            if (message.contains(blackListWord)) {
                return "BLOCKED MESSAGE";
            }
        }
        return message;
    }

    private void saveTextToFile(String fileName, String text) {

        PrintWriter printWriter = null;
        try {

            printWriter = new PrintWriter(new FileWriter(fileName, true));

            printWriter.append(text + "\n");

            System.out.println("Wrote something to: " + fileName);

        } catch (IOException e) {
            System.out.println("Something went wrong while writing to the file: " + fileName);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }
}
