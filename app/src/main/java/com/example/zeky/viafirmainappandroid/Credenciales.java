package com.example.zeky.viafirmainappandroid;

/**
 * Created by Zeky on 28/7/15.
 */
public class Credenciales {
    private String url;
    private String key;
    private String pass;

    public Credenciales(){
        this.url = "http://testservices.viafirma.com/viafirma";
        this.key = "xnoccio";
        this.pass = "12345";
    }

    public String getUrl(){
        return this.url;
    }

    public void setUrl(String newUrl){
        this.url = newUrl;
    }

    public String getKey(){
        return this.key;
    }

    public void setKey(String newKey){
        this.key = newKey;
    }

    public String getPass(){
        return this.pass;
    }

    public void setPass(String newPass){
        this.pass = newPass;
    }
}
