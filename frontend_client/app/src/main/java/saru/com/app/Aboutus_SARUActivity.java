package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Aboutus_SARUActivity extends AppCompatActivity {
    TextView textViewStoreLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aboutus);

        // Tìm TextView Store Location
        TextView textViewStoreLocation = findViewById(R.id.txtAboutus_StoreLocation);

        // Xử lý sự kiện nhấn TextView
        textViewStoreLocation.setOnClickListener(v -> {
            Intent intent = new Intent(Aboutus_SARUActivity.this, Aboutus_locationActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}