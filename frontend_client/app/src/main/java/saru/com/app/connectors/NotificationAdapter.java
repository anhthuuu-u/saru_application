package saru.com.app.connectors;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import saru.com.app.R;
import saru.com.app.models.Notification;

public class NotificationAdapter extends ArrayAdapter<Notification> {
    private Activity context;
    private int resource;
    private ArrayList<Notification> selectedNotifications;
    private OnSelectionChangedListener selectionChangedListener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    public NotificationAdapter(@NonNull Activity context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.selectedNotifications = new ArrayList<>();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public ArrayList<Notification> getSelectedNotifications() {
        return selectedNotifications;
    }

    public void selectAll(boolean isChecked) {
        selectedNotifications.clear();
        if (isChecked) {
            selectedNotifications.addAll(getList());
        }
        notifyDataSetChanged();
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged();
        }
    }

    private ArrayList<Notification> getList() {
        ArrayList<Notification> list = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            list.add(getItem(i));
        }
        return list;
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
            holder.chkNotiSelect = convertView.findViewById(R.id.chkNotiSelect);
            holder.imgNotiDelete = convertView.findViewById(R.id.imgNotiDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification notification = getItem(position);
        holder.txtNotiTitle.setText(notification.getNotiTitle());
        holder.txtNotiContent.setText(notification.getNoti_content());
        String formattedTime = formatDate(notification.getNotiTime());
        holder.txtNotiTime.setText(formattedTime);

        // Handle checkbox
        holder.chkNotiSelect.setOnCheckedChangeListener(null); // Prevent recursive calls
        holder.chkNotiSelect.setChecked(selectedNotifications.contains(notification));
        holder.chkNotiSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedNotifications.add(notification);
            } else {
                selectedNotifications.remove(notification);
            }
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged();
            }
        });

        // Handle delete button
        holder.imgNotiDelete.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("notifications").document(notification.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        remove(notification);
                        selectedNotifications.remove(notification);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show();
                        if (selectionChangedListener != null) {
                            selectionChangedListener.onSelectionChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        return convertView;
    }

    private String formatDate(com.google.firebase.Timestamp timestamp) {
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
        CheckBox chkNotiSelect;
        ImageView imgNotiDelete;
    }
}