package tn.esprit.educlass.model;

public class Message extends BaseEntity {

    private long id;
    private int conversationId;
    private int senderId;
    private String content;
    private boolean read;

    // Transient display helper
    private String senderName;

    public Message() {}

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
}
