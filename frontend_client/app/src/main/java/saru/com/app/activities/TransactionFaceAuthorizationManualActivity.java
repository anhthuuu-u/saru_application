package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import saru.com.app.R;

public class TransactionFaceAuthorizationManualActivity extends AppCompatActivity {
    private ImageView imgFaceError;
    private Button btnFaceAuthorConfirm;
    private Button btnFaceAuthorCancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_face_authorization_manual);
        addViews();
        addEvents();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addViews() {
        imgFaceError=findViewById(R.id.imgFaceError);
        btnFaceAuthorCancel=findViewById(R.id.btnFaceAuthorCancel);
        btnFaceAuthorConfirm=findViewById(R.id.btnFaceAuthorConfirm);
    }

    private void addEvents() {
        btnFaceAuthorConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTransactionAuthorizationCompletedActivity();
                finish();
            }
        });
    }

    public void onBackPressed(View view) {
        finish();
    }

    public void onConfirmClick(View view) {
    }
    void openTransactionAuthorizationCompletedActivity() {
        Intent intent = new Intent(TransactionFaceAuthorizationManualActivity.this, TransactionFaceAuthorizationCompletedActivity.class);
        startActivity(intent);
    }
}