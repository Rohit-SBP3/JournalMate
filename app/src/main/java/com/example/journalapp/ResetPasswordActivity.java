package com.example.journalapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    TextInputEditText edtEmail;
    MaterialButton btnResetPassword;
    FirebaseAuth firebaseAuth;

    TextView tvBackTOLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        edtEmail = findViewById(R.id.etResetEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackTOLogin = findViewById(R.id.tvBackToLogin);

        tvBackTOLogin.setOnClickListener(v -> {
            Intent i = new Intent(ResetPasswordActivity.this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
            startActivity(i);
            finish();
        });

        firebaseAuth = FirebaseAuth.getInstance();

        btnResetPassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                resetPassword(email);
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.rootResetPassword), "Please enter your email", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getColor(R.color.colorPrimary));
                TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.white));
                snackbar.show();
            }
        });
    }

    private void resetPassword(String email) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.rootResetPassword), "Reset link sent to " + email, Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getColor(R.color.colorPrimary));
                TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.white));
                snackbar.show();
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.rootResetPassword), "Error in sending reset link", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getColor(R.color.colorPrimary));
                TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.white));
                snackbar.show();
            }
        });
    }
}
