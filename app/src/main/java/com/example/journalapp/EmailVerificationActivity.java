package com.example.journalapp;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class EmailVerificationActivity extends AppCompatActivity {

    MaterialButton btnVerify;
    TextView tvBackToLogin;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        firebaseAuth = FirebaseAuth.getInstance();

        btnVerify = findViewById(R.id.btnVerifyEmail);
        tvBackToLogin = findViewById(R.id.tvBackToLoginReset);

        tvBackToLogin.setOnClickListener(v -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null && !user.isEmailVerified()) {
                // If the email is not verified, delete the account
                user.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Account successfully deleted
                        Intent i = new Intent(EmailVerificationActivity.this, SignUpActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
                        startActivity(i);
                        finish();
                    } else {
                        // Handle failure
                        showSnackbar("Error deleting account. Please try again.");
                    }
                });
            } else {
                // If user is null or email is verified, just redirect
                Intent i = new Intent(EmailVerificationActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
                startActivity(i);
                finish();
            }
        });

        btnVerify.setOnClickListener(v -> {
            Objects.requireNonNull(firebaseAuth.getCurrentUser()).reload()
                 .addOnCompleteListener(task -> {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Intent intent = new Intent(EmailVerificationActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            showSnackbar("Please check your email and verify through the link");
                        }
                    });
        });

    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rootVerifyEmail), message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getColor(R.color.colorPrimary));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.show();
    }

}
