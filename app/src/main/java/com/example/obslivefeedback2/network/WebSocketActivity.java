package com.example.obslivefeedback2.network;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;

abstract public class WebSocketActivity extends AppCompatActivity {
    private WebSocket ws = null;

    public boolean initWebSocket(String url){
        if(ws != null){
            return false;
        }

        try {
            ws = new WebSocketFactory().createSocket(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ws.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        thread.start();


        return true;
    }

    public String getKey(){
        SharedPreferences preferences = this.getSharedPreferences("preferences", MODE_PRIVATE);
        return preferences.getString("key", "");
    }

    public String getURL(){
        SharedPreferences preferences = this.getSharedPreferences("preferences", MODE_PRIVATE);
        String url =  preferences.getString("url", "");
        url = url.replaceFirst("^(http[s]?://)","");
        if(url.charAt(url.length()-1)=='/'){
            url = url.substring(0, url.length()-1);
        }

        return url;
    }

    public boolean getWebSocketConnection(){
        return ws.isOpen();
    }

    public WebSocket getWebSocket(){
        return ws;
    }

}
