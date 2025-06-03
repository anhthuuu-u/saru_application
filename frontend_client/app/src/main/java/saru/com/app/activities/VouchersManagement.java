package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import saru.com.app.R;
import saru.com.app.connectors.VoucherAdapter;
import saru.com.app.models.VoucherList;

public class VouchersManagement extends AppCompatActivity {

    private RecyclerView recyclerViewVouchers;
    private VoucherAdapter voucherAdapter;
    private VoucherList voucherList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vouchers_management);

        // Khởi tạo RecyclerView
        recyclerViewVouchers = findViewById(R.id.recycler_view_vouchers);

        // Kiểm tra null để tránh lỗi
        if (recyclerViewVouchers == null) {
            throw new IllegalStateException("RecyclerView with ID recycler_view_vouchers not found in layout");
        }

        // Khởi tạo VoucherList
        voucherList = new VoucherList();

        // Khởi tạo VoucherAdapter
        voucherAdapter = new VoucherAdapter(voucherList.getVouchers());

        // Cài đặt RecyclerView
        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVouchers.setAdapter(voucherAdapter);

        // Thông báo adapter cập nhật dữ liệu
        voucherAdapter.notifyDataSetChanged();
        // Xử lý nút btn_back_arrow
        ImageButton btnBackArrow = findViewById(R.id.btn_back_arrow);
        if (btnBackArrow != null) {
            btnBackArrow.setOnClickListener(v -> {
                Intent intent = new Intent(VouchersManagement.this, Homepage.class);
                startActivity(intent);
                finish(); // Kết thúc activity Products để không quay lại khi nhấn back
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Xử lý window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}