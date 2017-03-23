package com.elabs.aduinoandiot;

/**
 * Created by Tanmay on 20-03-2017.
 */

public class Profile {

    private String id,password;
   private int value=0;

    public Profile(String id, String password, int value){
        this.id = id;
        this.password=password;
        this.value=value;
    }

    public Profile(){};

    public String getId(){
        return id;
    }

    public String getPassword(){
        return password;
    }

    public int getValue(){
        return value;
    }
}
