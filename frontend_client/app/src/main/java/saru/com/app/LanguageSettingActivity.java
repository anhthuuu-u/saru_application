package saru.com.app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class LanguageSettingActivity extends AppCompatActivity {
    LinearLayout opt_english;
    LinearLayout opt_vietnam;
    TextView txt_english;
    TextView txt_vietnam;
    ImageView english_check;
    ImageView vietnamese_check;
    Button btn_submit_language;
    String selectedLanguage = "vi"; // default selected
    static final String PREFS_NAME = "app_prefs";
    static final String KEY_LANGUAGE = "selected_language";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_setting);

        // Initialize views
        opt_english = findViewById(R.id.opt_english);
        opt_vietnam = findViewById(R.id.opt_vietnam);
        english_check = findViewById(R.id.english_check);
        vietnamese_check = findViewById(R.id.vietnamese_check);
        btn_submit_language = findViewById(R.id.btn_submit_language);
        txt_english = findViewById(R.id.txt_english);
        txt_vietnam = findViewById(R.id.txt_vietnam);

        // Load saved language if any
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedLanguage = prefs.getString(KEY_LANGUAGE, "vi");
        updateCheckmarks();

        opt_english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLanguage = "en";
                updateCheckmarks();
            }
        });

        txt_english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLanguage = "en";
                updateCheckmarks();
            }
        });

        opt_vietnam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLanguage = "vi";
                updateCheckmarks();
            }
        });

        txt_vietnam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLanguage = "vi";
                updateCheckmarks();
            }
        });

        btn_submit_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLanguagePreference(selectedLanguage);
                updateLocale(selectedLanguage);
                restartActivity();
            }
        });
        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LanguageSettingActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void updateCheckmarks() {
        if ("en".equals(selectedLanguage)) {
            english_check.setVisibility(View.VISIBLE);
            vietnamese_check.setVisibility(View.GONE);
        } else {
            english_check.setVisibility(View.GONE);
            vietnamese_check.setVisibility(View.VISIBLE);
        }
    }

    private void saveLanguagePreference(String languageCode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }
    private void updateLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
