package com.elabs.aduinoandiot;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    DatabaseReference reference;
    FirebaseDatabase firebaseDatabase;
    SharedPreferences sharedPreferences;
    public  Intent getdata;
    public String address="",Url="";
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket socket;
    TextView value;
    ProgressDialog Dialog;
    Handler handler;

    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialise();
        try {
            SetUpConnectionSocket();
        } catch (IOException e) {
            e.printStackTrace();
            Display(e.toString()+"\n"+"Error in connecting to Bluetooth Socket!");
        }
        getValueFromDataBase();

    }

    private void getValueFromDataBase(){
        if(reference!=null)
        { reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Display("Data has changed");
                Profile p = dataSnapshot.child(name).getValue(Profile.class);
                value.setText("The value is "+p.getValue());
              try {
                    sendData(p.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                    Display(e.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Display("cancelled");
            }
        });
        }else{
            Display("Cant Read Database");
        }

    }

    private void SetUpConnectionSocket() throws IOException{
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
          socket = null;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if(adapter!=null){
                    while(socket==null){
                        try {
                            socket = adapter.getRemoteDevice(address).createInsecureRfcommSocketToServiceRecord(uuid);
                            socket.connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Display(e.toString()+"\n"+"The  requested device doesn't have the desired service!");

                           handler.post(new Runnable() {
                               @Override
                               public void run() {
                                   Dialog.cancel();

                               }
                           });

                            break;
                        }
                    }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Dialog.cancel();
                }
            });

                }
            }
        });

        t.start();
    }

    private void sendData(int data) throws IOException{
        socket.getOutputStream().write((data+"").getBytes());

    }

    private void Display(final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });
    Log.i("info",msg);
    }
    private void Initialise(){
         firebaseDatabase = FirebaseDatabase.getInstance(FirebaseApp.getInstance());
       // reference = FirebaseDatabase.getInstance(Fireb).getReference();

        reference = FirebaseDatabase.getInstance().getReference("Profile");
        getdata = getIntent();
        address = getdata.getStringExtra("mac");
        value = (TextView)findViewById(R.id.value);
        Dialog = ProgressDialog.show(this,"Connecting","Please Wait");
        handler = new Handler();
       sharedPreferences = getSharedPreferences(new Constants().sharedPreferenceConstant, Context.MODE_PRIVATE);
       // Display(address);
        name = sharedPreferences.getString("name","");
    }




}
