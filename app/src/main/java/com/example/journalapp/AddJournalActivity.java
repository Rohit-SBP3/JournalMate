package com.example.journalapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddJournalActivity extends AppCompatActivity {

    private MaterialButton addJournal;
    private ProgressBar progressBar;
    private TextInputEditText edtTitle, edtJournal;
    private FloatingActionButton addImageButton;
    private ImageView imageView;

    // Firebase FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");

    // Firebase Storage
    private StorageReference storageReference;

    // Firebase Auth | UserID | Username
    private String currentUserId;
    private String currentUserName;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    // Using Activity Result Launcher
    ActivityResultLauncher<String> mTakePhoto;
    ActivityResultLauncher<Intent> cropActivityResultLauncher;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal);

        edtJournal = findViewById(R.id.input_journal_thought);
        edtTitle = findViewById(R.id.input_journal_title);
        addImageButton = findViewById(R.id.button_add_image);
        progressBar = findViewById(R.id.progress_bar);
        addJournal = findViewById(R.id.button_add_journal);
        imageView = findViewById(R.id.journal_image_preview);

        progressBar.setVisibility(View.INVISIBLE);

        // Firebase
        storageReference = FirebaseStorage.getInstance().getReference();
        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        addImageButton.setOnClickListener(v -> {
            // Getting Image from the gallery
            mTakePhoto.launch("image/*");
        });

        addJournal.setOnClickListener(v -> {
            SaveJournal();
        });

        mTakePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri o) {
                        startCropActivity(o);
                    }
        });

        cropActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        final Uri resultUri = UCrop.getOutput(result.getData());
                        if (resultUri != null) {
                            imageUri = resultUri;
                            imageView.setImageURI(resultUri);
                        }
                    } else {
                        Toast.makeText(this, "Image cropping failed!", Toast.LENGTH_SHORT).show();
                    }
                });

        addImageButton.setOnClickListener(v -> {
            mTakePhoto.launch("image/*");
        });

        addJournal.setOnClickListener(v -> SaveJournal());
    }

    private void startCropActivity(@NonNull Uri uri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(80);
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(true);
        options.setToolbarTitle("Crop Image");

        Intent cropIntent = UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)  // Aspect ratio 1:1
                .withMaxResultSize(1080, 1080)
                .withOptions(options)
                .getIntent(this);

        cropActivityResultLauncher.launch(cropIntent); // Launch crop activity
    }


    private void SaveJournal() {
        String title = edtTitle.getText().toString().trim();
        String thoughts = edtJournal.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imageUri != null) {
            final StorageReference filePath = storageReference.child("journal_images")
                    .child("my_image_" + Timestamp.now().getSeconds());

            // Upload the cropped image
            filePath.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Save journal entry with image
                        Journal journal = new Journal();
                        journal.setTitle(title);
                        journal.setThoughts(thoughts);
                        journal.setImageURL(imageUrl);
                        journal.setUserID(currentUserId);
                        journal.setUsername(currentUserName);
                        journal.setTimeAdded(new Timestamp(new Date()));

                        collectionReference.add(journal).addOnSuccessListener(documentReference -> {
                            progressBar.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(AddJournalActivity.this, JournalListActivity.class));
                            finish();
                        }).addOnFailureListener(e -> {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(AddJournalActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(AddJournalActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(currentUserId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUserName = documentSnapshot.getString("username");
                        }
                    }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching username", e));
        }
    }
}