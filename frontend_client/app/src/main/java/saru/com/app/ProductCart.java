package saru.com.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import saru.com.app.connectors.CartAdapter;
import saru.com.app.models.CartItem;
import saru.com.app.models.ListCartItems;
import saru.com.app.models.Product;

public class ProductCart extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private ListCartItems listCartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_cart);

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ListCartItems
        listCartItems = new ListCartItems();

        // Add sample data
        listCartItems.addItem(new CartItem(new Product("Peach Wine", "200.000đ", "Brand A", "In Stock", "12%", "750ml", "Fruit Wine", "Peach", 4.5f, "Sweet wine"), 1));
        listCartItems.addItem(new CartItem(new Product("Rice Wine", "150.000đ", "Brand B", "In Stock", "15%", "500ml", "Traditional Wine", "Rice", 4.0f, "Strong flavor"), 2));
        listCartItems.addItem(new CartItem(new Product("Apple Wine", "180.000đ", "Brand C", "In Stock", "10%", "700ml", "Fruit Wine", "Apple", 4.2f, "Light taste"), 1));

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.cart_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and set adapter
        cartAdapter = new CartAdapter(this, listCartItems.getCartItems(), () -> updateTotalPrice());
        recyclerView.setAdapter(cartAdapter);

        // Handle back button
        ImageButton btnBack = findViewById(R.id.btn_back_arrow);
        btnBack.setOnClickListener(v -> finish());

        // Handle select all checkbox
        CheckBox selectAllCheckbox = findViewById(R.id.select_all_checkbox);
        selectAllCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listCartItems.setAllSelected(isChecked);
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
        });

        // Handle payment button
        Button paymentButton = findViewById(R.id.payment_button);
        paymentButton.setOnClickListener(v -> {
            double total = listCartItems.calculateTotalPrice();
            Toast.makeText(this, "Total Payment: " + String.format("%.0f", total) + "đ", Toast.LENGTH_SHORT).show();
            // TODO: Implement payment logic
        });

        // Update total price initially
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        TextView totalAmountText = findViewById(R.id.total_amount_text);
        double total = listCartItems.calculateTotalPrice();
        totalAmountText.setText("Tổng cộng: " + String.format("%.0f", total) + "đ");
    }
}