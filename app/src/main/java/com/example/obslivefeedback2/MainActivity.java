package com.example.obslivefeedback2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.obslivefeedback2.data.SceneModel;
import com.example.obslivefeedback2.network.WebSocketActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import com.example.obslivefeedback2.ui.main.SectionsPagerAdapter;
import com.example.obslivefeedback2.databinding.ActivityMainBinding;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends WebSocketActivity {

    private ActivityMainBinding binding;
    Context context;
    ArrayList<SceneModel> selectedScenes = new ArrayList();

    public void updateSelectedScenes(ArrayList<SceneModel> selectedScenes){
        this.selectedScenes = selectedScenes;

        SharedPreferences preferences = this.getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("selected_scenes", String.join(",", selectedScenes.stream().filter(p->p.isSelected()).map(s->s.getName()).toArray(String[]::new)));
        edit.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FullscreenActivity.class);
                intent.putParcelableArrayListExtra("SELECTED_SCENES", selectedScenes);

                startActivity(intent);
            }
        });


        if(this.getURL() != "" && this.getKey() != ""){
            Log.d("Websocket Client", "connecting to: "+"wss://"+this.getURL()+"/socket?key="+this.getKey());
            this.initWebSocket("wss://"+this.getURL()+"/socket?key="+this.getKey());

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(this.getWebSocket() == null){
            if(this.getURL() != "" && this.getKey() != ""){
                Log.d("Websocket Client", "connecting to: "+"wss://"+this.getURL()+"/socket?key="+this.getKey());
                this.initWebSocket("wss://"+this.getURL()+"/socket?key="+this.getKey());

            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(this.getWebSocket() == null){
            if(this.getURL() != "" && this.getKey() != ""){
                Log.d("Websocket Client", "connecting to: "+"wss://"+this.getURL()+"/socket?key="+this.getKey());
                this.initWebSocket("wss://"+this.getURL()+"/socket?key="+this.getKey());

            }
        }
    }
}