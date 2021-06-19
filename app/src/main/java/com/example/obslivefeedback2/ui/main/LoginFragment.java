package com.example.obslivefeedback2.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.obslivefeedback2.MainActivity;
import com.example.obslivefeedback2.R;
import com.example.obslivefeedback2.utils.FragmentIntentIntegrator;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import static android.content.Context.MODE_PRIVATE;

public class LoginFragment extends Fragment implements  View.OnClickListener{
    View view;
    private Button scan;
    private EditText textViewServer, textViewKey;

    private FragmentIntentIntegrator ZXINGScanner;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);

        scan = (Button)view.findViewById(R.id.scan);
        textViewServer = (EditText)view.findViewById(R.id.server);
        textViewKey = (EditText)view.findViewById(R.id.key);
        Switch enable_messages = (Switch)view.findViewById(R.id.enable_messages);
        SharedPreferences preferences = getActivity().getSharedPreferences("preferences", MODE_PRIVATE);
        textViewServer.setText(preferences.getString("url", ""));
        textViewKey.setText(preferences.getString("key", ""));
        enable_messages.setChecked(preferences.getBoolean("enable_messages", false));

        textViewServer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveSettings();
            }
        });
        textViewKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveSettings();
            }
        });
        enable_messages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSettings();
            }
        });

        ZXINGScanner = new FragmentIntentIntegrator(this);
        scan.setOnClickListener(this);

        return view;
    }

    private void saveSettings(){
        EditText key = view.findViewById(R.id.key);
        EditText url = view.findViewById(R.id.server);
        Switch show_messages = view.findViewById(R.id.enable_messages);
        SharedPreferences preferences = getActivity().getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor edit= preferences.edit();
        edit.putString("key", key.getText().toString());
        edit.putString("url", url.getText().toString());
        edit.putBoolean("enable_messages", show_messages.isChecked());
        edit.commit();

        ((MainActivity)getActivity()).initWebSocket("wss://"+((MainActivity)getActivity()).getURL()+"/socket?key="+((MainActivity)getActivity()).getKey());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.d("GRCode", result.getContents());
        if (result != null) {
            if (result.getContents() == null) {
                Snackbar.make(view, "Result Not Found", Snackbar.LENGTH_LONG).show();
            } else {
                try {
                    JSONObject obj = new JSONObject(result.getContents());
                    textViewServer.setText(obj.getString("url"));
                    textViewKey.setText(obj.getString("key"));

                    this.saveSettings();
                } catch (JSONException e) {
                    e.printStackTrace();

                    Snackbar.make(view, result.getContents(), Snackbar.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        ZXINGScanner.setCaptureActivity(CaptureActivityPortrait.class);
        ZXINGScanner.setPrompt("");
        ZXINGScanner.setBeepEnabled(false);
        ZXINGScanner.setOrientationLocked(false);
        ZXINGScanner.initiateScan();
    }
}
