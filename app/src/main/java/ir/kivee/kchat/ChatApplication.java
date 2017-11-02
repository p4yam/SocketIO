package ir.kivee.kchat;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by payam on 10/20/17.
 */

public class ChatApplication extends Application {
    private Socket mSocket;

    {
        try {
            // آدرس سرور چت که من به صورت لوکال راه اندازیش کردم
            mSocket = IO.socket("http://192.168.1.8");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
