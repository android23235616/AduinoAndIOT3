package com.elabs.aduinoandiot;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    DatabaseReference reference;
    FirebaseDatabase firebaseDatabase;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Profile errorProfile;
    OutputStreamWriter outputStreamWriter;
    public  Intent getdata;
    public String address="",Url="";
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket socket;
    int flagRetry=0,catchFlag=0;
    TextView value;
    ProgressDialog Dialog;
    Handler handler;
    int Retry=0;
    FloatingActionButton errorButton;
    int retrySuccess=0;
    long GlobalTime=0;
    TextView history;
    String name;
    PowerManager pwm;
    PowerManager.WakeLock wakeLock;
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
            socket=null;
            SetUpConnectionSocket();
        } catch (IOException e) {
            e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
            Display(e.toString()+"\n"+"\n"+"Error in connecting to Bluetooth Socket!");
            FirebaseCrash.report(new Exception(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n"));

        }
        getValueFromDataBase();
        errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = readFromFile(MainActivity.this);
                sendMail(msg);
            }
        });
    }

    private void getValueFromDataBase(){
        if(reference!=null)
        { reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Profile p = dataSnapshot.child(name).getValue(Profile.class);
                value.setText("T h e  v a l u e  i s  "+p.getValue());
              try {
                    sendData(p.getValue());
                  history.append(name+" @ "+convertSecondsToHMmSs(System.currentTimeMillis())+": "+p.getValue()+"\n");
                } catch (IOException e) {
                    e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);

                  ////////////////////////////////NEW CODE///////////////////////
                  FirebaseCrash.report(new Exception(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n"));
                    if(e.toString().contains("socket closed"))
                    {

                    }else if(e.toString().contains("Broken pipe")){
                        Display("broken pipe. Reconnecting");
                        writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
                        Retry=0;
                        try {
                            SetUpConnectionSocket();
                        } catch (IOException e1) {
                            e1.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);

                            Display(e.toString()+"\n");
                        }finally
                        {
                            try {
                               // RestablishingConnection(p);
                            } catch (Exception e1) {
                                e1.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
                                Display(e1.toString());
                            }
                        }
                    }else{
                        Display(e.toString()+"\n");
                    }

                  ////////////////////////////////////HERE///////////////////////////////
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

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d:%02d", h,m,s);
    }
///////////////////////////////////EXTRA FUNCTION
    private void RestablishingConnection(final Profile p) throws Exception{

       /* if(errorProfile!=null){
            if(errorProfile!=p){
                retrySuccess=100;
                Log.i("asdfgh",retrySuccess+"");
            }
            Log.i("rest","received error profile "+retrySuccess);
        }*/

       Thread t = new Thread(new Runnable() {
           @Override
           public void run() {

               while(retrySuccess!=100){
                   try {
                       sendData(p.getValue());
                   } catch (IOException e) {
                       e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
                   }
                   try {
                       Thread.sleep(1000);
                   } catch (InterruptedException e) {
                       e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
                   }
                   Log.i("running","running");
                   if(errorProfile!=null){
                       if(errorProfile!=p){

                           Log.i("asdfgh",retrySuccess+"");
                           break;
                       }
                       Log.i("rest","received error profile "+retrySuccess);
                   }
               }
               GlobalTime=0;
               retrySuccess=0;
           }
       });

        if(GlobalTime<1){
            GlobalTime++;
            t.start();
            errorProfile=p;
            Log.i("andr","Thread Started");
        }
    }
///////////////////////////////////////////////
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
                            FirebaseCrash.report(new Exception(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n"));
                            Log.i("23232323","I am here3");
                            e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
                            Display(e.toString()+"\n"+"\n"+"The  requested device doesn't have the desired service!");
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
                            e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
                        } finally {
                            flagRetry=1;
                            if(catchFlag==0) {
                                Display("Connected");

                            }

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
        if(socket!=null&&socket.getOutputStream()!=null)
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
                FirebaseCrash.report(new Exception(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n"));
                e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
                Display(e.toString()+"\n" + "\n" + "The  requested device doesn't have the desired service!");
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
        history = (TextView)findViewById(R.id.history);
        reference = FirebaseDatabase.getInstance().getReference("Profile");
        getdata = getIntent();
        pwm  = (PowerManager)getSystemService(POWER_SERVICE);
        wakeLock = pwm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"My Tag");

        address = getdata.getStringExtra("mac");
        value = (TextView)findViewById(R.id.value);
        Dialog = ProgressDialog.show(this,"Connecting","Please Wait");
        View v = LayoutInflater.from(this).inflate(R.layout.progress,null);
        TextView progress_id = (TextView)v.findViewById(R.id.progress_id);
        Typeface as=Typeface.createFromAsset(getAssets(),"android.ttf");
        progress_id.setTypeface(as);
        Dialog.setContentView(v);
        errorButton = (FloatingActionButton)findViewById(R.id.errorButton);
        handler = new Handler();
       sharedPreferences = getSharedPreferences(new Constants().sharedPreferenceConstant, Context.MODE_PRIVATE);
        try {
            outputStreamWriter = new OutputStreamWriter(this.openFileOutput("log.txt", Context.MODE_PRIVATE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            FirebaseCrash.report(new Exception(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n"));
            Display(e.toString());
        }
        // Display(address);
        editor = sharedPreferences.edit();
        name = sharedPreferences.getString("name","");
       
    }

    private void sendMail(String body){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","majumdartanmay68@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Error Log For AduinoIot");
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void writeToFile(String data,Context context) {
        try {

            outputStreamWriter.append(data+"\n");
           outputStreamWriter.flush();
        }
        catch (IOException e) {
            FirebaseCrash.report(new Exception(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n"));
            writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
            Log.e("Exception", "File write failed: " + e.toString()+"\n");
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("log.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString+"\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            FirebaseCrash.report(new Exception(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n"));
            writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
            Log.e("login activity", "File not found: " + e.toString()+"\n");
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString()+"\n");
            writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
            FirebaseCrash.report(new Exception(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n"));
        }

        return ret;
    }

   
        public static String getFormattedDateFromTimestamp(long timestampInMilliSeconds)
        {
            Date date = new Date();
            date.setTime(timestampInMilliSeconds);
            String formattedDate=new SimpleDateFormat("MMM d, yyyy").format(date);
            return formattedDate;

        }
    
    @Override
    public void onStop(){
        super.onStop();
      /*  synchronized (wakeLock.isHeld()) {
            // sanity check for null as this is a public method
            if (wakeLock != null) {
                Log.v("My WakeLog", "Releasing wakelock");
                FirebaseCrash.log("Releasing wakelock");
                try {
                    wakeLock.release();
                } catch (Throwable th) {
                    // ignoring this exception, probably wakeLock was already released
                }
            } else {
                // should never happen during normal workflow
                Log.e("My Tag", "Wakelock reference is null");
                FirebaseCrash.log(" wakelock is null");
            }
        }*/
        //if (wakeLock.isHeld())
          //  wakeLock.release();
        Log.i("stopOn","On Stop Called");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i("resumed","resumed");
        if(wakeLock.isHeld())
        wakeLock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Dialog.dismiss();
        try {
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Dialog.dismiss();
        wakeLock.acquire();
    }

    @Override
    public void onBackPressed(){
        try {
            socket.close();
           // socket=null;
            startActivity(new Intent(this,PairedDevices.class));
            finish();
        } catch (Exception e) {
            e.printStackTrace();writeToFile(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n",MainActivity.this);
            Display(e.toString()+"\n");
            FirebaseCrash.report(new Exception(name+"_"+getFormattedDateFromTimestamp(System.currentTimeMillis())+" : "+e.toString()+"\n"));
        }
    }
}
