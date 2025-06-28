package saru.com.app.activities;

import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox; // Import CheckBox
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import saru.com.app.R;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText txtLoginEmail;
    TextInputEditText txtLoginPassword;
    Button btnLoginComplete;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    CheckBox chkRememberMe; // Declare CheckBox

    // Constants for SharedPreferences keys
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_REMEMBER_ME = "remember_me";

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        // Firebase automatically handles session persistence.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // If user is already logged in, redirect to Homepage
            Intent intent = new Intent(getApplicationContext(), Homepage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        txtLoginEmail = findViewById(R.id.edtLoginEmail);
        txtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLoginComplete = findViewById(R.id.btn_complete_login);
        progressBar = findViewById(R.id.progressBar);
        chkRememberMe = findViewById(R.id.chkSaveLoginInfor); // Initialize CheckBox

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Custom back button (if exists in layout)
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Login button click listener
        Button btnLogin = findViewById(R.id.btn_complete_login);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressBar.setVisibility(View.VISIBLE);
                    String email, password;
                    email = String.valueOf(txtLoginEmail.getText()).trim(); // Trim whitespace
                    password = String.valueOf(txtLoginPassword.getText());

                    // Validate input
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(LoginActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(LoginActivity.this, "Please enter valid password", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    // Attempt to sign in with Firebase
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE); // Hide ProgressBar
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                                        saveLoginInformation(email, chkRememberMe.isChecked()); // Save login info
                                        Intent intent = new Intent(getApplicationContext(), Homepage.class);
                                        startActivity(intent);
                                        finish(); // Close LoginActivity
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Authentication failed. " + task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show(); // Show detailed error
                                    }
                                }
                            });
                }
            });
        }

        // Forgot password button (TextView)
        TextView btnForgotPassword = findViewById(R.id.btn_forgot_password);
        if (btnForgotPassword != null) {
            btnForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        // Sign up button (TextView)
        TextView btnSignup = findViewById(R.id.btn_signup);
        if (btnSignup != null) {
            btnSignup.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }

        // Additional signup TextView (textView4 in example, assuming txtBlogList_SeemoreInfo is its ID)
        TextView textViewSignup = findViewById(R.id.txtBlogList_SeemoreInfo);
        if (textViewSignup != null) {
            textViewSignup.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }

        // Restore saved login information when the activity is created
        restoreLoginInformation();
    }

    /**
     * Saves the user's email and "remember me" preference to SharedPreferences.
     * Only the email is saved, not the password, as Firebase handles authentication tokens.
     *
     * @param email The user's email.
     * @param rememberMe True if the "Remember Me" checkbox is checked, false otherwise.
     */
    private void saveLoginInformation(String email, boolean rememberMe) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (rememberMe) {
            editor.putString(PREF_EMAIL, email);
            editor.putBoolean(PREF_REMEMBER_ME, true);
        } else {
            editor.remove(PREF_EMAIL); // Clear saved email if not remembering
            editor.putBoolean(PREF_REMEMBER_ME, false);
        }
        editor.apply(); // Use apply() for asynchronous saving
    }

    /**
     * Restores the saved login email and "remember me" preference from SharedPreferences.
     * If "remember me" was checked, the email field is pre-filled and the checkbox is set.
     */
    private void restoreLoginInformation() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(PREF_REMEMBER_ME, false); // Default to false
        String savedEmail = preferences.getString(PREF_EMAIL, ""); // Default to empty string

        if (rememberMe && !TextUtils.isEmpty(savedEmail)) {
            txtLoginEmail.setText(savedEmail);
            chkRememberMe.setChecked(true);
        } else {
            txtLoginEmail.setText(""); // Clear email if not remembered or no email saved
            chkRememberMe.setChecked(false);
        }
    }

    // No need to override onPause/onResume for saving/restoring here
    // as it's handled on successful login and onCreate respectively.
}
