package ir.kivee.kchat;

/**
 * Created by payam on 10/30/17.
 */

public class Message {
    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_TYPING = 2;

    private int mType;
    private String mMessage;
    private String mUsername;

    private Message() {
    }

    public int getType() {
        return mType;
    }


    public String getMessage() {
        return mMessage;
    }


    public String getUsername() {
        return mUsername;
    }

    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;

        public Builder(int mType) {
            this.mType = mType;
        }

        public Builder username(String mUsername) {
            this.mUsername = mUsername;
            return this;
        }

        public Builder message(String mMessage) {
            this.mMessage = mMessage;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            return message;
        }
    }
}
