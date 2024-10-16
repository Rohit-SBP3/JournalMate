package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    TextInputEditText edtUsername, edtPassword;
    MaterialButton btnLogIn;
    TextView tvSignUp, tvForgotPassword;

    ProgressBar progressBar;


    // FirebaseAuth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUsername = findViewById(R.id.etLoginUsername);
        edtPassword = findViewById(R.id.etLoginPassword);

        btnLogIn = findViewById(R.id.btnLogin);

        tvSignUp = findViewById(R.id.tvSignUp);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        progressBar = findViewById(R.id.progressBarMainLogin);

        tvSignUp.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this,SignUpActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
            startActivity(i);
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            // Direct to ResetPasswordActivity
            Intent resetIntent = new Intent(MainActivity.this, ResetPasswordActivity.class);
            startActivity(resetIntent);
        });

        // Login Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        btnLogIn.setOnClickListener(v -> {
            logEmailPasswordUser(edtUsername.getText().toString().trim(),
                    edtPassword.getText().toString().trim());
        });
    }

    // Check if the user is already logged in when the app starts
    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // Check if email is verified
            if (currentUser.isEmailVerified()) {
                // If email is verified, go to JournalListActivity
                Intent i = new Intent(MainActivity.this, JournalListActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            } else {
                // If email is not verified, redirect to EmailVerificationActivity
                Intent i = new Intent(MainActivity.this,EmailVerificationActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                showVerificationDialog();
            }
        }
    }


    private void logEmailPasswordUser(String username, String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {

            // Disable the button and show progress
            btnLogIn.setEnabled(false);
            btnLogIn.setText(""); // Remove the text
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.signInWithEmailAndPassword(username, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if (authResult != null) {
                                // Hide progress bar and enable login button
                                progressBar.setVisibility(View.GONE);
                                btnLogIn.setEnabled(true);
                                FirebaseUser user = firebaseAuth.getCurrentUser();

                                if (user != null && user.isEmailVerified()) {
                                    // Email is verified, proceed to JournalListActivity
                                    Intent i = new Intent(MainActivity.this, JournalListActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();
                                } else if (user != null && !user.isEmailVerified()) {
                                    // Email is not verified, send verification email and redirect to EmailVerificationActivity
                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                showVerificationDialog();
                                            } else {
                                                showSnackbar("Failed to send verification email. Please try again.");
                                            }
                                        }
                                    });
                                }
                            } else {
                                // Show an error message if login fails
                                showSnackbar("Can't find account.");
                            }
                        }
                    }).addOnFailureListener(e -> {
                        // Handle failure cases
                        progressBar.setVisibility(View.GONE);
                        btnLogIn.setEnabled(true);
                        showSnackbar("Login failed: " + e.getMessage());
                    });
        } else {
            showSnackbar("Please fill in both fields.");
        }
    }


    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rootSignUP), message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getColor(R.color.colorPrimary));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.show();
    }

    private void showVerificationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Email Verification")
                .setMessage("A verification email has been sent to your email. Please verify your email to proceed.")
                .setPositiveButton("OK", (dialog, which) -> {
                    btnLogIn.setText(R.string.login);
                    btnLogIn.setEnabled(true);
                    // Redirect to EmailVerificationActivity
                    Intent intent = new Intent(MainActivity.this, EmailVerificationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

}