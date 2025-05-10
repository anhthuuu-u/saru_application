package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OrderReviewActivity extends AppCompatActivity {

    ImageView icBackArrow;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_review);

        icBackArrow = findViewById(R.id.ic_back_arrow);
        submitButton = findViewById(R.id.submit_review_button);

        // Quay về trang OrderDetailActivity
        icBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // hoặc dùng Intent nếu cần chuyển mới
            }
        });

        // Xử lý khi bấm nút Gửi đánh giá
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomToast();
            }
        });
    }

    private void showCustomToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView toastText = layout.findViewById(R.id.tv_toast_message);
        toastText.setText(getString(R.string.review_submit_success)); // chuỗi từ strings.xml

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100); // gần phía trên
        toast.show();
    }
}
