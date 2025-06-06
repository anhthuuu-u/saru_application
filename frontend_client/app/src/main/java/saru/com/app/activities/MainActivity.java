package saru.com.app.activities;

import android.content.Intent;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import saru.com.app.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseAuth auth;
    FirebaseUser user;


    //Bottom Navigation
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            // If user is not logged in, redirect to LoginActivity
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity
        }
        else {
            // User is logged in, you can proceed with your app logic
            // For example, you can load user data or show a welcome message
            Toast.makeText(this, "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();
        }
//        // Load the map fragment
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map_container);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
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
                // Mở HomeActivity
                Intent intent = new Intent(MainActivity.this, Homepage.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.menu_product) {
                // Mở ProductActivity hoặc làm gì đó
                Intent intent = new Intent(MainActivity.this, Products.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.menu_compare) {
                // Mở CompareActivity
                Intent intent = new Intent(MainActivity.this, ProductComparison.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.menu_account) {
                // Mở AccountActivity
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
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