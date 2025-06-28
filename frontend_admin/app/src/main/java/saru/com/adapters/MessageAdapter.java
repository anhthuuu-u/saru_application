package saru.com.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import saru.com.models.Message;
import saru.com.app.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<Message> messageList;
    private final String currentUserID;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(List<Message> messageList, String currentUserID) {
        this.messageList = messageList;
        this.currentUserID = currentUserID;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        // This line requires message.getSenderID()
        if (message.getSenderID() != null && message.getSenderID().equals(currentUserID)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        // This line requires message.getContent()
        holder.txtMessageContent.setText(message.getContent());
        if (message.getTimestamp() != null) {
            holder.txtMessageTime.setText(timeFormat.format(message.getTimestamp().toDate()));
        } else {
            holder.txtMessageTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessageContent, txtMessageTime;

        ViewHolder(View itemView) {
            super(itemView);
            txtMessageContent = itemView.findViewById(R.id.txtMessageContent);
            txtMessageTime = itemView.findViewById(R.id.txtMessageTime);
        }
    }
}