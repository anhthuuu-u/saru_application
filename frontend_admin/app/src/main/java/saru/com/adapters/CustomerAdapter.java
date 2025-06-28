package saru.com.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import saru.com.models.Customer;
import saru.com.app.R;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private List<Customer> masterCustomerList; // Master list to hold all customers
    private List<Customer> filteredCustomerList; // List to display
    private OnCustomerClickListener editListener;
    private OnCustomerClickListener deleteListener;
    private OnCustomerClickListener messageListener;

    public interface OnCustomerClickListener {
        void onClick(Customer customer);
    }

    public CustomerAdapter(List<Customer> customerList, OnCustomerClickListener editListener,
                           OnCustomerClickListener deleteListener, OnCustomerClickListener messageListener) {
        this.masterCustomerList = new ArrayList<>(customerList);
        this.filteredCustomerList = new ArrayList<>(customerList);
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.messageListener = messageListener;
    }

    // Update the master list and refresh the filtered list
    public void updateList(List<Customer> newList) {
        masterCustomerList.clear();
        masterCustomerList.addAll(newList);
        filter(""); // Apply empty filter to show all items
    }

    // Filter the list based on a query
    public void filter(String query) {
        filteredCustomerList.clear();
        if (query.isEmpty()) {
            filteredCustomerList.addAll(masterCustomerList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Customer customer : masterCustomerList) {
                if (customer.getCustomerName().toLowerCase().contains(lowerCaseQuery) ||
                        customer.getCustomerPhone().toLowerCase().contains(lowerCaseQuery)) {
                    filteredCustomerList.add(customer);
                }
            }
        }
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
        Customer customer = filteredCustomerList.get(position);
        holder.txtCustomerName.setText(customer.getCustomerName());
        holder.txtCustomerPhone.setText(customer.getCustomerPhone());
        holder.txtUnreadCount.setVisibility(View.GONE);
        holder.txtCustomerStatus.setText("Status: Active"); // Placeholder status

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
        return filteredCustomerList.size();
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