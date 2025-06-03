package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import saru.com.app.R;

public class TransactionFaceAuthorizationActivity extends AppCompatActivity {
    private ImageView imgFaceAuthorization;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_face_authorization);
        addViews();
        addEvents();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addViews() {

        imgFaceAuthorization=findViewById(R.id.imgFaceAuthorization);
    }

    private void addEvents() {
        imgFaceAuthorization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTransactionFaceAuthorizationManualActivity();
            }
        });
    }

    public void onBackPressed(View view) {
        finish();
    }
    void  openTransactionFaceAuthorizationManualActivity(){
        Intent intent=new Intent(TransactionFaceAuthorizationActivity.this, TransactionFaceAuthorizationManualActivity.class);
        startActivity(intent);
        finish();
    }
}