package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import saru.com.adapters.DashboardAdapter;
import saru.com.models.DashboardItem;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvDashboard;
    private Button btnLogout;
    private List<DashboardItem> dashboardItems;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        initializeViews();
        setupDashboard();
        setupLogout();
    }

    private void initializeViews() {
        rvDashboard = findViewById(R.id.rvDashboard);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupDashboard() {
        dashboardItems = new ArrayList<>();
        dashboardItems.add(new DashboardItem("Product Management", R.mipmap.ic_product));
        dashboardItems.add(new DashboardItem("Order Management", R.mipmap.ic_order));
        dashboardItems.add(new DashboardItem("Customer Management", R.mipmap.ic_customer));
        dashboardItems.add(new DashboardItem("Blogs", R.mipmap.ic_blog));
        dashboardItems.add(new DashboardItem("Promotions", R.mipmap.ic_promotion));
        dashboardItems.add(new DashboardItem("FAQs", R.mipmap.ic_faq));
        dashboardItems.add(new DashboardItem("Customer Support", R.mipmap.ic_support));
        dashboardItems.add(new DashboardItem("Feedback Management", R.mipmap.ic_feedback));

        DashboardAdapter adapter = new DashboardAdapter(dashboardItems, position -> {
            String title = dashboardItems.get(position).getTitle();
            switch (title) {
                case "Product Management":
                    startActivity(new Intent(this, ProductListActivity.class));
                    break;
                case "Order Management":
                    startActivity(new Intent(this, OrderManagementActivity.class));
                    break;
                case "Customer Management":
                    startActivity(new Intent(this, CustomerManagementActivity.class));
                    break;
                case "Blogs":
                    startActivity(new Intent(this, BlogsManagementActivity.class));
                    break;
                case "Promotions":
                    startActivity(new Intent(this, PromotionsManagementActivity.class));
                    break;
                case "FAQs":
                    startActivity(new Intent(this, FAQsManagementActivity.class));
                    break;

                default:
                    Toast.makeText(this, "Feature: " + title + " not implemented yet", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        rvDashboard.setLayoutManager(new LinearLayoutManager(this));
        rvDashboard.setAdapter(adapter);
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}