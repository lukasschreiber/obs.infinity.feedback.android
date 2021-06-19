package com.example.obslivefeedback2;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.obslivefeedback2.data.SceneModel;
import com.example.obslivefeedback2.databinding.ActivityFullscreenBinding;
import com.example.obslivefeedback2.network.WebSocketActivity;
import com.google.android.flexbox.FlexboxLayout;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends WebSocketActivity {

    private long startTime=0;
    private long endTime=0;
    ArrayList<SceneModel> selectedScenes;
    TextView fullscreenBannerText;
    TextView timeView;
    RelativeLayout fullscreenBanner;
    Integer seconds;
    View view;
    Context context;
    FlexboxLayout flex;
    ImageButton close_message;

    private ActivityFullscreenBinding binding;

    private boolean isEnabledMessages(){
        SharedPreferences preferences = this.getSharedPreferences("preferences", MODE_PRIVATE);
        return preferences.getBoolean("enable_messages", false);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        selectedScenes = intent.getParcelableArrayListExtra("SELECTED_SCENES");

        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        view = binding.getRoot();
        context = this;

        fullscreenBanner = (RelativeLayout) binding.getRoot().findViewById(R.id.fullscreen_banner);
        fullscreenBannerText = (TextView) binding.getRoot().findViewById(R.id.fullscreen_banner_text);
        timeView = (TextView) binding.getRoot().findViewById(R.id.fullscreen_timer);
        flex = (FlexboxLayout) binding.getRoot().findViewById(R.id.fullscreen_flex);
        close_message = (ImageButton) binding.getRoot().findViewById(R.id.close_fullscreen_banner);

        Log.d("Fullscreen", "Active Scenes: "+String.join(",", selectedScenes.stream().filter(p->p.isSelected()).map(s->s.getName()).toArray(String[]::new)));

        close_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fullscreenBanner.setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(this.getURL() != "" && this.getKey() != ""){
            Log.d("Websocket Client", "connecting to: "+"wss://"+this.getURL()+"/socket?key="+this.getKey());
            this.initWebSocket("wss://"+this.getURL()+"/socket?key="+this.getKey());

        }

        runTimer();

        if(this.getWebSocket() != null){
            this.getWebSocket().addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    Log.d("Fullscreen Socket", text);
                    JSONObject json = new JSONObject(text);

                    if (json.has("message") && isEnabledMessages()) {
                        Log.d("Fullscreen Messenger", text);

                        final String msg = json.getString("message");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fullscreenBannerText.setText(msg);
                                fullscreenBanner.setVisibility(View.VISIBLE);
                            }
                        });

                        new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                fullscreenBanner.setVisibility(View.GONE);
                                            }
                                        });

                                    }
                                },
                                10000
                        );
                    }


                    if (json.has("scenes")) {
                        JSONArray scenesJson = json.getJSONObject("scenes").getJSONArray("payload");

                        ArrayList<SceneModel> renderScenes = new ArrayList<>();

                        for (int i = 0; i < json.getJSONObject("scenes").getInt("count"); i++) {
                            JSONObject scene = scenesJson.getJSONObject(i);
                            String status = "";
                            String name = scene.getString("name");
                            switch (scene.getInt("status")) {
                                case 1:
                                    status = "active";
                                    break;
                                case 2:
                                    status = "preview";
                                    break;
                                default:
                                    status = "";
                                    break;
                            }

                            if(selectedScenes.stream().filter(p->p.isSelected()).anyMatch(p->p.getName().equals(name))){
                                renderScenes.add(new SceneModel(name, status, true));
                            }

                        }

                        Collections.sort(renderScenes, (SceneModel s1, SceneModel s2)->s1.getName().compareTo(s2.getName()));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                flex.removeAllViews();
                                for(SceneModel scene : renderScenes){
                                    Log.d("Scene Renderer", "render "+scene.getName());

                                    LinearLayout wrapper = new LinearLayout(context);
                                    if(scene.getStatus().equals("active"))
                                        wrapper.setBackground(context.getDrawable(R.drawable.shape_active));
                                    else if(scene.getStatus().equals("preview"))
                                        wrapper.setBackground(context.getDrawable(R.drawable.shape_preview));
                                    else
                                        wrapper.setBackground(context.getDrawable(R.drawable.shape));

                                    FlexboxLayout.LayoutParams wrapperLayoutParams = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
                                    wrapperLayoutParams.setMargins(5,5,5,5);
                                    wrapperLayoutParams.setFlexGrow(1);
                                    final float scale = context.getResources().getDisplayMetrics().density;
                                    int pixels = (int) (250 * scale + 0.5f);
                                    wrapper.setMinimumWidth(pixels);

                                    TextView title = new TextView(context);
                                    title.setText(scene.getName());
                                    LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    titleLayoutParams.setMargins(0, 80, 0,0);
                                    title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                                    title.setTypeface(Typeface.MONOSPACE);


                                    wrapper.addView(title, titleLayoutParams);
                                    flex.addView(wrapper, wrapperLayoutParams);
                                }
                            }
                        });

                    }

                    if (json.has("stream")) {
                        Log.d("Stream Timer", json.getJSONObject("stream").getString("status"));
                        if (json.getJSONObject("stream").getString("status").equals("live")) {
                            Log.d("Stream Timer", "ms: " + json.getJSONObject("stream").getLong("start"));
                            try {

                                long start = json.getJSONObject("stream").getLong("start");
                                long now = System.currentTimeMillis();
                                int uptime = (int) (now - start) / 1000;
                                seconds = uptime;
                                Log.d("Stream Timer", "Time: " + uptime);
                            } catch (Exception e) {
                                Log.d("Stream Timer", e.getMessage());
                            }

                        } else {
                            seconds = null;
                        }
                    }
                }


            });

        }
    }

    private void runTimer() {
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override

            public void run() {

                if(seconds != null) {
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;

                    String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeView.setText("Live: "+time);
                        }
                    });

                    seconds++;

                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeView.setText("Live: 0:00:00");
                        }
                    });
                }

                handler.postDelayed(this, 1000);
            }
        });
    }


}