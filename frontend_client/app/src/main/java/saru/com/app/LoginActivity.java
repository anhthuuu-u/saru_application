package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nút back custom
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Nút login
        Button btnLogin = findViewById(R.id.btn_complete_login);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                // TODO: Thêm validate email, password trước khi navigate

                // Đại đại navigate qua MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Đóng LoginActivity
            });
        }

        // Nút forgot password (TextView)
        TextView btnForgotPassword = findViewById(R.id.btn_forgot_password);
        if (btnForgotPassword != null) {
            btnForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        // Nút signup (TextView)
        TextView btnSignup = findViewById(R.id.btn_signup);
        if (btnSignup != null) {
            btnSignup.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }

        // TextView đăng ký bên cạnh (textView4)
        TextView textViewSignup = findViewById(R.id.txtBlogList_SeemoreInfo);
        if (textViewSignup != null) {
            textViewSignup.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }
    }
}
