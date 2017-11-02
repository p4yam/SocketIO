package ir.kivee.kchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoginActivity extends AppCompatActivity {

    private EditText txtUsername;
    private String mUsername;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.connect();
        txtUsername = findViewById(R.id.username_input);
        Button btnJoin = findViewById(R.id.sign_in_button);
        btnJoin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                emitUsername();
            }
        });

        mSocket.on("login", onLogin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("login", onLogin);
    }

    private void emitUsername() {
        txtUsername.setError(null);
        String username = txtUsername.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            txtUsername.setError("Please enter a valid username");
            txtUsername.requestFocus();
            return;
        }

        mUsername = username;
        mSocket.emit("add user", username);
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            intent.putExtra("selfUsername", mUsername);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            startActivity(intent);
            finish();
        }
    };
}
