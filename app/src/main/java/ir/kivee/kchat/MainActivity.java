package ir.kivee.kchat;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private static final int TYPING_TIMER_LENGTH = 600;
    private RecyclerView rvContent;
    private MessageAdapter adapter;
    private List<Message> messages;
    private EditText etInput;
    private int numUsers;
    private String selfUsername;
    private Socket socket;
    private boolean isTyping = false;
    private Handler typingHandler;
    private Boolean isConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startUp();
        connectSocket();

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!socket.connected()) return;

                if (!isTyping) {
                    isConnected = true;
                    socket.emit("typing");
                }

                typingHandler.removeCallbacks(onTypingTimeOut);
                typingHandler.postDelayed(onTypingTimeOut, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        ImageView imgSend=findViewById(R.id.activity_main_send_message);
        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend();
            }
        });
    }

    private void startUp() {
        rvContent = findViewById(R.id.activity_main_recycler);
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        etInput = findViewById(R.id.activity_main_message_input);
        messages = new ArrayList<>();
        typingHandler = new Handler();
        numUsers = getIntent().getExtras().getInt("numUsers", 1);
        selfUsername = getIntent().getExtras().getString("selfUsername");
        adapter = new MessageAdapter(messages, selfUsername, this);
        rvContent.setAdapter(adapter);
    }

    private void connectSocket() {
        ChatApplication app = (ChatApplication) getApplication();
        socket = app.getSocket();
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectionError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectionError);
        socket.on("new message", onNewMessage);
        socket.on("user joined", onUserJoined);
        socket.on("user left", onUserLeft);
        socket.on("typing", onTyping);
        socket.on("stop typing", onStopTyping);
        socket.connect();

        //TODO : add log to recycler
    }

    private void addLog(String message) {
        messages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        adapter.notifyItemInserted(messages.size() - 1);
        rvContent.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void addParticipantsLog(int numUsers) {
        if (numUsers == 1)
            addLog("There is " + numUsers + " user in room.");
        else
            addLog("There are " + numUsers + " users in room.");
    }

    private void addMessage(String username, String message) {
        messages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username)
                .message(": "+message).build());
        adapter.notifyItemInserted(messages.size() - 1);
        rvContent.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void addTyping(String username) {
        messages.add(new Message.Builder(Message.TYPE_TYPING)
                .username(username).build());
        adapter.notifyItemInserted(messages.size() - 1);
        rvContent.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void removeTyping(String username) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message.getType() == Message.TYPE_TYPING && message.getUsername().equals(username)) {
                messages.remove(i);
                adapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend(){
        if (!socket.connected()) return;
        String message=etInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)){
            etInput.requestFocus();
            return;
        }
        etInput.setText("");
        addMessage(selfUsername,message);
        socket.emit("new message",message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();

        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectionError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectionError);
        socket.off("new message", onNewMessage);
        socket.off("user joined", onUserJoined);
        socket.off("user left", onUserLeft);
        socket.off("typing", onTyping);
        socket.off("stop typing", onStopTyping);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    if (selfUsername != null)
                        Toast.makeText(MainActivity.this,
                                "Connected", Toast.LENGTH_LONG).show();
                    isConnected = true;
                }
            }
        });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    Toast.makeText(MainActivity.this,
                            "Disconnected", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "Connection Error", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String username;
                String message;
                JSONObject data = (JSONObject) args[0];
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                } catch (JSONException ignored) {
                    return;
                }
                removeTyping(username);
                addMessage(username,message);
            }
        });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    int numUsers;
                    String username;

                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException ignored) {
                        return;
                    }
                    addLog(username+" joined.");
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    int numUsers;
                    String username;

                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException ignored) {
                        return;
                    }
                    addLog(username+" left the room.");
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;

                    try {
                        username = data.getString("username");
                    } catch (JSONException ignored) {
                        return;
                    }
                    addTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;

                    try {
                        username = data.getString("username");
                    } catch (JSONException ignored) {
                        return;
                    }
                    removeTyping(username);
                }
            });
        }
    };

    private Runnable onTypingTimeOut = new Runnable() {
        @Override
        public void run() {
            if (!isTyping) return;

            isTyping = false;
            socket.emit("stop typing");
        }
    };
}
