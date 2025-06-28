package saru.com.app.connectors;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saru.com.app.R;
import saru.com.app.activities.ProfileEditActivity;
import saru.com.app.models.Address;

public class AddressAdapter extends ArrayAdapter<Address> {
    private Context context;
    private List<Address> addressList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String customerID; // To store the customerID for Firestore updates

    public AddressAdapter(Context context, List<Address> addressList, Object o) {
        super(context, R.layout.address_item, addressList);
        this.context = context;
        this.addressList = addressList;
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.customerID = customerID; // Initialize with customerID passed from activity
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.address_item, null);
        }

        Address address = addressList.get(position);

        TextView txtAddressName = convertView.findViewById(R.id.txtCustomerFullName);
        TextView txtAddressPhone = convertView.findViewById(R.id.txtCustomerPhone);
        TextView txtAddressDetail = convertView.findViewById(R.id.txtCustomerFullAddress);
        RadioButton radioButtonSetDefault = convertView.findViewById(R.id.RadioButtonSetDefault);

        // Bind data to views
        txtAddressName.setText(address.getName());
        txtAddressPhone.setText(address.getPhone());
        txtAddressDetail.setText(address.getAddress());
        radioButtonSetDefault.setChecked(address.isDefault());

        // Handle RadioButton click to set default address
        radioButtonSetDefault.setOnClickListener(v -> {
            if (radioButtonSetDefault.isChecked()) {
                String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                if (userUID != null && customerID != null) {
                    // Unset all other addresses as default
                    for (int i = 0; i < addressList.size(); i++) {
                        addressList.get(i).setDefault(false);
                    }
                    // Set the clicked address as default
                    address.setDefault(true);

                    // Update Firestore
                    db.collection("customers").document(customerID)
                            .collection("addresses")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (var document : queryDocumentSnapshots) {
                                    Map<String, Object> updateData = new HashMap<>();
                                    updateData.put("isDefault", document.getId().equals(address.getId()) && address.isDefault());
                                    db.collection("customers").document(customerID)
                                            .collection("addresses")
                                            .document(document.getId())
                                            .update(updateData)
                                            .addOnSuccessListener(aVoid -> {
                                                notifyDataSetChanged(); // Refresh the list
                                                Toast.makeText(context, "Default address updated", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Error updating default address", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            });
                }
            }
        });

        // Add long-click listener to the entire item view
        // Add click listener to the entire item view
        convertView.setOnClickListener(v -> {
            Log.d("AddressAdapter", "Click detected at position: " + position);
            if (context instanceof ProfileEditActivity) {
                ((ProfileEditActivity) context).showAddressContextMenu(position);
            } else {
                Log.e("AddressAdapter", "Context is not ProfileEditActivity");
            }
        });

        // Ensure RadioButton doesn't interfere with long-click
        radioButtonSetDefault.setOnLongClickListener(v -> false); // Prevent RadioButton from consuming long-click

        return convertView;
    }

    // Method to set customerID (called from activity)
    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
}