package corechat;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private User sender;
    private User receiver;
    private String messageContent;
    private String timestamp;

    public Message(User sender, User receiver, String messageContent, String timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.messageContent = messageContent;
        this.timestamp = (timestamp == null || timestamp.trim().isEmpty())
                ? LocalTime.now().format(TIME_FORMATTER)
                : timestamp;
    }

    public Message(User sender, User receiver, String messageContent) {
        this(sender, receiver, messageContent, LocalTime.now().format(TIME_FORMATTER));
    }

    public User getSender() {
        return this.sender;
    }

    public User getReceiver() {
        return this.receiver;
    }

    public String getMessage() {
        return this.messageContent;
    }

    public String getTimestamp() {
        return this.timestamp;
    }
}
