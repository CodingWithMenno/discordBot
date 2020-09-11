import java.util.Random;

public class Message {

    private MessageType messageType;
    private String answerString;
    private String[] answerArray;
    private String description;
    private Random random;

    public Message(MessageType messageType, String answerString, String description) {
        this.messageType = messageType;
        this.answerString = answerString;
        this.description = description;
        this.random = new Random();
    }

    public Message(MessageType messageType, String[] answerArray, String description) {
        this.messageType = messageType;
        this.answerArray = answerArray;
        this.description = description;
        this.random = new Random();
    }

    public MessageType getMessageType() {
        return this.messageType;
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
