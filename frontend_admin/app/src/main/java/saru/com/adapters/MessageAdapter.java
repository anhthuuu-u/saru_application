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
    private final String currentSenderId;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(List<Message> messageList, String currentSenderId) {
        this.messageList = messageList;
        this.currentSenderId = currentSenderId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return (message.getSender() != null && message.getSender().equals(currentSenderId))
                ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                viewType == VIEW_TYPE_SENT ? R.layout.item_message_sent : R.layout.item_message_received,
                parent, false
        );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.txtMessageContent.setText(message.getMessageContent());
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