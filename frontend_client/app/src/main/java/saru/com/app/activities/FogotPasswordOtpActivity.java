package saru.com.app.activities;

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

import saru.com.app.R;

public class FogotPasswordOtpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fogot_password_otp);

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

        // Nút verify OTP
        Button btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        if (btnVerifyOtp != null) {
            btnVerifyOtp.setOnClickListener(v -> {
                Intent intent = new Intent(FogotPasswordOtpActivity.this, FogotPasswordCreateActivity.class);
                startActivity(intent);
            });
        }

        // Nút resend code (TextView)
        TextView btnResendOtp = findViewById(R.id.btn_resend_otp);
        if (btnResendOtp != null) {
            btnResendOtp.setOnClickListener(v -> {
                // TODO: Xử lý gửi lại mã OTP
            });
        }

        // Nút signup (TextView) - nếu bạn muốn liên kết đăng ký ở đây
        TextView btnSignup = findViewById(R.id.btn_signup);
        if (btnSignup != null) {
            btnSignup.setOnClickListener(v -> {
                Intent intent = new Intent(FogotPasswordOtpActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }
    }
}
