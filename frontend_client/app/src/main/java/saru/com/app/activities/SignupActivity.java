package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

import saru.com.app.R;

public class SignupActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPass, edtConfirmPass;
    private Button btnSignup;
    private TextView txtToLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPassword);
        edtConfirmPass = findViewById(R.id.edtConfirmPassword);
        btnSignup = findViewById(R.id.btn_complete_signup);
        txtToLogin = findViewById(R.id.btn_login);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtName.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String password = edtPass.getText().toString().trim();
                String confirmPass = edtConfirmPass.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(SignupActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(SignupActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignupActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(confirmPass)) {
                    Toast.makeText(SignupActivity.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPass)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String userId = mAuth.getCurrentUser().getUid();
                                    String accountId = "acc_" + userId.substring(0, 8);

                                    // Generate CustomerID using transaction
                                    db.runTransaction(new Transaction.Function<String>() {
                                        @Override
                                        public String apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                            DocumentReference counterRef = db.collection("metadata").document("customer_counter");
                                            DocumentSnapshot counterSnapshot = transaction.get(counterRef);

                                            long lastNumber = 0;
                                            if (counterSnapshot.exists()) {
                                                lastNumber = counterSnapshot.getLong("lastCustomerNumber") != null ? counterSnapshot.getLong("lastCustomerNumber") : 0;
                                            }

                                            String customerId = "cus" + (lastNumber + 1);
                                            DocumentReference customerRef = db.collection("customers").document(customerId);
                                            DocumentSnapshot customerSnapshot = transaction.get(customerRef);

                                            if (customerSnapshot.exists()) {
                                                throw new FirebaseFirestoreException("Customer ID already exists", FirebaseFirestoreException.Code.ABORTED);
                                            }

                                            // Update counter
                                            Map<String, Object> counterData = new HashMap<>();
                                            counterData.put("lastCustomerNumber", lastNumber + 1);
                                            transaction.set(counterRef, counterData);

                                            return customerId;
                                        }
                                    }).addOnSuccessListener(customerId -> {
                                        // Create customer document
                                        Map<String, Object> customerMap = new HashMap<>();
                                        customerMap.put("CustomerName", name);
                                        customerMap.put("CustomerAvatar", "");
                                        customerMap.put("CustomerID", customerId);
                                        customerMap.put("CustomerPhone", "");
                                        customerMap.put("CustomerBirth", "");
                                        customerMap.put("Sex", "");
                                        customerMap.put("ReceiveEmail", false);
                                        customerMap.put("MemberID", "");

                                        // Create account document
                                        Map<String, Object> accountMap = new HashMap<>();
                                        accountMap.put("CustomerEmail", email);
                                        accountMap.put("CustomerID", customerId);
                                        accountMap.put("AccountID", accountId);
                                        accountMap.put("User UID", userId);

                                        // Write to customers collection
                                        db.collection("customers").document(customerId).set(customerMap)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("SignupActivity", "Customer document created: " + customerId);
                                                    // Write to accounts collection
                                                    db.collection("accounts").document(userId).set(accountMap)
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                Log.d("SignupActivity", "Account document created: " + userId);
                                                                Toast.makeText(SignupActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(SignupActivity.this, Homepage.class);
                                                                startActivity(intent);
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("SignupActivity", "Error creating account document: ", e);
                                                                Toast.makeText(SignupActivity.this, "Failed to create account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("SignupActivity", "Error creating customer document: ", e);
                                                    Toast.makeText(SignupActivity.this, "Failed to create customer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }).addOnFailureListener(e -> {
                                        Log.e("SignupActivity", "Error generating customer ID: ", e);
                                        Toast.makeText(SignupActivity.this, "Failed to generate customer ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    Log.e("SignupActivity", "Sign up failed: ", task.getException());
                                    Toast.makeText(SignupActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        txtToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}