package saru.com.app.connectors;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.google.firebase.auth.FirebaseAuth;
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
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    public NotificationAdapter(@NonNull Activity context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.selectedNotifications = new ArrayList<>();
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
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
        if (notification != null) {
            holder.txtNotiTitle.setText(notification.getNotiTitle());
            holder.txtNotiContent.setText(notification.getNoti_content());
            String formattedTime = formatDate(notification.getNotiTime());
            holder.txtNotiTime.setText(formattedTime);

            // Handle checkbox
            holder.chkNotiSelect.setOnCheckedChangeListener(null); // Prevent recursive calls
            holder.chkNotiSelect.setChecked(selectedNotifications.contains(notification));
            holder.chkNotiSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedNotifications.contains(notification)) {
                        selectedNotifications.add(notification);
                    }
                } else {
                    selectedNotifications.remove(notification);
                }
                if (selectionChangedListener != null) {
                    selectionChangedListener.onSelectionChanged();
                }
            });

            // Handle delete button with improved security
            holder.imgNotiDelete.setOnClickListener(v -> {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(context, "Please log in to delete notifications", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userUID = mAuth.getCurrentUser().getUid();

                // Verify user has permission to delete this notification
                db.collection("accounts").document(userUID).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String userAccountID = documentSnapshot.getString("AccountID");

                                if (userAccountID == null) {
                                    Toast.makeText(context, "Error: User Account ID not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Check if the notification belongs to this user
                                if (!userAccountID.equals(notification.getAccountID())) {
                                    Toast.makeText(context, "Unauthorized: This notification doesn't belong to you", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Show confirmation dialog
                                new AlertDialog.Builder(context)
                                        .setTitle("Delete Notification")
                                        .setMessage("Are you sure you want to delete this notification?")
                                        .setIcon(R.mipmap.ic_noti)
                                        .setPositiveButton(R.string.title_confirm_exit_message_yes, (dialog, which) -> {
                                            // Proceed with deletion
                                            deleteNotification(notification);
                                        })
                                        .setNegativeButton(R.string.title_confirm_exit_message_no, (dialog, which) -> dialog.dismiss())
                                        .setCancelable(true)
                                        .show();

                            } else {
                                Toast.makeText(context, "Error: User account not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error verifying user account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        }

        return convertView;
    }

    private void deleteNotification(Notification notification) {
        db.collection("notifications").document(notification.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    remove(notification);
                    selectedNotifications.remove(notification);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Notification deleted successfully", Toast.LENGTH_SHORT).show();
                    if (selectionChangedListener != null) {
                        selectionChangedListener.onSelectionChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error deleting notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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