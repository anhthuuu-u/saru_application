package saru.com.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText edtUserName, edtPassword;
    private CheckBox chkSaveLogin;
    private Button btnLogin;
    private TextView txtNetworkType;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        addViews();
        addEvents();
        restoreLoginInformation();
    }

    private void addViews() {
        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        chkSaveLogin = findViewById(R.id.chkSaveLogin);
        btnLogin = findViewById(R.id.btnLogin);
        txtNetworkType = findViewById(R.id.txtNetworkType);
    }

    private void addEvents() {
        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email = edtUserName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        checkAdminStatus(mAuth.getCurrentUser().getUid());
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkAdminStatus(String uid) {
        db.collection("admins").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            saveLoginInformation();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "You are not an admin", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(this, "Error checking admin status", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveLoginInformation() {
        SharedPreferences preferences = getSharedPreferences("LOGIN_INFORMATION", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("EMAIL", edtUserName.getText().toString());
        editor.putString("PASSWORD", edtPassword.getText().toString());
        editor.putBoolean("SAVED", chkSaveLogin.isChecked());
        editor.apply();
    }

    private void restoreLoginInformation() {
        SharedPreferences preferences = getSharedPreferences("LOGIN_INFORMATION", MODE_PRIVATE);
        String email = preferences.getString("EMAIL", "");
        String password = preferences.getString("PASSWORD", "");
        boolean isSaved = preferences.getBoolean("SAVED", false);
        if (isSaved) {
            edtUserName.setText(email);
            edtPassword.setText(password);
            chkSaveLogin.setChecked(true);
        }
    }
}