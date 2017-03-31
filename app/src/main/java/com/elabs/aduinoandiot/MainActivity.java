package com.elabs.aduinoandiot;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    DatabaseReference reference;
    FirebaseDatabase firebaseDatabase;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public  Intent getdata;
    public String address="",Url="";
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket socket;
    int flagRetry=0,catchFlag=0;
    TextView value;
    ProgressDialog Dialog;
    Handler handler;
    int Retry=0;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialise();
        Display(address);
        if(isFirstTime()){
            recreate();
            editor.putBoolean("first",true);
            editor.commit();
        }
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
                value.setText("T h e  v a l u e  i s  "+p.getValue());
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
                Log.i("23232323","I am here");
                if(adapter!=null){
                    while(socket==null){
                        catchFlag=0;
                        try {
                            Log.i("23232323","I am here2");
                                    socket = adapter.getRemoteDevice(address).createInsecureRfcommSocketToServiceRecord(uuid);
                            socket = (BluetoothSocket) BluetoothAdapter.getDefaultAdapter().
                                    getRemoteDevice(address).getClass().
                                    getMethod("createRfcommSocket", new Class[] {int.class}).invoke( adapter.getRemoteDevice(address),1);
                                    socket.connect();

                        } catch (IOException e) {
                            Log.i("23232323","I am here3");
                            e.printStackTrace();
                            Display(e.toString()+"\n"+"The  requested device doesn't have the desired service!");
                            catchFlag=1;
                           handler.post(new Runnable() {
                               @Override
                               public void run() {
                                   Dialog.cancel();

                               }
                           });

                            if(Retry<3){
                                Display("Retrying "+(Retry+1)+"/"+"3");
                                Retry();
                            }

                            break;
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } finally {
                            flagRetry=1;
                            if(catchFlag==0)
                                Display("Connected");
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
        if(socket!=null)
            socket.getOutputStream().write((data+"").getBytes());

    }

    private void Retry() {
        catchFlag=0;
        Retry++;
        if(flagRetry==0)
        {
            try {
                socket = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address).createInsecureRfcommSocketToServiceRecord(uuid);
               // socket = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address).getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                socket.connect();
             //   socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                Display(e.toString() + "\n" + "The  requested device doesn't have the desired service!");
                catchFlag=1;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Dialog.cancel();

                    }
                });
                if (Retry < 3) {
                    Display("Retrying " + (Retry + 1) + "/" + "3");
                    Retry();
                }

            }finally {
                flagRetry=1;
                if (catchFlag==0)
                    Display("Connected");
            }
        }
    }
    private boolean isFirstTime(){
        return sharedPreferences.getBoolean("first",false);
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
        View v = LayoutInflater.from(this).inflate(R.layout.progress,null);
        TextView progress_id = (TextView)v.findViewById(R.id.progress_id);
        Typeface as=Typeface.createFromAsset(getAssets(),"android.ttf");
        progress_id.setTypeface(as);
        Dialog.setContentView(v);
        handler = new Handler();
       sharedPreferences = getSharedPreferences(new Constants().sharedPreferenceConstant, Context.MODE_PRIVATE);
       // Display(address);
        editor = sharedPreferences.edit();
        name = sharedPreferences.getString("name","");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Dialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Dialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        try {
            socket.close();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Display(e.toString());
        }
    }
}
