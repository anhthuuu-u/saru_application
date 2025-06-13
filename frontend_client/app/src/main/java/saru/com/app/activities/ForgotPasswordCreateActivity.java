package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import saru.com.app.R;

public class ForgotPasswordCreateActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordCreate";
    private FirebaseAuth mAuth;
    private TextInputEditText passwordEditText, confirmPasswordEditText;
    private String actionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password_create);

        mAuth = FirebaseAuth.getInstance();
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        if (passwordEditText == null || confirmPasswordEditText == null) {
            Log.e(TAG, "TextInputEditText not found");
            Toast.makeText(this, "UI initialization error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Log intent details
        Log.d(TAG, "Intent: " + getIntent().toString());
        if (getIntent().getData() != null) {
            Log.d(TAG, "Intent Data: " + getIntent().getData().toString());
        }

        // Handle Dynamic Link
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    if (pendingDynamicLinkData != null && pendingDynamicLinkData.getLink() != null) {
                        actionCode = pendingDynamicLinkData.getLink().getQueryParameter("oobCode");
                        Log.d(TAG, "Dynamic Link oobCode: " + actionCode);
                        verifyActionCode();
                    } else {
                        // Fallback to deep link
                        if (getIntent().getData() != null) {
                            actionCode = getIntent().getData().getQueryParameter("oobCode");
                            Log.d(TAG, "Deep Link oobCode: " + actionCode);
                            verifyActionCode();
                        } else {
                            Log.e(TAG, "No Dynamic Link or deep link provided");
                            Toast.makeText(this, "Invalid reset link", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        }
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Dynamic Link error: ", e);
                    // Fallback to deep link
                    if (getIntent().getData() != null) {
                        actionCode = getIntent().getData().getQueryParameter("oobCode");
                        Log.d(TAG, "Deep Link oobCode: " + actionCode);
                        verifyActionCode();
                    } else {
                        Toast.makeText(this, "Error processing reset link: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }
                });

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        Button btnCreateNewPassword = findViewById(R.id.btn_create_new_password);
        if (btnCreateNewPassword != null) {
            btnCreateNewPassword.setOnClickListener(v -> {
                String newPassword = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
                String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString().trim() : "";

                if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(this, "Please fill in both password fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(actionCode)) {
                    Toast.makeText(this, "Invalid reset link", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return;
                }

                mAuth.confirmPasswordReset(actionCode, newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Password reset successful");
                                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            } else {
                                Log.e(TAG, "Password reset failed: ", task.getException());
                                Toast.makeText(this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void verifyActionCode() {
        if (!TextUtils.isEmpty(actionCode)) {
            mAuth.verifyPasswordResetCode(actionCode)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Invalid or expired oobCode: ", task.getException());
                            Toast.makeText(this, "Invalid or expired reset link", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            Log.d(TAG, "oobCode verified successfully");
                        }
                    });
        } else {
            Log.e(TAG, "No oobCode provided");
            Toast.makeText(this, "Invalid reset link", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}