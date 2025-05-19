package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FaceAuthenticationActivity extends AppCompatActivity {

    private View initialStateContainer;
    private View resultSuccessStateContainer;
    private View resultFailStateContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_authentication);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Tham chiếu các layout trạng thái
        initialStateContainer = findViewById(R.id.initialStateContainer);
        resultSuccessStateContainer = findViewById(R.id.resultSuccessStateContainer);
        resultFailStateContainer = findViewById(R.id.resultFailStateContainer);

        // Xử lý nút Back
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Nút Back to Signup
        Button btnBackToSignup = findViewById(R.id.resultSecondaryButton);
        btnBackToSignup.setOnClickListener(v -> {
            Intent intent = new Intent(FaceAuthenticationActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        // Nút Scan Again
        Button btnScanAgain = findViewById(R.id.resultPrimaryButton);
        btnScanAgain.setOnClickListener(v -> {
            // Reset UI về trạng thái ban đầu
            initialStateContainer.setVisibility(View.VISIBLE);
            resultFailStateContainer.setVisibility(View.GONE);
            resultSuccessStateContainer.setVisibility(View.GONE);
        });
    }
}

