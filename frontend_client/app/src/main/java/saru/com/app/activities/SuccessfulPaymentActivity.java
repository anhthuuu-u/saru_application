package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import saru.com.app.R;

public class SuccessfulPaymentActivity extends AppCompatActivity {
    private TextView txtProductName1;
    private Button btnAddToCart1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful_payment);
        addViews();
        addEvents();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addViews() {
    }

    private void addEvents() {
    }

    public void onBackPressed(View view) {
        Intent intent=new Intent(this,ProductCart.class);
        startActivity(intent);

    }

    /**
     * 1. Xử lý sự kiện khi người dùng nhấn nút xem chi tiết đơn hàng
     */
    public void do_view_order_detail(View view) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        startActivity(intent);
    }

    /**
     * 2. Xử lý sự kiện khi người dùng nhấn vào biểu tượng giỏ hàng
     */
    public void do_cart(View view) {
        Intent intent = new Intent(this, ProductCart.class);
        startActivity(intent);
    }

    /**
     * 3. Xử lý sự kiện khi người dùng nhấn vào ảnh hoặc tên sản phẩm
     */
    public void do_view_product_detail(View view) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        startActivity(intent);
    }

    /**
     * 4. Xử lý sự kiện khi người dùng nhấn nút thêm vào giỏ hàng
     */
    public void do_add_to_cart(View view) {
        String productName = getString(R.string.title_trans_product_name);
        String message = getString(R.string.title_the_add_to_cart) + productName + getString(R.string.title_add_to_cart_message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 5. Xử lý sự kiện khi người dùng nhấn nút so sánh sản phẩm
     */
    public void do_compare_product(View view) {
        Intent intent = new Intent(this, ProductComparison.class);
        startActivity(intent);
    }
}