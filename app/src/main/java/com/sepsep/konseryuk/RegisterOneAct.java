package com.sepsep.konseryuk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterOneAct extends AppCompatActivity {
    LinearLayout btn_back;
    Button btn_continue;
    EditText username,password,email_address;
    DatabaseReference reference,referenceusername;

    String USERNAME_KEY = "usernamekey";
    String username_key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_one);

        username= findViewById(R.id.username);
        password= findViewById(R.id.password);
        email_address= findViewById(R.id.email_address);
        btn_continue= findViewById(R.id.btn_continue);
        btn_back= findViewById(R.id.btn_back);



        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_continue.setEnabled(false);
                btn_continue.setText("Loading ...");

                referenceusername = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(username.getText().toString());
                referenceusername.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Toast.makeText(getApplicationContext(), "Username Sudah Ada!", Toast.LENGTH_SHORT).show();
                            btn_continue.setEnabled(true);
                            btn_continue.setText("CONTINUE");
                        }
                        else {

                            SharedPreferences sharedPreferences = getSharedPreferences(USERNAME_KEY, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(username_key, username.getText().toString());
                            editor.apply();

                            //Simpan ke Database
                            reference = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child(username.getText().toString());
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    dataSnapshot.getRef().child("username").setValue(username.getText().toString());
                                    dataSnapshot.getRef().child("password").setValue(password.getText().toString());
                                    dataSnapshot.getRef().child("email_address").setValue(email_address.getText().toString());
                                    dataSnapshot.getRef().child("user_balance").setValue(1000);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                            Intent gotonextregister = new Intent(RegisterOneAct.this,RegisterTwoAct.class);
                            startActivity(gotonextregister);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                ///Menyimpan Lokal

            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goback = new Intent(getApplicationContext(),GetStartedAct.class);
                startActivity(goback);
                finish();
            }
        });
    }
    public void onBackPressed(){
        Intent goback = new Intent(getApplicationContext(),GetStartedAct.class);
        startActivity(goback);
        finish();
    }

}
