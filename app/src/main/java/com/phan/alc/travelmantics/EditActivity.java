package com.phan.alc.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {
    TravelDeals deal, de;
    EditText title, price, description;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Button button;
    ImageView imageView;
    public static final int REQUEST = 12;
    String imageU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        title = findViewById(R.id.title);
        price = findViewById(R.id.price);
        description = findViewById(R.id.description);
        button = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        imageU = "null";
        //FirebaseUtil.openReference("traveldeals", this);
        database = FirebaseUtil.database;
        myRef = FirebaseUtil.myRef;
        Intent intent = getIntent();
        de = (TravelDeals) intent.getSerializableExtra("Deals");
        if(de == null){
            de = new TravelDeals();
        }
        this.deal = de;
        title.setText(de.getTitle());
        price.setText(de.getPrice());
        description.setText(de.getDescription());
        imageU = de.getImageUrl();

        if(FirebaseUtil.isAdmin) {
            title.setEnabled(true);
            price.setEnabled(true);
            description.setEnabled(true);
            button.setEnabled(true);
        }else{
            title.setEnabled(false);
            price.setEnabled(false);
            description.setEnabled(false);
            button.setEnabled(false);
        }
        showImage(de.getImageUrl());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Select Image"), REQUEST);
            }
        });
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST && resultCode == RESULT_OK) {
            showImage(imageU);
            button.setText("Getting Image...");
            button.setEnabled(false);
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.mStoragereference.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String picName = taskSnapshot.getStorage().getPath();
                    deal.setImageName(picName);
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageU = uri.toString();
                            showImage(imageU);
                            button.setText("Select Image...");
                            button.setEnabled(true);
                            Toast.makeText(EditActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    int yx = (int) progress;
                    String prog = "Uploading " + yx + "%";
                    button.setText(prog);
                }
            });
        }
    }
    private void showImage(String url){
        if (url != null && url.isEmpty() == false){
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.place)
                    .into(imageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_menu,menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.sav_menu).setVisible(true);
        }else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.sav_menu).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sav_menu:
                deal.setTitle(title.getText().toString());
                deal.setPrice(price.getText().toString());
                deal.setDescription(description.getText().toString());
                deal.setImageUrl(imageU);
                myRef.child(de.getId()).setValue(deal);
                Toast.makeText(EditActivity.this, "Added Item Successfully", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            case R.id.delete_menu:
                myRef.child(de.getId()).removeValue();
                if (de.getImageName() != null && de.getImageName().isEmpty() == false){
                    StorageReference mreff = FirebaseUtil.mFirebasestorage.getReference().child(de.getImageName());
                    mreff.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditActivity.this, "Delete Deal Successful", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
