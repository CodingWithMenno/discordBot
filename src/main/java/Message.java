public class Message {

    private MessageType messageType;
    private String activationString;
    private String answerString;

    public Message(MessageType messageType, String activationString, String answerString) {
        this.messageType = messageType;
        this.activationString = activationString;
        this.answerString = answerString;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getActivationString() {
        return activationString;
    }

    public String getAnswerString() {
        return answerString;
    }
}
