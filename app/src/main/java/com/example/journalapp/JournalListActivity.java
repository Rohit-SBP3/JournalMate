package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class JournalListActivity extends AppCompatActivity {

    private FloatingActionButton addImageButtonList;

    // Firebase Auth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firebase FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");

    // Firebase Storage
    private StorageReference storageReference;

    // List of Journals
    private List<Journal> journalList;

    // RecyclerView
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Remove default title (since we are using a custom one)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        addImageButtonList = findViewById(R.id.button_add_image_list);

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Widgets
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // posts ArrayList
        journalList = new ArrayList<>();

        addImageButtonList.setOnClickListener(v -> {
            Intent i = new Intent(JournalListActivity.this,AddJournalActivity.class);
            startActivity(i);
        });
    }

    // Adding a menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.journal_menu, menu);

        // Get the Sign Out menu item
        MenuItem signOutItem = menu.findItem(R.id.actionSignOut);

        // Create a SpannableString to apply custom text color
        SpannableString s = new SpannableString(signOutItem.getTitle());
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.purple700)), 0, s.length(), 0);
        signOutItem.setTitle(s);

        // Set the icon tint color
        Drawable icon = signOutItem.getIcon();
        if (icon != null) {
            Log.d("MenuItemIcon", "Icon set for sign out");
            icon.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            signOutItem.setIcon(icon);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if(itemId == R.id.actionAdd) {
            if (currentUser != null && firebaseAuth != null) {
                Intent i = new Intent(JournalListActivity.this, AddJournalActivity.class);
                startActivity(i);
            }
        }
        else if(itemId == R.id.actionSignOut) {
            if (currentUser != null && firebaseAuth != null) {
                firebaseAuth.signOut();
                Intent i = new Intent(JournalListActivity.this, MainActivity.class);
                startActivity(i);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart(){
        super.onStart();

        // Clear the journalList to avoid duplicate entries
        journalList.clear();

        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots ->{
            // QueryDocumentSnapshot: It is a document that represents a single
            // document retrieved from a FireStore query.

            for(QueryDocumentSnapshot journalDS : queryDocumentSnapshots){
                Journal journal = journalDS.toObject(Journal.class);
                journalList.add(journal);
            }

            // Update RecyclerView
            myAdapter = new MyAdapter(journalList,JournalListActivity.this);
            recyclerView.setAdapter(myAdapter);
            myAdapter.notifyDataSetChanged();
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(JournalListActivity.this, "OOPS! Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}