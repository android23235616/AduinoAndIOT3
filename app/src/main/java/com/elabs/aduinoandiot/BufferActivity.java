package com.elabs.aduinoandiot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class BufferActivity extends AppCompatActivity {
    TextView t1,t2,t3,t4,t5,t6,t7,t8,tutuorial;
    CheckBox checkBox;
    Button Registration;
    boolean isCheck=false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buffer);
        Initialise();
        if(Is_Registered()){
            startActivity(new Intent(this,Registration.class));
            finish();
        }
        Settypeface();
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCheck=isChecked;
                editor.putBoolean("share",isCheck);
                editor.commit();
            }
        });
        Registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(),Registration.class));
            }
        });
    }

    private void Initialise(){
        t1 = (TextView)findViewById(R.id.text1);
        t2 = (TextView)findViewById(R.id.text2);
        t3 = (TextView)findViewById(R.id.text3);
        t4 = (TextView)findViewById(R.id.text4);
        t5 = (TextView)findViewById(R.id.text5);
        t6 = (TextView)findViewById(R.id.text6);
        t7 = (TextView)findViewById(R.id.text7);
        t8 = (TextView)findViewById(R.id.text8);

        tutuorial = (TextView)findViewById(R.id.tutorial);
        Registration =(Button)findViewById(R.id.nxt_page);
        checkBox  = (CheckBox)findViewById(R.id.checker);
        sharedPreferences = getSharedPreferences(Constants.sharedPrefernce2, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    private boolean Is_Registered(){
        return sharedPreferences.getBoolean("share",false);
    }

    private void Settypeface(){
        Typeface tf = Typeface.createFromAsset(getAssets(),"android.ttf");
        t1.setTypeface(tf);
        t2.setTypeface(tf);
        t3.setTypeface(tf);
        t4.setTypeface(tf);
        t5.setTypeface(tf);
        t6.setTypeface(tf);
        t7.setTypeface(tf);
        tutuorial.setTypeface(tf);
        t8.setTypeface(tf);
        checkBox.setTypeface(tf);
        Registration.setTypeface(tf);
    }
    @Override
    public void onPause(){
        super.onPause();

    }
}
