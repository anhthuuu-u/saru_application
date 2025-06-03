package saru.com.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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

public class SignupActivity extends AppCompatActivity {
    TextInputEditText txtName;
    TextInputEditText txtEmail;
    TextInputEditText txtPassword;
    Button btnSignupComplete;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser != null){
//            Intent intent = new Intent(getApplicationContext(), Homepage.class);
//            startActivity(intent);
//            finish(); // Đóng LoginActivity
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo các trường nhập liệu
        txtName = findViewById(R.id.edtName);
        txtEmail = findViewById(R.id.edtEmail);
        txtPassword = findViewById(R.id.edtPassword);
        btnSignupComplete = findViewById(R.id.btn_complete_signup);
        progressBar = findViewById(R.id.progressBar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nút back
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Nút signup hoàn thành
        Button btnSignupComplete = findViewById(R.id.btn_complete_signup);
        if (btnSignupComplete != null) {
            btnSignupComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Hiển thị ProgressBar
                    progressBar.setVisibility(View.VISIBLE);
                    String name = String.valueOf(txtName.getText());
                    String email = String.valueOf(txtEmail.getText());
                    String password = String.valueOf(txtPassword.getText());

                    // Kiểm tra thông tin đầu vào
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(SignupActivity.this, "Enter Name Please", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Ẩn ProgressBar khi có lỗi
                        return;
                    }
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(SignupActivity.this, "Enter Email Please", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Ẩn ProgressBar khi có lỗi
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(SignupActivity.this, "Enter Password Please", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Ẩn ProgressBar khi có lỗi
                        return;
                    }

                    // Đăng ký tài khoản với Firebase
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE); // Ẩn ProgressBar sau khi hoàn thành

                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Account created.",
                                                Toast.LENGTH_SHORT).show();

                                        // Sau khi đăng ký thành công, chuyển hướng đến trang Homepage
                                        Intent intent = new Intent(SignupActivity.this, Homepage.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Nếu đăng ký thất bại, hiển thị lỗi
                                        Toast.makeText(SignupActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }


        // Nút login (TextView)
        TextView btnLogin = findViewById(R.id.btn_login);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Vùng faceid
        androidx.constraintlayout.widget.ConstraintLayout faceVerificationTriggerArea = findViewById(R.id.faceVerificationTriggerArea);
        if (faceVerificationTriggerArea != null) {
            faceVerificationTriggerArea.setOnClickListener(v -> {
                Intent intent = new Intent(SignupActivity.this, FaceAuthenticationActivity.class);
                startActivity(intent);
            });
        }
    }
}