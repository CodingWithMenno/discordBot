package discordBot;

import java.util.Random;

public class Message {

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
}
