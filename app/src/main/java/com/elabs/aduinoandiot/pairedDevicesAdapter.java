package com.elabs.aduinoandiot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

/**
 * Created by Tanmay on 14-03-2017.
 */

public class pairedDevicesAdapter extends RecyclerView.Adapter<pairedDevicesAdapter.innerViewHolder> {
    private List<String> deviceDetails ;
    public static Context context;
    public pairedDevicesAdapter(List<String> deviceDetails){
        this.deviceDetails=deviceDetails;

    }
    @Override
    public innerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
       // Display(context,"Called");
        return new innerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final innerViewHolder holder, int position) {
       holder.deviceText.setText(deviceDetails.get(holder.getAdapterPosition()));
       // Display(holder.deviceText.getContext(),deviceDetails.get(holder.getAdapterPosition()));
        holder.deviceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),MainActivity.class);
               i.putExtra("mac",deviceDetails.get(holder.getAdapterPosition()).substring(0,17));
                v.getContext().startActivity(i);
                new PairedDevices().finish();

            }
        });
      Log.i("info23235616",""+deviceDetails.get(position));

    }

    private void Display(final Context v,final String s){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(v,s,Toast.LENGTH_SHORT).show();
                Log.e("error",s);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceDetails.size();
    }

    public static class innerViewHolder extends RecyclerView.ViewHolder{
        public TextView deviceText;
        public innerViewHolder(View itemView) {
            super(itemView);
            deviceText = (TextView) itemView.findViewById(R.id.items);
            Typeface as=Typeface.createFromAsset(itemView.getContext().getAssets(),"android.ttf");
            deviceText.setTypeface(as);
        }
    }
}
