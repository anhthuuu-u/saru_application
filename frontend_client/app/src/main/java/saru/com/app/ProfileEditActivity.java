package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        ImageView backIcon = findViewById(R.id.ic_back_arrow);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay về ProfileActivity
                finish();
            }
        });
        ImageView reloadBtn = findViewById(R.id.btn_reload);
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileEditActivity.this, ProfileUpdateCardActivity.class);
                startActivity(intent);
            }
        });

    }
}
