package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.concurrent.atomic.AtomicBoolean;

import saru.com.app.R;
import saru.com.app.connectors.AddressAdapter;
import saru.com.app.models.Address;

public class ProfileEditActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> paymentMethodsList;

    // UI elements
    ImageView imgCustomerAva;
    EditText edtCustomerName, edtCustomerPhone, edtCustomerBirth, edtCustomerMail, edtCustomerPass, edtCustomerJoinDate;
    TextView txtCustomerName, txtCustomerEmail, txtCustomerFullName, txtCustomerFullAddress;
    RadioButton ckbMale, ckbFemale;
    ListView lvAddresses;
    private List<Address> addressList = new ArrayList<>();
    private AddressAdapter addressAdapter;

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
                fetchPaymentMethods(userUID);  // Fetch payment methods for the user
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
                    if (userUID != null) {
                        db.collection("accounts").document(userUID).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String customerID = documentSnapshot.getString("CustomerID");
                                    if (customerID != null) {
                                        Intent intent = new Intent(ProfileEditActivity.this, AddAddressActivity.class);
                                        intent.putExtra("customerID", customerID);
                                        startActivityForResult(intent, REQUEST_CODE_ADD_ADDRESS);
                                    } else {
                                        Log.e("ProfileEditActivity", "customerID is null");
                                        Toast.makeText(ProfileEditActivity.this, "Failed to get customer ID", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ProfileEditActivity", "Error fetching customer ID: ", e);
                                    Toast.makeText(ProfileEditActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("ProfileEditActivity", "userUID is null in btnAddAddress click");
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
            if (userUID != null) {
                fetchUserProfile(userUID); // Refresh the profile data
                fetchPaymentMethods(userUID);
            } else {
                Log.e("ProfileEditActivity", "userUID is null in onActivityResult");
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
        if (edtCustomerJoinDate == null)
            Log.e("ProfileEditActivity", "edtCustomerJoinDate is null");

        ckbMale = findViewById(R.id.ckbMale);
        if (ckbMale == null) Log.e("ProfileEditActivity", "ckbMale is null");

        ckbFemale = findViewById(R.id.ckbFemale);
        if (ckbFemale == null) Log.e("ProfileEditActivity", "ckbFemale is null");

        txtCustomerFullName = findViewById(R.id.txtCustomerFullName);
        if (txtCustomerFullName == null)
            Log.e("ProfileEditActivity", "txtCustomerFullName is null");

        txtCustomerFullAddress = findViewById(R.id.txtCustomerFullAddress);
        if (txtCustomerFullAddress == null)
            Log.e("ProfileEditActivity", "txtCustomerFullAddress is null");

        lvAddresses = findViewById(R.id.lvAddresses);
        if (lvAddresses == null) Log.e("ProfileEditActivity", "lvAddresses is null");

        txtCustomerName = findViewById(R.id.txtCustomerName);
        if (txtCustomerName == null) Log.e("ProfileEditActivity", "txtCustomerName is null");

        txtCustomerEmail = findViewById(R.id.txtCustomerEmail);
        if (txtCustomerEmail == null) Log.e("ProfileEditActivity", "txtCustomerEmail is null");

        addressAdapter = new AddressAdapter(this, addressList);
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

                    String customerID = task.getResult().getString("CustomerID");
                    if (customerID != null) {
                        Log.d("ProfileEditActivity", "Fetched CustomerID: " + customerID);
                        fetchCustomerData(customerID);
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
                    addressList.clear();

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
                        addressList.add(new Address(name, phone, address)); // Thêm địa chỉ chính
                    }

                    // Lấy tất cả địa chỉ từ subcollection 'addresses'
                    db.collection("customers").document(customerID)
                            .collection("addresses")
                            .get()
                            .addOnCompleteListener(addressTask -> {
                                if (addressTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : addressTask.getResult()) {
                                        String addrName = document.getString("name");
                                        String addrPhone = document.getString("phone");
                                        String addrAddress = document.getString("address");
                                        if (addrName != null && addrPhone != null && addrAddress != null) {
                                            addressList.add(new Address(addrName, addrPhone, addrAddress));
                                        }
                                    }
                                    if (addressAdapter != null) {
                                        addressAdapter.notifyDataSetChanged();
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
        loadPaymentMethod(1, customerID, paymentMethodsList, new AtomicBoolean(false));
    }

    private void loadPaymentMethod(int i, String customerID, List<String> paymentMethodsList, AtomicBoolean isComplete) {
        if (isComplete.get()) return; // Stop if already completed

        String paymentCollectionName = "payment0" + i;

        db.collection("paymentofcustomer")
                .document(customerID)
                .collection(paymentCollectionName)
                .get()
                .addOnCompleteListener(paymentTask -> {
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
                            // Move to the next collection
                            loadPaymentMethod(i + 1, customerID, paymentMethodsList, isComplete);
                        } else {
                            // No more payment methods, mark as complete and populate spinner
                            isComplete.set(true);
                            populateSpinner(paymentMethodsList);
                        }
                    } else {
                        Log.e("ProfileEditActivity", "Error fetching payment details for collection " + paymentCollectionName + ": " + paymentTask.getException());
                        isComplete.set(true);
                        populateSpinner(paymentMethodsList); // Populate even in case of error
                    }
                });
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
        Log.d("ProfileEditActivity", "Populating spinner with payment methods");

        if (paymentMethodsList.isEmpty()) {
            Log.e("ProfileEditActivity", "No payment methods to populate spinner with.");
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ProfileEditActivity.this,
                    android.R.layout.simple_spinner_item, paymentMethodsList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            Spinner paymentMethodSpinner = findViewById(R.id.paymentMethod);
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
                                    // Check if the selected method exists in the list
                                    int selectedPosition = paymentMethodsList.indexOf(selectedPaymentMethod);
                                    if (selectedPosition != -1) {
                                        paymentMethodSpinner.setSelection(selectedPosition); // Set the selected method in the spinner
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

                    // Save the selected payment method
                    saveSelectedPaymentMethod(selectedMethod);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // Do nothing
                }
            });
        }
    }





}






