public class Message {

    private MessageType messageType;
    private String answerString;

    public Message(MessageType messageType, String answerString) {
        this.messageType = messageType;
        this.answerString = answerString;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getAnswerString() {
        return answerString;
    }
}
