package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import saru.com.app.R;

public class ForgotPasswordOtpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password_otp);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        Button btn_verify_otp = findViewById(R.id.btn_verify_otp);
        TextView btn_resend_otp = findViewById(R.id.btn_resend_otp);

        // Setup OTP input auto-move
        setupOtpInputs();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Get phone number from intent
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (phoneNumber != null) {
            sendVerificationCode(phoneNumber);
        } else {
            Toast.makeText(this, "Phone number not provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Verify OTP
        btn_verify_otp.setOnClickListener(v -> {
            String otp = getOtpCode();
            if (otp.length() != 6) {
                Toast.makeText(ForgotPasswordOtpActivity.this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            } else {
                verifyCode(otp);
            }
        });

        // Resend OTP
        btn_resend_otp.setOnClickListener(v -> {
            if (resendingToken != null) {
                resendVerificationCode(phoneNumber, resendingToken);
            } else {
                Toast.makeText(this, "Cannot resend OTP yet", Toast.LENGTH_SHORT).show();
            }
        });

        // Signup link
        TextView btnSignup = findViewById(R.id.btn_signup);
        if (btnSignup != null) {
            btnSignup.setOnClickListener(v -> {
                Intent intent = new Intent(ForgotPasswordOtpActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupOtpInputs() {
        EditText[] otpFields = {otp1, otp2, otp3, otp4, otp5, otp6};
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }
            });
        }
    }

    private String getOtpCode() {
        return otp1.getText().toString() +
                otp2.getText().toString() +
                otp3.getText().toString() +
                otp4.getText().toString() +
                otp5.getText().toString() +
                otp6.getText().toString();
    }

    private void setOtpCode(String code) {
        if (code.length() == 6) {
            otp1.setText(String.valueOf(code.charAt(0)));
            otp2.setText(String.valueOf(code.charAt(1)));
            otp3.setText(String.valueOf(code.charAt(2)));
            otp4.setText(String.valueOf(code.charAt(3)));
            otp5.setText(String.valueOf(code.charAt(4)));
            otp6.setText(String.valueOf(code.charAt(5)));
        }
    }

    private void sendVerificationCode(String number) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallBack)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String number, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallBack)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        Toast.makeText(this, "Resending OTP", Toast.LENGTH_SHORT).show();
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(s, token);
                    verificationId = s;
                    resendingToken = token;
                    Toast.makeText(ForgotPasswordOtpActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        setOtpCode(code);
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(ForgotPasswordOtpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordOtpActivity.this, "Verification successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotPasswordOtpActivity.this, ForgotPasswordCreateActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ForgotPasswordOtpActivity.this, "Verification failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}