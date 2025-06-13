package saru.com.app.connectors;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import saru.com.app.R;
import saru.com.app.models.Notification;

public class NotificationAdapter extends ArrayAdapter<Notification> {
    private Activity context;
    private int resource;

    public NotificationAdapter(@NonNull Activity context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.txtNotiTitle = convertView.findViewById(R.id.txtNotiTitle);
            holder.txtNotiContent = convertView.findViewById(R.id.txtNotiContent);
            holder.txtNotiTime = convertView.findViewById(R.id.txtNotiTime);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification notification = getItem(position);
        holder.txtNotiTitle.setText(notification.getNotiTitle());
        holder.txtNotiContent.setText(notification.getNoti_content());
        String formattedTime = formatDate(notification.getNotiTime());
        holder.txtNotiTime.setText(formattedTime);

        return convertView;
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "Unknown time";
        }
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return outputFormat.format(timestamp.toDate());
    }

    static class ViewHolder {
        TextView txtNotiTitle;
        TextView txtNotiContent;
        TextView txtNotiTime;
    }
}