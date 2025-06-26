package saru.com.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import saru.com.app.R;
import saru.com.app.connectors.AddressAdapter;
import saru.com.app.models.Address;

public class ProfileEditActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> paymentMethodsList;
    private String customerID; // Store the correct customerID

    // UI elements
    ImageView imgCustomerAva;
    EditText edtCustomerName, edtCustomerPhone, edtCustomerBirth, edtCustomerMail, edtCustomerPass, edtCustomerJoinDate;
    TextView txtCustomerName, txtCustomerEmail, txtCustomerFullName, txtCustomerFullAddress;
    RadioButton ckbMale, ckbFemale;
    ListView lvAddresses;
    private List<Address> addressList = new ArrayList<>();
    private AddressAdapter addressAdapter;
    private Spinner paymentMethodSpinner;

    private static final int REQUEST_CODE_ADD_ADDRESS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        try {
            initializeFirebase();
            initializeViews();

            if (mAuth == null) {
                Log.e("ProfileEditActivity", "mAuth is null, initializing FirebaseAuth");
                mAuth = FirebaseAuth.getInstance();
            }

            String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
            Log.d("ProfileEditActivity", "User UID: " + userUID);

            if (userUID != null) {
                fetchUserProfile(userUID);
                fetchPaymentMethods(userUID);
            } else {
                Log.d("ProfileEditActivity", "User not logged in.");
                Toast.makeText(ProfileEditActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(ProfileEditActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                return;
            }

            ImageView backIcon = findViewById(R.id.ic_back_arrow);
            if (backIcon != null) {
                backIcon.setOnClickListener(v -> finish());
            } else {
                Log.e("ProfileEditActivity", "backIcon is null");
            }

            ImageView reloadBtn = findViewById(R.id.btn_reload);
            if (reloadBtn != null) {
                reloadBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(ProfileEditActivity.this, ProfileUpdateCardActivity.class);
                    startActivity(intent);
                });
            } else {
                Log.e("ProfileEditActivity", "reloadBtn is null");
            }

            // Add address button
            Button btnAddAddress = findViewById(R.id.btnAddAddress);
            if (btnAddAddress != null) {
                btnAddAddress.setOnClickListener(v -> {
                    if (userUID != null && customerID != null) {
                        Intent intent = new Intent(ProfileEditActivity.this, AddAddressActivity.class);
                        intent.putExtra("customerID", customerID);
                        startActivityForResult(intent, REQUEST_CODE_ADD_ADDRESS);
                    } else {
                        Log.e("ProfileEditActivity", "userUID or customerID is null in btnAddAddress click: userUID=" + userUID + ", customerID=" + customerID);
                        Toast.makeText(ProfileEditActivity.this, "Failed to get customer ID", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("ProfileEditActivity", "btnAddAddress is null");
            }
        } catch (Exception e) {
            Log.e("ProfileEditActivity", "Error in onCreate: ", e);
            Toast.makeText(ProfileEditActivity.this, "An error occurred, please try again later.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_ADDRESS && resultCode == RESULT_OK && data != null) {
            String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
            if (userUID != null && customerID != null) {
                Log.d("ProfileEditActivity", "Received result from AddAddressActivity");
                String addressId = data.getStringExtra("addressId");
                String name = data.getStringExtra("name");
                String phone = data.getStringExtra("phone");
                String address = data.getStringExtra("address");

                if (addressId != null) {
                    // Update the specific address in the list
                    for (int i = 0; i < addressList.size(); i++) {
                        if (addressList.get(i).getId() != null && addressList.get(i).getId().equals(addressId)) {
                            addressList.get(i).setName(name);
                            addressList.get(i).setPhone(phone);
                            addressList.get(i).setAddress(address);
                            Log.d("ProfileEditActivity", "Updated address at position " + i + " with ID: " + addressId);
                            break;
                        }
                    }
                    if (addressAdapter != null) {
                        addressAdapter.notifyDataSetChanged();
                        setListViewHeightBasedOnChildren(lvAddresses);
                        Log.d("ProfileEditActivity", "Adapter notified after edit");
                    }
                } else {
                    // For new addresses, rely on the full refresh
                    Log.d("ProfileEditActivity", "No addressId, performing full refresh");
                    fetchUserProfile(userUID);
                }
            } else {
                Log.e("ProfileEditActivity", "userUID or customerID is null in onActivityResult: userUID=" + userUID + ", customerID=" + customerID);
            }
        }
    }

    private void initializeFirebase() {
        Log.d("ProfileEditActivity", "Initializing Firebase...");
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.e("ProfileEditActivity", "Error initializing Firebase: ", e);
        }
    }

    private void initializeViews() {
        imgCustomerAva = findViewById(R.id.imgCustomerAva);
        if (imgCustomerAva == null) Log.e("ProfileEditActivity", "imgCustomerAva is null");

        edtCustomerName = findViewById(R.id.edtCustomerName);
        if (edtCustomerName == null) Log.e("ProfileEditActivity", "edtCustomerName is null");

        edtCustomerPhone = findViewById(R.id.edtCustomerPhone);
        if (edtCustomerPhone == null) Log.e("ProfileEditActivity", "edtCustomerPhone is null");

        edtCustomerBirth = findViewById(R.id.edtCustomerBirth);
        if (edtCustomerBirth == null) Log.e("ProfileEditActivity", "edtCustomerBirth is null");

        edtCustomerMail = findViewById(R.id.edtCustomerMail);
        if (edtCustomerMail == null) Log.e("ProfileEditActivity", "edtCustomerMail is null");

        edtCustomerPass = findViewById(R.id.edtCustomerPass);
        if (edtCustomerPass == null) Log.e("ProfileEditActivity", "edtCustomerPass is null");

        edtCustomerJoinDate = findViewById(R.id.edtCustomerJoinDate);
        if (edtCustomerJoinDate == null) Log.e("ProfileEditActivity", "edtCustomerJoinDate is null");

        ckbMale = findViewById(R.id.ckbMale);
        if (ckbMale == null) Log.e("ProfileEditActivity", "ckbMale is null");

        ckbFemale = findViewById(R.id.ckbFemale);
        if (ckbFemale == null) Log.e("ProfileEditActivity", "ckbFemale is null");

        txtCustomerFullName = findViewById(R.id.txtCustomerFullName);
        if (txtCustomerFullName == null) Log.e("ProfileEditActivity", "txtCustomerFullName is null");

        txtCustomerFullAddress = findViewById(R.id.txtCustomerFullAddress);
        if (txtCustomerFullAddress == null) Log.e("ProfileEditActivity", "txtCustomerFullAddress is null");

        lvAddresses = findViewById(R.id.lvAddresses);
        if (lvAddresses == null) Log.e("ProfileEditActivity", "lvAddresses is null");

        txtCustomerName = findViewById(R.id.txtCustomerName);
        if (txtCustomerName == null) Log.e("ProfileEditActivity", "txtCustomerName is null");

        txtCustomerEmail = findViewById(R.id.txtCustomerEmail);
        if (txtCustomerEmail == null) Log.e("ProfileEditActivity", "txtCustomerEmail is null");

        paymentMethodSpinner = findViewById(R.id.paymentMethod);
        if (paymentMethodSpinner == null) Log.e("ProfileEditActivity", "paymentMethodSpinner is null");

        addressAdapter = new AddressAdapter(this, addressList, null); // Initialize with null customerID, will be set later
        if (lvAddresses != null) {
            lvAddresses.setAdapter(addressAdapter);
        } else {
            Log.e("ProfileEditActivity", "Cannot set adapter, lvAddresses is null");
        }
    }

    private void fetchUserProfile(String userUID) {
        Log.d("ProfileEditActivity", "Fetching user profile for UID: " + userUID);

        try {
            DocumentReference accountRef = db.collection("accounts").document(userUID);

            accountRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String email = task.getResult().getString("CustomerEmail");
                    String password = task.getResult().getString("CustomerPassword");

                    if (txtCustomerEmail != null) txtCustomerEmail.setText(email);
                    if (edtCustomerMail != null) edtCustomerMail.setText(email);
                    if (edtCustomerPass != null) edtCustomerPass.setText(password);

                    customerID = task.getResult().getString("CustomerID"); // Store the correct customerID
                    if (customerID != null) {
                        Log.d("ProfileEditActivity", "Fetched CustomerID: " + customerID);
                        fetchCustomerData(customerID);
                        if (addressAdapter != null) {
                            addressAdapter.setCustomerID(customerID); // Set customerID for AddressAdapter
                        }
                    } else {
                        Log.e("ProfileEditActivity", "CustomerID is missing.");
                        Toast.makeText(ProfileEditActivity.this, "CustomerID is missing", Toast.LENGTH_SHORT).show();
                    }

                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null && edtCustomerJoinDate != null) {
                        long creationTimestamp = currentUser.getMetadata().getCreationTimestamp();
                        Date date = new Date(creationTimestamp);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String createdDate = sdf.format(date);
                        edtCustomerJoinDate.setText(createdDate);
                    }
                } else {
                    Log.e("ProfileEditActivity", "Error fetching user data: " + task.getException());
                    Toast.makeText(ProfileEditActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("ProfileEditActivity", "Error fetching user profile: ", e);
        }
    }

    private void fetchCustomerData(String customerID) {
        Log.d("ProfileEditActivity", "Fetching customer data for CustomerID: " + customerID);

        try {
            DocumentReference customerRef = db.collection("customers").document(customerID);

            customerRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    addressList.clear(); // Clear the list to avoid duplicates

                    String name = task.getResult().getString("CustomerName");
                    Object phoneObject = task.getResult().get("CustomerPhone");
                    String phone = (phoneObject != null) ? phoneObject.toString() : null;
                    String birth = task.getResult().getString("CustomerBirth");
                    String avatarUrl = task.getResult().getString("CustomerAvatar");
                    String sex = task.getResult().getString("Sex");
                    String address = task.getResult().getString("CustomerAdd");

                    if (name != null) {
                        if (txtCustomerName != null) txtCustomerName.setText(name);
                        if (edtCustomerName != null) edtCustomerName.setText(name);
                        if (txtCustomerFullName != null) txtCustomerFullName.setText(name);
                    }
                    if (phone != null && edtCustomerPhone != null) {
                        edtCustomerPhone.setText(phone);
                    }
                    if (birth != null && edtCustomerBirth != null) {
                        edtCustomerBirth.setText(birth);
                    }
                    if (address != null && txtCustomerFullAddress != null) {
                        txtCustomerFullAddress.setText(address);
                        // Add the main address with a default status
                        addressList.add(new Address(null, name, phone, address, true));
                        Log.d("ProfileEditActivity", "Added main address: " + name + ", " + phone + ", " + address);
                    }

                    // Fetch all addresses from subcollection 'addresses'
                    db.collection("customers").document(customerID)
                            .collection("addresses")
                            .get()
                            .addOnCompleteListener(addressTask -> {
                                if (addressTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : addressTask.getResult()) {
                                        String addrName = document.getString("name");
                                        String addrPhone = document.getString("phone");
                                        String addrAddress = document.getString("address");
                                        boolean isDefault = document.getBoolean("isDefault") != null && document.getBoolean("isDefault");
                                        String docId = document.getId();
                                        if (addrName != null && addrPhone != null && addrAddress != null) {
                                            addressList.add(new Address(docId, addrName, addrPhone, addrAddress, isDefault));
                                            Log.d("ProfileEditActivity", "Added subcollection address: " + addrName + ", " + addrPhone + ", " + addrAddress + ", isDefault: " + isDefault);
                                        }
                                    }
                                    if (addressAdapter != null) {
                                        addressAdapter.notifyDataSetChanged();
                                        setListViewHeightBasedOnChildren(lvAddresses); // Set dynamic height
                                        Log.d("ProfileEditActivity", "Adapter notified, addressList size: " + addressList.size());
                                    } else {
                                        Log.e("ProfileEditActivity", "addressAdapter is null");
                                    }
                                } else {
                                    Log.e("ProfileEditActivity", "Error fetching addresses: " + addressTask.getException());
                                }
                            });

                    if (sex != null) {
                        if ("Female".equalsIgnoreCase(sex) && ckbFemale != null && ckbMale != null) {
                            ckbFemale.setChecked(true);
                            ckbMale.setChecked(false);
                        } else if ("Male".equalsIgnoreCase(sex) && ckbMale != null && ckbFemale != null) {
                            ckbMale.setChecked(true);
                            ckbFemale.setChecked(false);
                        }
                    }

                    if (avatarUrl != null && imgCustomerAva != null) {
                        Glide.with(ProfileEditActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.mipmap.img_circle)
                                .into(imgCustomerAva);
                    }
                } else {
                    Log.e("ProfileEditActivity", "Error fetching customer data: " + task.getException());
                    Toast.makeText(ProfileEditActivity.this, "Failed to load customer data.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("ProfileEditActivity", "Error fetching customer data: ", e);
        }
    }

    private void fetchPaymentMethods(String customerID) {
        Log.d("ProfileEditActivity", "Fetching payment methods for CustomerID: " + customerID);
        paymentMethodsList = new ArrayList<>(); // Reset the list
        final CountDownLatch latch = new CountDownLatch(5); // Wait for 5 collections

        // Iterate through all possible payment collections
        for (int i = 1; i <= 5; i++) {
            final int index = i;
            String paymentCollectionName = "payment0" + index;

            db.collection("paymentofcustomer")
                    .document(customerID)
                    .collection(paymentCollectionName)
                    .get()
                    .addOnCompleteListener(paymentTask -> {
                        try {
                            if (paymentTask.isSuccessful()) {
                                if (!paymentTask.getResult().isEmpty()) {
                                    DocumentSnapshot paymentDoc = paymentTask.getResult().getDocuments().get(0);
                                    if (paymentDoc != null) {
                                        String paymentMethodID = paymentDoc.getString("PaymentMethodID");
                                        if ("1".equals(paymentMethodID)) { // Internet Banking
                                            String cardType = paymentDoc.getString("CardType");
                                            String bank = paymentDoc.getString("Bank");
                                            paymentMethodsList.add(cardType + " - " + bank);
                                        } else if ("0".equals(paymentMethodID)) { // Cash
                                            paymentMethodsList.add("Cash");
                                        } else {
                                            String paymentMethodName = paymentDoc.getString("PaymentMethod");
                                            paymentMethodsList.add(paymentMethodName);
                                        }
                                    }
                                }
                            } else {
                                Log.e("ProfileEditActivity", "Error fetching payment details for collection " + paymentCollectionName + ": " + paymentTask.getException());
                            }
                        } finally {
                            latch.countDown(); // Decrease latch count after each task completes
                        }
                    });
        }

        // Wait for all tasks to complete and then populate the spinner
        new Thread(() -> {
            try {
                latch.await(); // Wait for all 5 collections to be processed
                runOnUiThread(() -> populateSpinner(paymentMethodsList));
            } catch (InterruptedException e) {
                Log.e("ProfileEditActivity", "Interrupted while waiting for latch: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ProfileEditActivity.this, "Error loading payment methods", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void saveSelectedPaymentMethod(String selectedMethod) {
        // Check if the method is not "Cash"
        if (!"Cash".equals(selectedMethod)) {
            String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
            if (userUID != null) {
                db.collection("paymentofcustomer")
                        .document(userUID)
                        .update("selectedPaymentMethod", selectedMethod)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ProfileEditActivity", "Payment method saved successfully: " + selectedMethod);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProfileEditActivity", "Error saving payment method: " + e.getMessage());
                        });
            }
        }
    }

    private void populateSpinner(List<String> paymentMethodsList) {
        Log.d("ProfileEditActivity", "Populating spinner with payment methods: " + paymentMethodsList);

        if (paymentMethodsList.isEmpty()) {
            Log.e("ProfileEditActivity", "No payment methods to populate spinner with.");
            paymentMethodsList.add("No payment methods available"); // Placeholder for empty list
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ProfileEditActivity.this,
                android.R.layout.simple_spinner_item, paymentMethodsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (paymentMethodSpinner != null) {
            paymentMethodSpinner.setAdapter(adapter);

            // Set the selected payment method based on Firestore value
            String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
            if (userUID != null) {
                db.collection("paymentofcustomer")
                        .document(userUID)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String selectedPaymentMethod = documentSnapshot.getString("selectedPaymentMethod");
                                if (selectedPaymentMethod != null) {
                                    int selectedPosition = paymentMethodsList.indexOf(selectedPaymentMethod);
                                    if (selectedPosition != -1) {
                                        paymentMethodSpinner.setSelection(selectedPosition);
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e("ProfileEditActivity", "Error fetching selected payment method: " + e.getMessage()));
            }

            paymentMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String selectedMethod = paymentMethodsList.get(position);
                    Log.d("ProfileEditActivity", "Selected payment method: " + selectedMethod);
                    if (!"No payment methods available".equals(selectedMethod)) {
                        saveSelectedPaymentMethod(selectedMethod);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // Do nothing
                }
            });

            // Add long-click listener to show context menu
            paymentMethodSpinner.setOnLongClickListener(v -> {
                if (paymentMethodSpinner.getSelectedItemPosition() >= 0) {
                    String selectedMethod = paymentMethodsList.get(paymentMethodSpinner.getSelectedItemPosition());
                    if (!"No payment methods available".equals(selectedMethod)) {
                        showContextMenu(selectedMethod);
                    }
                }
                return true;
            });
        } else {
            Log.e("ProfileEditActivity", "paymentMethodSpinner is null in populateSpinner");
        }
    }

    private void showContextMenu(String selectedMethod) {
        String[] options = {"Delete", "Edit"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an option")
                .setItems(options, (dialog, which) -> {
                    String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                    if (userUID != null) {
                        if (which == 0) { // Delete
                            showDeleteConfirmation(selectedMethod, userUID);
                        } else if (which == 1) { // Edit
                            navigateToEdit(selectedMethod, userUID);
                        }
                    }
                });
        builder.create().show();
    }

    private void showDeleteConfirmation(String selectedMethod, String userUID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this payment method?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deletePaymentMethod(selectedMethod, userUID);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true);
        builder.create().show();
    }

    private void deletePaymentMethod(String selectedMethod, String userUID) {
        for (int i = 1; i <= 5; i++) {
            String paymentCollectionName = "payment0" + i;
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(paymentCollectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String cardType = document.getString("CardType");
                                String bank = document.getString("Bank");
                                String currentMethod = cardType + " - " + bank;
                                if (currentMethod.equals(selectedMethod)) {
                                    db.collection("paymentofcustomer")
                                            .document(userUID)
                                            .collection(paymentCollectionName)
                                            .document(document.getId())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("ProfileEditActivity", "Payment method deleted successfully from " + paymentCollectionName);
                                                Toast.makeText(ProfileEditActivity.this, "Payment method deleted", Toast.LENGTH_SHORT).show();
                                                // Refresh all payment methods
                                                fetchPaymentMethods(userUID);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("ProfileEditActivity", "Error deleting payment method: " + e.getMessage());
                                                Toast.makeText(ProfileEditActivity.this, "Error deleting payment method", Toast.LENGTH_SHORT).show();
                                            });
                                    return; // Exit after finding and deleting
                                }
                            }
                        }
                    });
        }
    }

    private void navigateToEdit(String selectedMethod, String userUID) {
        for (int i = 1; i <= 5; i++) {
            String paymentCollectionName = "payment0" + i;
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(paymentCollectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String cardType = document.getString("CardType");
                                String bank = document.getString("Bank");
                                String currentMethod = cardType + " - " + bank;
                                if (currentMethod.equals(selectedMethod)) {
                                    Intent intent = new Intent(ProfileEditActivity.this, ProfileUpdateCardActivity.class);
                                    intent.putExtra("paymentCollection", paymentCollectionName);
                                    intent.putExtra("paymentMethodID", document.getId());
                                    intent.putExtra("cardType", cardType);
                                    intent.putExtra("bank", bank);
                                    intent.putExtra("cardNumber", document.getString("CardNumber"));
                                    intent.putExtra("cvv", document.getString("CVV"));
                                    intent.putExtra("expiryDate", document.getString("ExpiryDate"));
                                    startActivity(intent);
                                    return; // Exit after finding and navigating
                                }
                            }
                        }
                    });
        }
    }

    public void showAddressContextMenu(final int position) {
        Log.d("ProfileEditActivity", "Showing context menu for position: " + position);
        String[] options = {"Delete", "Edit"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an option")
                .setItems(options, (dialog, which) -> {
                    Address selectedAddress = addressList.get(position);
                    if (which == 0) { // Delete
                        showDeleteConfirmation(selectedAddress, position);
                    } else if (which == 1) { // Edit
                        navigateToEdit(selectedAddress);
                    }
                });
        builder.create().show();
    }

    private void showDeleteConfirmation(Address address, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteAddress(address, position);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true);
        builder.create().show();
    }

    private void deleteAddress(Address address, int position) {
        if (customerID != null) {
            if (address.getId() == null) {
                // This is the main address (no document ID)
                addressList.remove(position);
                addressAdapter.notifyDataSetChanged();
                setListViewHeightBasedOnChildren(lvAddresses);
                Toast.makeText(this, "Main address removed from list", Toast.LENGTH_SHORT).show();
            } else {
                // This is a subcollection address
                db.collection("customers").document(customerID)
                        .collection("addresses")
                        .document(address.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ProfileEditActivity", "Address deleted successfully: " + address.getId());
                            addressList.remove(position);
                            addressAdapter.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(lvAddresses);
                            Toast.makeText(ProfileEditActivity.this, "Address deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProfileEditActivity", "Error deleting address: " + e.getMessage());
                            Toast.makeText(ProfileEditActivity.this, "Error deleting address", Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            Log.e("ProfileEditActivity", "customerID is null in deleteAddress");
            Toast.makeText(this, "Error: Customer ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToEdit(Address address) {
        if (customerID != null) {
            Intent intent = new Intent(ProfileEditActivity.this, AddAddressActivity.class);
            intent.putExtra("customerID", customerID); // Use the stored customerID
            intent.putExtra("addressId", address.getId()); // Pass document ID if it exists
            intent.putExtra("name", address.getName());
            intent.putExtra("phone", address.getPhone());
            intent.putExtra("address", address.getAddress());
            intent.putExtra("isEdit", true); // Flag to indicate edit mode
            startActivityForResult(intent, REQUEST_CODE_ADD_ADDRESS);
        } else {
            Log.e("ProfileEditActivity", "customerID is null in navigateToEdit");
            Toast.makeText(this, "Error: Customer ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        if (listView == null || addressAdapter == null) return;

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < addressAdapter.getCount(); i++) {
            View listItem = addressAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (addressAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}