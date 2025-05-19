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

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

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
            btnSignupComplete.setOnClickListener(v -> {
                // TODO: Thêm validate form trước khi chuyển
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
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
