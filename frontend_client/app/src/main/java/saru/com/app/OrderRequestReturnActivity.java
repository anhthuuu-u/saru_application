package saru.com.app;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OrderRequestReturnActivity extends AppCompatActivity {

    private ImageView backArrow;
    private Button submitReturnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_returnrequest);

        // Ánh xạ view
        backArrow = findViewById(R.id.ic_back_arrow);
        submitReturnButton = findViewById(R.id.submit_return_button);

        // Bắt sự kiện click nút back
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay lại OrderDetailActivity
            }
        });

        // Bắt sự kiện click nút "Send request"
        submitReturnButton.setOnClickListener(new View.OnClickListener() {
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
        toastText.setText(getString(R.string.return_submit_success)); // chuỗi trong strings.xml

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
}