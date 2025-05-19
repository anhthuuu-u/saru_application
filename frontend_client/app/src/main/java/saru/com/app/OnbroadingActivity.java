package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OnbroadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onbroading);

        // Ánh xạ các View từ layout XML
        Button btnLogin = findViewById(R.id.btn_login);
        TextView btnSignup = findViewById(R.id.btn_signup);
        // Thiết lập sự kiện click cho nút Login
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(OnbroadingActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        // Thiết lập sự kiện click cho nút Signup (TextView hoạt động như một nút)
        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(OnbroadingActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets; // Trả về insets đã được xử lý (hoặc không thay đổi)
        });
    }
}
