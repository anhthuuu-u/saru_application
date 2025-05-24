package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Blog_ListActivity extends AppCompatActivity {
    TextView txtBlog_CategoryNameInfo;
    TextView txtBlogList_SeemoreInfo;
    ImageView imgBlog_CategoryNameInfo;

    TextView txtBlog_BlogTitle;
    TextView txtBlog_BlogContent;
    TextView txtBlogList_SeemoreBlog1;
    ImageView imgBlog_Blog1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_list);
        addView();
        addEvents();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void addView() {
        imgBlog_CategoryNameInfo = findViewById(R.id.imgBlog_CategoryNameInfo);
        txtBlog_CategoryNameInfo = findViewById(R.id.txtBlog_CategoryNameInfo);
        txtBlogList_SeemoreInfo = findViewById(R.id.txtBlogList_SeemoreInfo);

        imgBlog_Blog1 = findViewById(R.id.imgBlog_Blog1);
        txtBlog_BlogTitle = findViewById(R.id.txtBlog_BlogTitle);
        txtBlog_BlogContent = findViewById(R.id.txtBlog_BlogContent);
        txtBlogList_SeemoreBlog1 = findViewById(R.id.txtBlogList_SeemoreBlog1);

    }
    void openBlogDetail()
    {
        Intent intent=new Intent(Blog_ListActivity.this, Blog_BlogDetailActivity.class);
        startActivity(intent);
    }
    void openEachCatalog()
    {
        Intent intent=new Intent(Blog_ListActivity.this, Blog_EachCatalogActivity.class);
        startActivity(intent);
    }
    private void addEvents()
    {
        imgBlog_CategoryNameInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEachCatalog();
            }
        });

        txtBlog_CategoryNameInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEachCatalog();
            }
        });

        txtBlogList_SeemoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEachCatalog();
            }
        });

        txtBlog_BlogTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBlogDetail();
            }
        });
        imgBlog_Blog1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBlogDetail();
            }
        });
        txtBlog_BlogContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBlogDetail();
            }
        });
        txtBlogList_SeemoreBlog1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBlogDetail();
            }
        });

    }
}