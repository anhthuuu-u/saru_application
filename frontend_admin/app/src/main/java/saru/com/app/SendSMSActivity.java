package saru.com.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import saru.com.models.TelephonyInfor;

import saru.com.app.R;

public class SendSMSActivity extends AppCompatActivity {

    TextView txtTelephonyName;
    TextView txtTelephonyNumber;
    EditText edtBody;
    ImageView imgSendSms1;
    ImageView imgSendSms2;

    TelephonyInfor ti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_smsactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addViews();
        addEvents();
    }

    private void addEvents() {
        imgSendSms1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ko quan tâm gửi SMS thành công hay thất bại
                sendSms(ti,edtBody.getText().toString());
            }
        });

        imgSendSms2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gửi SMS và quan tâm nó đã gửi thành công hay chưa
                sendSmsPendingIntent(ti,edtBody.getText().toString());
            }
        });
    }

    private void addViews() {
        txtTelephonyName=findViewById(R.id.txtTelephonyName);
        txtTelephonyNumber=findViewById(R.id.txtTelephonyNumber);
        edtBody=findViewById(R.id.edtBody);
        imgSendSms1=findViewById(R.id.imgSendsms1);
        imgSendSms2=findViewById(R.id.imgSendsms2);

        Intent intent=getIntent();
        ti= (TelephonyInfor) intent.getSerializableExtra("TI");
        if(ti!=null)
        {
            txtTelephonyName.setText(ti.getName());
            txtTelephonyNumber.setText(ti.getPhone());
        }
    }

    public  void sendSms(TelephonyInfor ti, String content)
    {
        final SmsManager sms = SmsManager.getDefault();

        sms.sendTextMessage( ti.getPhone(), null, content, null, null);
        Toast.makeText(SendSMSActivity.this, "Đã gửi tin nhắn tới "+ti.getPhone(),
                Toast.LENGTH_LONG).show();
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public  void sendSmsPendingIntent(TelephonyInfor ti, String content)
    {
        //lấy mặc định SmsManager
        final SmsManager sms = SmsManager.getDefault();
        Intent msgSent = new Intent("ACTION_MSG_SENT");
//Khai báo pendingintent để kiểm tra kết quả
        final PendingIntent pendingMsgSent =
                PendingIntent.getBroadcast(this, 0, msgSent, PendingIntent.FLAG_IMMUTABLE);
        registerReceiver(new BroadcastReceiver() {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            public void onReceive(Context context, Intent intent) {
                int result = getResultCode();
                String msg="Send OK";
                if (result != Activity.RESULT_OK) {
                    msg="Send failed";
                }
                Toast.makeText(SendSMSActivity.this, msg,
                        Toast.LENGTH_LONG).show();
            }
        }, new IntentFilter("ACTION_MSG_SENT"));
//Gọi hàm gửi tin nhắn đi
        sms.sendTextMessage(ti.getPhone(), null, content,
                pendingMsgSent, null);
    }
}