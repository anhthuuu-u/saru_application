package saru.com.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import saru.com.app.R;

public class Aboutus_SARUActivity extends AppCompatActivity {
    TextView txtAboutus_SaruWine;
    TextView txtAboutus_StoreLocation;
    ImageView imgAboutUs_Back;
    VideoView videoView;
    private RecyclerView recyclerViewAboutUs;
    private FirebaseFirestore db;
    private List<AboutUsItem> aboutUsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aboutus);
        addView();
        addEvents();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        aboutUsList = new ArrayList<>();

        // Thiết lập RecyclerView
        recyclerViewAboutUs = findViewById(R.id.recyclerViewAboutUs);
        recyclerViewAboutUs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewAboutUs.setAdapter(new AboutUsAdapter());

        // Lấy dữ liệu từ Firestore
        loadAboutUsData();

        // Tải video lên
        videoView = findViewById(R.id.videoView);
        String videoUrl = "https://ik.imagekit.io/saruApp/Saru%20App/videointro.mp4"; // 🔁 Đổi thành link video thật

        Uri uri = Uri.parse(videoUrl);
        videoView.setVideoURI(uri);

        // Tạo controller cho phép play/pause tua video
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.requestFocus();
        videoView.start(); // Phát tự động

    }
    private void addView() {
        txtAboutus_SaruWine=findViewById(R.id.txtAboutus_SaruWine);
        txtAboutus_StoreLocation=findViewById(R.id.txtAboutus_StoreLocation);
        imgAboutUs_Back=findViewById(R.id.imgAboutUs_Back);

    }

    private void addEvents() {
        txtAboutus_SaruWine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_SaruWine();
            }
        });
        txtAboutus_StoreLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_StoreLocation();
            }
        });
        imgAboutUs_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileActivity();
            }
        });
    }

    void openAboutUs_SaruWine()
    {
        Intent intent=new Intent(Aboutus_SARUActivity.this, Aboutus_SARUActivity.class);
        startActivity(intent);
    }

    void openAboutUs_StoreLocation()
    {
        Intent intent=new Intent(Aboutus_SARUActivity.this, Aboutus_locationActivity.class);
        startActivity(intent);
    }

    void openProfileActivity()
    {
        Intent intent=new Intent(Aboutus_SARUActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
    private void loadAboutUsData() {
        db.collection("aboutus")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    aboutUsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String titlePosition = document.getString("titlePosition");
                        String founderImageUrl = document.getString("founderImage");
                        if (titlePosition != null && founderImageUrl != null) {
                            aboutUsList.add(new AboutUsItem(titlePosition, founderImageUrl));
                        }
                    }
                    recyclerViewAboutUs.getAdapter().notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi nếu cần
                });
    }

    // Model cho dữ liệu
    private static class AboutUsItem {
        String titlePosition;
        String founderImageUrl;

        AboutUsItem(String titlePosition, String founderImageUrl) {
            this.titlePosition = titlePosition;
            this.founderImageUrl = founderImageUrl;
        }
    }

    // Adapter cho RecyclerView
    private class AboutUsAdapter extends RecyclerView.Adapter<AboutUsAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_aboutus, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AboutUsItem item = aboutUsList.get(position);
            holder.txtPosition.setText(item.titlePosition);
            Glide.with(holder.itemView.getContext())
                    .load(item.founderImageUrl)
                    .into(holder.imgFounder);
        }

        @Override
        public int getItemCount() {
            return aboutUsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgFounder;
            TextView txtPosition;

            ViewHolder(View itemView) {
                super(itemView);
                imgFounder = itemView.findViewById(R.id.imgFounder);
                txtPosition = itemView.findViewById(R.id.txtPosition);
            }
        }
    }
}