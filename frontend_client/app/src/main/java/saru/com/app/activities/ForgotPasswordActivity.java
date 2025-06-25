package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
import saru.com.app.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";
    private FirebaseAuth mAuth;
    private TextInputEditText emailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        // Uncomment for emulator testing
        // mAuth.useEmulator("10.0.2.2", 9099);

        emailEditText = findViewById(R.id.emailEditText);

        if (emailEditText == null) {
            Log.e(TAG, "Email TextInputEditText not found");
            Toast.makeText(this, "UI initialization error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Submit email button
        Button btnSubmitEmail = findViewById(R.id.btn_submit_email);
        if (btnSubmitEmail != null) {
            btnSubmitEmail.setOnClickListener(v -> {
                String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email cannot be empty");
                    Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Invalid email address");
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Password reset email sent to " + email);
                                Toast.makeText(this, "Password reset email sent. Check your inbox!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            } else {
                                Log.e(TAG, "Failed to send reset email: ", task.getException());
                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                Toast.makeText(this, "Failed to send reset email: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }
    }
}