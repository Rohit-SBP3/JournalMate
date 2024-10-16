package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    // Widgets
    TextInputEditText edtEmail, edtUsername, edtPassword, edtConfirmPassword;
    MaterialButton signUP;
    TextView alreadyLogin;

    ProgressBar progressBar;

    // Firebase Auth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtEmail = findViewById(R.id.etEmail);
        edtUsername = findViewById(R.id.etUsername);
        edtPassword = findViewById(R.id.etPassword);
        edtConfirmPassword = findViewById(R.id.etConfirmPassword);

        signUP = findViewById(R.id.btnSignUp);
        alreadyLogin = findViewById(R.id.tvLogIn);

        progressBar = findViewById(R.id.progressBarMainSignup);

        firebaseAuth = FirebaseAuth.getInstance();

        alreadyLogin.setOnClickListener(v -> {
            Intent i = new Intent(SignUpActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        signUP.setOnClickListener(v -> {

            // Disable the button and show progress
            signUP.setText("");
            progressBar.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(edtEmail.getText().toString())
                    && !TextUtils.isEmpty(edtUsername.getText().toString())
                    && !TextUtils.isEmpty(edtPassword.getText().toString())
                    && !TextUtils.isEmpty(edtConfirmPassword.getText().toString())) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String confirmPassword = edtConfirmPassword.getText().toString().trim();
                String username = edtUsername.getText().toString().trim();

                CreateUserEmailAccount(email, password, username, confirmPassword);
            } else {
                progressBar.setVisibility(View.GONE);
                signUP.setText(R.string.sign_up);
                signUP.setEnabled(true);
                showSnackbar("Fields should not be empty");
            }
        });
    }

    // Function to create a user account
    private void CreateUserEmailAccount(String email, String password, String username, String confirmPassword) {


        // Validate password every time
        String passwordValidationMessage = getPasswordValidationMessage(password,confirmPassword);

        // If password is invalid, show the dialog and stop further execution
        if (!passwordValidationMessage.isEmpty()) {
            showPasswordInvalidDialog(passwordValidationMessage);
            return;
        }

        // If everything is valid, check for username uniqueness in Firestore
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {
            checkIfUsernameExists(email, password, username);
        }
    }

    // Check if the username exists in Firebase Firestore
    private void checkIfUsernameExists(String email, String password, String username) {
        collectionReference
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // Username is unique, proceed to create the account
                                createFirebaseAccount(email, password, username);
                            } else {
                                // Username already exists, show dialog
                                showPasswordInvalidDialog("Username already exists. Please choose another.");
                            }
                        } else {
                            showPasswordInvalidDialog("Failed to check username availability. Please try again.");
                        }
                    }
                });
    }

    // Create Firebase user account
    private void createFirebaseAccount(String email, String password, String username) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // Hide the ProgressBar once the task is complete
                progressBar.setVisibility(View.GONE);
                signUP.setText(R.string.sign_up);
                signUP.setEnabled(true);

                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // Send email verification
                        user.sendEmailVerification()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Store user details in Firestore
                                        saveUserToFirestore(user, username);
                                        // Show verification dialog
                                        showVerificationDialog();
                                    } else {
                                        showSnackbar("Failed to send verification email.");
                                    }
                                });
                    }
                } else {
                    showSnackbar("Account creation failed. Email might be already in use.");
                }
            }
        });
    }

    // Save user to Firestore
    private void saveUserToFirestore(FirebaseUser user, String username) {
        Map<String, String> newUser = new HashMap<>();
        newUser.put("userId", user.getUid());
        newUser.put("username", username);
        newUser.put("email", user.getEmail());

        collectionReference.document(user.getUid()).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved user data
                    Toast.makeText(SignUpActivity.this, "User data saved.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private String getPasswordValidationMessage(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return "Both password fields must be identical.";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters long.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one number.";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`].*")) {
            return "Password must contain at least one special character.";
        }
        return ""; // Empty string means the password is valid
    }

    private void showPasswordInvalidDialog(String message) {

        progressBar.setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle("Invalid Password")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    signUP.setText(R.string.sign_up);
                    signUP.setEnabled(true);
                    dialog.dismiss();
                })
                .show();
    }

    private void showVerificationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Email Verification")
                .setMessage("A verification email has been sent to your email. Please verify your email to proceed.")
                .setPositiveButton("OK", (dialog, which) -> {
                    signUP.setText(R.string.sign_up);
                    signUP.setEnabled(true);
                    Intent intent = new Intent(SignUpActivity.this, EmailVerificationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    // Show snackbar message
    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rootSignUP), message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getColor(R.color.colorPrimary));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.show();
    }
}
