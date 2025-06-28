package saru.com.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import saru.com.adapters.TelephonyInforAdapter;
import saru.com.models.TelephonyInfor;

import saru.com.app.R;

public class TelephonyActivity extends AppCompatActivity {

    ListView lvTelephony;
    //ArrayAdapter<TelephonyInfor> adapter;
    TelephonyInforAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_telephony);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addViews();
        getAllContacts();

        addEvents();
    }

    private void addEvents() {
        lvTelephony.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TelephonyInfor ti=adapter.getItem(i);
                makeAPhoneCall(ti);
            }
        });
    }

    private void makeAPhoneCall(TelephonyInfor ti) {
        Uri uri=Uri.parse("tel:"+ti.getPhone());
        Intent intent=new Intent(Intent.ACTION_CALL);
        intent.setData(uri);
        startActivity(intent);
    }

    private void getAllContacts() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null,  null, null,  null);

        adapter.clear();

        while (cursor.moveToNext()){
            int nameIndex =cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            String name = cursor.getString(nameIndex); //Get Name
            int phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds. Phone.NUMBER);
            String phone = cursor.getString(phoneIndex); //Get Phone Number

            TelephonyInfor ti=new TelephonyInfor();
            ti.setName(name);
            ti.setPhone(phone);
            adapter.add(ti);
        }
        cursor.close();


    }

    private void addViews() {
        lvTelephony=findViewById(R.id.lvTelephonyInfor);
        //adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        adapter=new TelephonyInforAdapter(this,R.layout.item_telephony_infor);
        lvTelephony.setAdapter(adapter);
    }

    public void directCall(TelephonyInfor ti)
    {
        Uri uri=Uri.parse("tel:"+ti.getPhone());
        Intent intent=new Intent(Intent.ACTION_CALL);
        intent.setData(uri);
        startActivity(intent);
    }
    public void dialupCall(TelephonyInfor ti)
    {
        Uri uri=Uri.parse("tel:"+ti.getPhone());
        Intent intent=new Intent(Intent.ACTION_DIAL);
        intent.setData(uri);
        startActivity(intent);
    }
}