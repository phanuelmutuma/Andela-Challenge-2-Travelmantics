package com.phan.alc.travelmantics;

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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class InsertActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference myRef;
    EditText title, price,description;
    ImageView image;
    Button selectImage;
    TravelDeals deal;
    String imageU;
    String picName;
    public static final int REQUEST = 12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        database = FirebaseUtil.database;
        myRef = FirebaseUtil.myRef;
        title = findViewById(R.id.title);
        price = findViewById(R.id.price);
        description = findViewById(R.id.description);
        selectImage = findViewById(R.id.select_image);
        image = findViewById(R.id.image);
        imageU = "null";
        selectImage.setOnClickListener(new View.OnClickListener() {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST && resultCode == RESULT_OK) {
            showImage(imageU);
            imageU = "upload";
            selectImage.setText("Getting Image...");
            selectImage.setEnabled(false);
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.mStoragereference.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    picName = taskSnapshot.getStorage().getPath();
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageU = uri.toString();
                            showImage(imageU);
                            selectImage.setText("Select Image...");
                            selectImage.setEnabled(true);
                            Toast.makeText(InsertActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    int yx = (int) progress;
                    String prog = "Uploading " + yx + "%";
                    selectImage.setText(prog);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu,menu);
            return true;
    }

    private void save(){
        if(imageU.equals("upload")){
            Toast.makeText(this, "Please Wait While We Upload Your Image...", Toast.LENGTH_SHORT).show();
        }else if (imageU.equals("null")) {
            Toast.makeText(this, "Select an Image...", Toast.LENGTH_SHORT).show();
        }else{
            deal = new TravelDeals();
            deal.setTitle(title.getText().toString());
            deal.setPrice(price.getText().toString());
            deal.setDescription(description.getText().toString());
            deal.setImageUrl(imageU);
            deal.setImageName(picName);
            myRef.push().setValue(deal);
            Toast.makeText(this, "Deal Saved Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void showImage(String url){
        if (url != null && url.isEmpty() == false){
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.place)
                    .into(image);
        }
    }
}
