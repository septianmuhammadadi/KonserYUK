package com.sepsep.konseryuk;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class EditProfileAct extends AppCompatActivity {
    LinearLayout btn_back;
    ImageView photo_edit_profile;
    EditText xnama_lengkap,xusername,xpassword,xbio,xemail_address;
    Button btn_save,btn_add_new_photo;
    String USERNAME_KEY = "usernamekey";
    String username_key = "";
    String username_key_new = "";
    Uri photo_location;

    Integer photo_max=1;
    DatabaseReference reference;
    StorageReference storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getUsernameLocal();
        btn_back = findViewById(R.id.btn_back);
        photo_edit_profile = findViewById(R.id.photo_edit_profile);
        xnama_lengkap = findViewById(R.id.xnama_lengkap);
        xusername = findViewById(R.id.xusername);
        xpassword = findViewById(R.id.xpassword);
        xbio = findViewById(R.id.xbio);
        xemail_address = findViewById(R.id.xemail_address);
        btn_add_new_photo = findViewById(R.id.btn_add_new_photo);
        btn_save = findViewById(R.id.btn_save);

        reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(username_key_new);
        storage = FirebaseStorage.getInstance().getReference().child("Photousers").child(username_key_new );
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                xnama_lengkap.setText(Objects.requireNonNull(dataSnapshot.child("nama_lengkap").getValue()).toString());
                xbio.setText(Objects.requireNonNull(dataSnapshot.child("bio").getValue()).toString());
                xusername.setText(Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString());
                xpassword.setText(Objects.requireNonNull(dataSnapshot.child("password").getValue()).toString());
                xemail_address.setText(Objects.requireNonNull(dataSnapshot.child("email_address").getValue()).toString());

                Picasso.with(EditProfileAct.this).load(Objects.requireNonNull(dataSnapshot.child("url_photo_profile").getValue()).toString()).centerCrop()
                        .fit().into(photo_edit_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_save.setEnabled(false);
                btn_save.setText("Loading ...");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().child("username").setValue(xusername.getText().toString());
                        dataSnapshot.getRef().child("password").setValue(xpassword.getText().toString());
                        dataSnapshot.getRef().child("bio").setValue(xbio.getText().toString());
                        dataSnapshot.getRef().child("nama_lengkap").setValue(xnama_lengkap.getText().toString());
                        dataSnapshot.getRef().child("email_address").setValue(xemail_address.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                if (photo_location !=null){
                    final StorageReference storageReference1 =
                            storage.child(System.currentTimeMillis()+ "." +
                                    getFileExtension(photo_location));

                    storageReference1.putFile(photo_location).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String uri_photo = uri.toString();
                                    reference.getRef().child("url_photo_profile").setValue(uri_photo);

                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Intent gopindahprofile = new Intent(EditProfileAct.this, MyProfileAct.class);
                                    startActivity(gopindahprofile);
                                    finish();
                                }
                            });
                        }
                    }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            Intent gopindahprofile = new Intent(EditProfileAct.this, MyProfileAct.class);
                            startActivity(gopindahprofile);
                            finish();
                        }
                    });
                }
            }
        });
        btn_add_new_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPhoto();
            }
        });


        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gopindahprofile = new Intent(EditProfileAct.this, MyProfileAct.class);
                startActivity(gopindahprofile);
                finish();

            }
        });
    }
    public void onBackPressed(){
        Intent gopindahprofile = new Intent(EditProfileAct.this, MyProfileAct.class);
        startActivity(gopindahprofile);
        finish();
    }
    String getFileExtension (Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    public void findPhoto(){
        Intent pic = new Intent();
        pic.setType("image/*");
        pic.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(pic, photo_max);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == photo_max && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            photo_location = data.getData();
            Picasso.with(this).load(photo_location).centerCrop().fit().into(photo_edit_profile);
        }
    }
    public void  getUsernameLocal(){
        SharedPreferences sharedPreferences = getSharedPreferences(USERNAME_KEY,  MODE_PRIVATE);
        username_key_new = sharedPreferences.getString(username_key, "");
    }
}
