import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    private int type;
    private String msg;
    private String recipient;

    public ChatMessage(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public ChatMessage(int type, String msg, String recipient) {
        this.type = type;
        this.msg = msg;
        this.recipient = recipient;
    }

    public int getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }


    public String getRecipient() {
        return recipient;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
