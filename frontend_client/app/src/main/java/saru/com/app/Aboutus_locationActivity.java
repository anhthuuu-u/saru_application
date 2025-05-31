package saru.com.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Aboutus_locationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMapLaoCai;
    private GoogleMap mMapHCM;

    TextView txtAboutus_SaruWine;
    TextView txtAboutus_StoreLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aboutus_location);
        addView();
        addEvents();

        // Xử lý nút quay lại
        ImageView backButton = findViewById(R.id.imgAboutUs_Back);
        backButton.setOnClickListener(v -> finish());

        // Khởi tạo SupportMapFragment cho Lào Cai
        SupportMapFragment mapFragmentLaoCai = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_laocai);
        if (mapFragmentLaoCai != null) {
            mapFragmentLaoCai.getMapAsync(this);
        }

        // Khởi tạo SupportMapFragment cho HCM
        SupportMapFragment mapFragmentHCM = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_hcm);
        if (mapFragmentHCM != null) {
            mapFragmentHCM.getMapAsync(this);
        }

        // Xử lý nhấn vào CardView Lào Cai
        CardView cardViewLaoCai = findViewById(R.id.cardView_laocai);
        cardViewLaoCai.setOnClickListener(v -> {
            String geoUri = "geo:22.4857,103.9751?q=22.4857,103.9751(Lào+Cai+Branch)";
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=22.4857,103.9751"));
                startActivity(browserIntent);
            }
        });

        // Xử lý nhấn vào CardView HCM
        CardView cardViewHCM = findViewById(R.id.cardView_hcm);
        cardViewHCM.setOnClickListener(v -> {
            String geoUri = "geo:10.7769,106.7009?q=10.7769,106.7009(HCM+Branch)";
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=10.7769,106.7009"));
                startActivity(browserIntent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override // Đánh dấu phương thức này là override từ interface OnMapReadyCallback
    public void onMapReady(GoogleMap googleMap) {
        // Kiểm tra map nào đã sẵn sàng
        if (mMapLaoCai == null && getSupportFragmentManager().findFragmentById(R.id.map_laocai) != null) {
            mMapLaoCai = googleMap;
            LatLng laoCai = new LatLng(22.4857, 103.9751); // Tọa độ Lào Cai
            mMapLaoCai.addMarker(new MarkerOptions().position(laoCai).title("Lào Cai Branch"));
            mMapLaoCai.moveCamera(CameraUpdateFactory.newLatLngZoom(laoCai, 15));
        } else if (mMapHCM == null && getSupportFragmentManager().findFragmentById(R.id.map_hcm) != null) {
            mMapHCM = googleMap;
            LatLng hcm = new LatLng(10.7769, 106.7009); // Tọa độ TP.HCM
            mMapHCM.addMarker(new MarkerOptions().position(hcm).title("HCM Branch"));
            mMapHCM.moveCamera(CameraUpdateFactory.newLatLngZoom(hcm, 15));
        }
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