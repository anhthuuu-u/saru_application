package saru.com.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.* ;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity  implements OnMapReadyCallback {




    //Bottom Navigation
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Load the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//Bottom Navigation

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(item -> {
           int id = item.getItemId();

           if (id == R.id.menu_home) {
               Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.menu_product ){
               Toast.makeText(MainActivity.this, "Product", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.menu_compare){
               Toast.makeText(MainActivity.this, "Compare", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.menu_account) {
               Toast.makeText(MainActivity.this, "Account", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    public void onMapReady(GoogleMap googleMap) {
        // Define the location (e.g., Lào Cai, Vietnam)
        LatLng location = new LatLng(22.338086, 104.003602);

        // Add a marker and move the camera
        googleMap.addMarker(new MarkerOptions().position(location).title("Cửa hàng Saru Tây Bắc"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }
}