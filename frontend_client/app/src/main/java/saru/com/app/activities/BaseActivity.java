package saru.com.app.activities;

import android.content.Intent;
import android.util.Log; // For logging
import android.view.MenuItem; // Import MenuItem
import androidx.annotation.IdRes; // Import IdRes
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView; // Correct import

import saru.com.app.R;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    // Abstract method to be implemented by subclasses to indicate which menu item is selected
    @IdRes // Ensures that the returned int is a resource ID
    protected abstract int getSelectedMenuItemId();

    protected void setupBottomNavigation() {
        // Ensure your layout file for the subclassing activity includes a BottomNavigationView
        // with the ID "bottom_nav"
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        if (bottomNav == null) {
            // Log an error or handle the case where the BottomNavigationView is not found.
            // This prevents a NullPointerException if the layout doesn't include R.id.bottom_nav.
            Log.e(TAG, "BottomNavigationView with ID R.id.bottom_nav not found in the current layout.");
            return;
        }

        // Use setOnItemSelectedListener for Material Components 1.3.0 and later
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();
                // Using if-else if for clarity, can also use switch
                if (itemId == R.id.menu_home) {
                    if (!(BaseActivity.this instanceof Homepage)) { // Avoid restarting if already on Homepage
                        startActivity(new Intent(BaseActivity.this, Homepage.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));
                    }
                    return true;
                } else if (itemId == R.id.menu_product) {
                    if (!(BaseActivity.this instanceof Products)) {
                        startActivity(new Intent(BaseActivity.this, Products.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));
                    }
                    return true;
                } else if (itemId == R.id.menu_compare) {
                    if (!(BaseActivity.this instanceof ProductComparison)) {
                        startActivity(new Intent(BaseActivity.this, ProductComparison.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));
                    }
                    return true;
                } else if (itemId == R.id.menu_account) {
                    if (!(BaseActivity.this instanceof ProfileActivity)) {
                        startActivity(new Intent(BaseActivity.this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));
                    }
                    return true;
                }
                return false;
            }
        });

        // Set the selected item based on the activity
        // Ensure getSelectedMenuItemId() returns a valid menu item ID present in your menu resource
        if (getSelectedMenuItemId() != 0) { // Check if a valid ID is provided
            bottomNav.setSelectedItemId(getSelectedMenuItemId());
        }
    }
}