package com.elabs.aduinoandiot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PairedDevices extends AppCompatActivity {

    RecyclerView recyclerView;
    BluetoothAdapter adapter;
    Set<BluetoothDevice> devices;
    List<String> deviceDetails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_devices);

        try{
            Initialise();
           // deviceDetails.add("gandu");
            adapter=BluetoothAdapter.getDefaultAdapter();
            checkBluetoothConnection(adapter);

        }catch (Exception e){
             Display(e.toString());
        }

    }

    private void checkBluetoothConnection(BluetoothAdapter adapter1) throws Exception {
    if(adapter1==null)
    {
        Toast.makeText(this, "No Bluetooth", Toast.LENGTH_SHORT).show();

    }
    else
    {
        if(!adapter1.isEnabled())
        {
            Intent i=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i,13);
        }
        else {
            getPairedDevices();
        }
    }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==13)
        {
            if(resultCode==RESULT_OK)
            {
                Toast.makeText(this, "Turned on", Toast.LENGTH_SHORT).show();
                try {
                    getPairedDevices();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void Initialise() throws Exception{
        recyclerView=(RecyclerView)findViewById(R.id.pairedDevices);
        adapter = BluetoothAdapter.getDefaultAdapter();

       // View v= LayoutInflater.from(this).inflate(R.layout.content_paired_devices,null);
        Typeface as=Typeface.createFromAsset(getAssets(),"android.ttf");
        TextView fd=(TextView)findViewById(R.id.fcuk_recycle);
        fd.setTypeface(as);
    }


    private void getPairedDevices() throws Exception{
        devices = adapter.getBondedDevices();

        if(devices.size()>0){
            for(BluetoothDevice device:devices){
                deviceDetails.add(device.getAddress()+"\n"+device.getName());
               // Display(device.getAddress()+"\n"+device.getName());
            }
        }else{
            Display("You have 0 number of paired devices!");
        }
        pairedDevicesAdapter.context=PairedDevices.this;
        pairedDevicesAdapter devicesAdapter = new pairedDevicesAdapter(deviceDetails);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(devicesAdapter);



    }

    private void Display(final String s){
  new Handler().post(new Runnable() {
       @Override
          public void run() {
            Toast.makeText(PairedDevices.this,s,Toast.LENGTH_SHORT).show();
          Log.e("error",s);
            }
        });
    }
}
