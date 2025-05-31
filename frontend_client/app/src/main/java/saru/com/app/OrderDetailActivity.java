package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetail);

        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnWriteReview = findViewById(R.id.btn_write_review);
        btnWriteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetailActivity.this, OrderReviewActivity.class);
                startActivity(intent);
            }
        });

        Button btnRequestReturn = findViewById(R.id.btn_request_return);
        btnRequestReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetailActivity.this, OrderRequestReturnActivity.class);
                startActivity(intent);
            }
        });

        Button btnCancelOrder = findViewById(R.id.btn_cancel_order);
        btnCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetailActivity.this, OrderCancelActivity.class);
                startActivity(intent);
            }
        });
    }
}
