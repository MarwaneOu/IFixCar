package ifixcar.crismon.com.ifixcar.Model;

public class Result {

    private String message_id;

    public Result() {
    }

    public Result(String messageId) {
        this.message_id = messageId;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
}
