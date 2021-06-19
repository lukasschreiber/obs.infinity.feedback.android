package com.example.obslivefeedback2.ui.main;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.obslivefeedback2.MainActivity;
import com.example.obslivefeedback2.R;
import com.example.obslivefeedback2.data.SceneModel;
import com.google.android.material.snackbar.Snackbar;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class ListFragment extends Fragment {
    View view;
    Integer seconds = null;
    ArrayList<SceneModel> scenes = new ArrayList<SceneModel>();
    SceneListAdapter adapter;
    TextView banner_text;
    LinearLayout banner;
    ImageButton button_clear;
    String[] selectedScenesArray = new String[0];

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        banner = (LinearLayout) view.findViewById(R.id.banner);
        banner_text = (TextView) view.findViewById(R.id.banner_text);
        button_clear = (ImageButton) view.findViewById(R.id.button_clear);

        ListView listView = (ListView) view.findViewById(R.id.list_view);


        adapter = new SceneListAdapter(scenes, getContext());

        SharedPreferences preferences = getActivity().getSharedPreferences("preferences", MODE_PRIVATE);
        String selecetedScenes = preferences.getString("selected_scenes", null);
        if(selecetedScenes != null){
            selectedScenesArray = selecetedScenes.split(",");
        }

        //I'm feeling very bad for this line ):
        if(((MainActivity)getActivity()).getWebSocket() != null) {
            ((MainActivity) getActivity()).getWebSocket().addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    Log.d("Socket", text);
                    JSONObject json = new JSONObject(text);

                    if (json.has("message")) {
                        Log.d("Messenger", text);
                        banner_text.setText(json.getString("message"));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                banner.setVisibility(View.VISIBLE);
                            }
                        });


                        new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                banner.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                },
                                10000
                        );
                    }

                    if (json.has("scenes")) {
                        JSONArray scenesJson = json.getJSONObject("scenes").getJSONArray("payload");

                        scenes.clear();
                        for (int i = 0; i < json.getJSONObject("scenes").getInt("count"); i++) {
                            JSONObject scene = scenesJson.getJSONObject(i);
                            String status = "";
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
                            final String _status = status;
                            final String _name = scene.getString("name");
                            final boolean _active = Arrays.stream(selectedScenesArray).anyMatch(_name::equals);

                            ((MainActivity) getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scenes.add(new SceneModel(_name, _status, _active));
                                    adapter.notifyDataSetChanged(); //Update listview in Ui thread
                                }
                            });
                            ((MainActivity) getActivity()).updateSelectedScenes(scenes);

                        }


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


        button_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        banner.setVisibility(View.GONE);
                    }
                });
            }
        });

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SceneModel s = scenes.get(position);
                s.setSelected(!s.isSelected());
                scenes.set(position, s);
                ((MainActivity)getActivity()).updateSelectedScenes(scenes);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged(); //Update listview in Ui thread
                    }
                });
            }
        });


        runTimer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, container, false);

        return view;
    }

    private void runTimer() {

        final TextView timeView = (TextView) view.findViewById(R.id.timer);

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override

            public void run() {
                if(seconds != null) {
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;

                    String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

                    timeView.setText(time);

                    seconds++;

                }else{
                    timeView.setText("0:00:00");

                }

                handler.postDelayed(this, 1000);
            }
        });
    }
}
