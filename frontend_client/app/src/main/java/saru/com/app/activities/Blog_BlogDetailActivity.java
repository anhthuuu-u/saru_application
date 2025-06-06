package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import saru.com.app.R;

public class Blog_BlogDetailActivity extends AppCompatActivity {
    ImageView imgDetailBlog_Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_blog_detail);
        addView();
        addEvents();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void addView()
    {
        imgDetailBlog_Back=findViewById(R.id.imgBlogList_Back);
    }
    void backToBlogList()
    {
        Intent intent=new Intent(Blog_BlogDetailActivity.this, Blog_ListActivity.class);
        startActivity(intent);
    }
    private void addEvents()
    {
        imgDetailBlog_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToBlogList();
            }
        });
    }
}