package com.elabs.aduinoandiot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Registration extends AppCompatActivity {

    TextView type;
    EditText Username, password;
    Constants constants;
    Button submit;
    String name;
    Handler handler;
    ProgressDialog dialog;
    int flag = 0, flag2 = 1;
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    DataSnapshot checkSnapshot;
    ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Initialise();
        setTypeFace();
        Profile TestProfile = new Profile("Fake Profile", "Fake Password", 0);
        setUpFirebase(TestProfile);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              dialog.cancel();
                checkSnapshot = dataSnapshot;
                if (flag == 0 && flag2 == 0 ){
                    Display("Registered");
                   /* handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(Registration.this, EmptyActivity.class));
                            finish();
                        }
                    }, 300);*/
                    startActivity(new Intent(Registration.this, EmptyActivity.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Display(databaseError.toString());
            }
        };
        databaseReference.addValueEventListener(valueEventListener);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = Username.getText().toString();
                String pass = password.getText().toString();
                editor.putString("name", name);
                editor.putString("pass", pass);
                Profile profile = new Profile(name, pass, 0);
                editor.apply();
                if(CheckUniqueness(name)){
                    flag2 = 0;
                    dialog.show();
                    setUpFirebase(profile);
                }

            }
        });
    }


    private boolean checkForRegistration(){
        String checker = sharedPreferences.getString("name","");
        if(!checker.equals("")){
           startActivity(new Intent(this,PairedDevices.class));
            finish();
            return true;
        }else
            return false;
    }
    private boolean CheckUniqueness(String name) {
        if(checkSnapshot!=null){
            for (DataSnapshot snap : checkSnapshot.getChildren()) {
                if ((snap.getValue(Profile.class).getId().equals(name))) {
                    Display("User already exists");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onStop(){
        super.onStop();
        databaseReference.removeEventListener(valueEventListener);
    }
    private void setTypeFace()
    {
         Typeface as=Typeface.createFromAsset(getAssets(),"android.ttf");
        type.setTypeface(as);
        Username.setTypeface(as);
        password.setTypeface(as);
        submit.setTypeface(as);
    }
/*
* forEach {
*
* }
* */
    private void Initialise(){
        databaseReference = FirebaseDatabase.getInstance().getReference("Profile");
        Username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        submit = (Button)findViewById(R.id.submit);
        constants = new Constants();
        sharedPreferences = getSharedPreferences(constants.sharedPreferenceConstant, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        type= (TextView) findViewById(R.id.fcuk);
        handler = new Handler();
        if(!checkForRegistration()){
            Display("This may take a while!!");
        }

        dialog  =  ProgressDialog.show(this,"Connecting","Please Wait");
        View v = LayoutInflater.from(this).inflate(R.layout.progress,null);
        TextView progress_id = (TextView)v.findViewById(R.id.progress_id);
        Typeface as=Typeface.createFromAsset(getAssets(),"android.ttf");
        progress_id.setTypeface(as);
        dialog.setContentView(v);
    }

    private void Display(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
        Log.i("2323",s);
    }

    private void setUpFirebase(Profile p1){

        databaseReference.child(p1.getId()).setValue(p1);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dialog.dismiss();
    }
}
