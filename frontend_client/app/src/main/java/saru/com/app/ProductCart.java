package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

import saru.com.app.connectors.CartAdapter;
import saru.com.app.models.CartItem;
import saru.com.app.models.ListCartItems;
import saru.com.app.models.Product;

public class ProductCart extends AppCompatActivity {
    private ListCartItems listCartItems;
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listCartItems = new ListCartItems();
        List<CartItem> sampleItems = getSampleCartItems();
        Log.d("ProductCart", "Sample items size: " + (sampleItems != null ? sampleItems.size() : "null"));
        if (sampleItems != null) {
            for (CartItem item : sampleItems) {
                listCartItems.addItem(item); // Sử dụng addItem thay vì addAll trên bản sao
            }
        } else {
            Log.e("ProductCart", "getSampleCartItems() returned null");
        }
        Log.d("ProductCart", "Cart items size after adding: " + listCartItems.getCartItems().size());

        recyclerView = findViewById(R.id.cart_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, listCartItems, this::updateTotalPrice);
        recyclerView.setAdapter(cartAdapter);
        Log.d("ProductCart", "Adapter item count: " + cartAdapter.getItemCount());

        ImageButton btnBack = findViewById(R.id.btn_back_arrow);
        btnBack.setOnClickListener(v -> finish());

        CheckBox selectAllCheckbox = findViewById(R.id.select_all_checkbox);
        selectAllCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listCartItems.setAllSelected(isChecked);
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
        });

        Button paymentButton = findViewById(R.id.payment_button);
        paymentButton.setOnClickListener(v -> {
            double total = listCartItems.calculateTotalPrice();
            Toast.makeText(this, getString(R.string.product_cart_payment_toast_label) + " " + String.format("%.0f", total) + getString(R.string.product_cart_currency), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProductCart.this, TransactionCheckoutActivity.class);
            startActivity(intent);
        });

        // Thêm sự kiện click cho txtDeleteAll
        TextView txtDeleteAll = findViewById(R.id.txtProductCart_DeleteAll);
        txtDeleteAll.setOnClickListener(v -> {
            List<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : listCartItems.getCartItems()) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }
            for (CartItem item : selectedItems) {
                int position = listCartItems.getCartItems().indexOf(item);
                if (position != -1) {
                    listCartItems.removeItem(position);
                    cartAdapter.notifyItemRemoved(position);
                }
            }
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
            Toast.makeText(this, getString(R.string.delete_all_noti), Toast.LENGTH_SHORT).show();
        });
        // Thêm sự kiện click cho back_to_products_button
        Button backToProductsButton = findViewById(R.id.back_to_products_button);
        backToProductsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductCart.this, Products.class);
            startActivity(intent);
            finish(); // Đóng ProductCart activity
        });

        updateTotalPrice();
    }

    private List<CartItem> getSampleCartItems() {
        List<CartItem> sampleItems = new ArrayList<>();
        try {
            sampleItems.add(new CartItem("Peach Wine", 200000, 1));
            sampleItems.add(new CartItem("Rice Wine", 150000, 2));
            sampleItems.add(new CartItem("Apple Wine", 180000, 1));
            sampleItems.add(new CartItem("Apricot Wine", 220000, 1));
            sampleItems.add(new CartItem("Nang Mo Wine", 220000, 1));
            sampleItems.add(new CartItem("Hoang Su Phi Blood Plum Wine", 392000, 1));
            sampleItems.add(new CartItem("Hoang Su Phi Blood Plum Wine", 392000, 1));
            sampleItems.add(new CartItem("Hoang Su Phi Blood Plum Wine", 392000, 1));

            Log.d("ProductCart", "Added 3 sample items to getSampleCartItems");
        } catch (Exception e) {
            Log.e("ProductCart", "Error adding sample items: " + e.getMessage());
        }
        return sampleItems;
    }

    private void updateTotalPrice() {
        double total = listCartItems.calculateTotalPrice();
        TextView totalAmountText = findViewById(R.id.total_amount_text);
        totalAmountText.setText(getString(R.string.product_cart_total_amount_label) + " " + String.format("%.0f", total) + getString(R.string.product_cart_currency));
    }
}