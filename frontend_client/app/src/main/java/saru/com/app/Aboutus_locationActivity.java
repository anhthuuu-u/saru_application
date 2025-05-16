package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Aboutus_locationActivity extends AppCompatActivity {

    TextView txtAboutus_SaruWine;
    TextView txtAboutus_StoreLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aboutus_location);
        addView();
        addEvents();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addView() {
        txtAboutus_SaruWine=findViewById(R.id.txtAboutus_SaruWine);
        txtAboutus_StoreLocation=findViewById(R.id.txtAboutus_StoreLocation);

    }

    private void addEvents() {
        txtAboutus_SaruWine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_SaruWine();
            }
        });
        txtAboutus_StoreLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_StoreLocation();
            }
        });
    }

    void openAboutUs_SaruWine()
    {
        Intent intent=new Intent(Aboutus_locationActivity.this, Aboutus_SARUActivity.class);
        startActivity(intent);
    }

    void openAboutUs_StoreLocation()
    {
        Intent intent=new Intent(Aboutus_locationActivity.this, Aboutus_locationActivity.class);
        startActivity(intent);
    }
}