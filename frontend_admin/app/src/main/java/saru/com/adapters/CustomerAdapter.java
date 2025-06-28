package saru.com.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import saru.com.models.Customer;
import saru.com.app.R;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private List<Customer> customerList;
    private OnCustomerClickListener editListener;
    private OnCustomerClickListener deleteListener;
    private OnCustomerClickListener messageListener;
    private FirebaseFirestore db;

    public interface OnCustomerClickListener {
        void onClick(Customer customer);
    }

    public CustomerAdapter(List<Customer> customerList, OnCustomerClickListener editListener,
                           OnCustomerClickListener deleteListener, OnCustomerClickListener messageListener) {
        this.customerList = customerList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.messageListener = messageListener;
        this.db = FirebaseFirestore.getInstance();
    }

    public void updateList(List<Customer> newList) {
        customerList.clear();
        customerList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.txtCustomerName.setText(customer.getCustomerName());
        holder.txtCustomerPhone.setText(customer.getCustomerPhone());

        // *** FIX APPLIED HERE ***
        // Safely check if 'sex' is null or empty before setting the text.
        String sex = customer.getSex();
        if (sex != null && !sex.isEmpty()) {
            holder.txtCustomerStatus.setText(sex);
        } else {
            holder.txtCustomerStatus.setText("Unknown");
        }

        // The query for unread messages is inefficient here as it runs for every item.
        // For better performance, this logic should be handled differently,
        // but it is not the cause of the crash.
        db.collection("messages")
                .whereEqualTo("customerID", customer.getCustomerID())
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int unreadCount = querySnapshot.size();
                    if (unreadCount > 0) {
                        holder.txtUnreadCount.setText(String.valueOf(unreadCount));
                        holder.txtUnreadCount.setVisibility(View.VISIBLE);
                    } else {
                        holder.txtUnreadCount.setVisibility(View.GONE);
                    }
                });

        holder.btnMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMoreOptions);
            popup.inflate(R.menu.customer_item_menu);
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_edit) {
                    editListener.onClick(customer);
                    return true;
                } else if (itemId == R.id.menu_delete) {
                    deleteListener.onClick(customer);
                    return true;
                } else if (itemId == R.id.menu_message) {
                    messageListener.onClick(customer);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCustomerName, txtCustomerPhone, txtUnreadCount, txtCustomerStatus;
        ImageButton btnMoreOptions;

        ViewHolder(View itemView) {
            super(itemView);
            txtCustomerName = itemView.findViewById(R.id.txtCustomerName);
            txtCustomerPhone = itemView.findViewById(R.id.txtCustomerPhone);
            txtUnreadCount = itemView.findViewById(R.id.txtUnreadCount);
            txtCustomerStatus = itemView.findViewById(R.id.txtCustomerStatus);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}