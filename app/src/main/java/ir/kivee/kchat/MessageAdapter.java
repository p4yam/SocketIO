package ir.kivee.kchat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

/**
 * Created by payam on 10/31/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messages;
    private String self;
    private Context context;

    public MessageAdapter(List<Message> messages, String self, Context context) {
        this.messages = messages;
        this.self = self;
        this.context = context;
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = 0;
        switch (viewType) {
            case Message.TYPE_MESSAGE:
                layout = R.layout.item_message;
                break;
            case Message.TYPE_LOG:
                layout = R.layout.item_log;
                break;
            case Message.TYPE_TYPING:
                layout = R.layout.item_typing;
                break;
            default:
                break;
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.setUsername(message.getUsername());
        holder.setMessage(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername;
        TextView txtMessage;

        ViewHolder(View itemView) {
            super(itemView);
            txtUsername = itemView.findViewById(R.id.username);
            txtMessage = itemView.findViewById(R.id.message);
        }

        void setUsername(String username) {
            if (txtUsername == null) return;
            txtUsername.setText(username);
            if (Objects.equals(username, self))
                txtUsername.setTextColor(context.getResources()
                        .getColor(R.color.colorAccent));
            else
                txtUsername.setTextColor(context.getResources()
                        .getColor(R.color.colorPrimary));
        }

        void setMessage(String message) {
            if (txtMessage == null) return;
            txtMessage.setText(message);
        }
    }
}
