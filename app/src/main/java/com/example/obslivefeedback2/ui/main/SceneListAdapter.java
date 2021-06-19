package com.example.obslivefeedback2.ui.main;

import android.content.Context;
import android.transition.Scene;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.obslivefeedback2.R;
import com.example.obslivefeedback2.data.SceneModel;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class SceneListAdapter extends BaseAdapter implements View.OnClickListener {
    private ArrayList<SceneModel> scenes;
    Context mContext;

    private static class ViewHolder {
        TextView txtName;
        TextView txtStatus;
        ImageView checkbox;
    }

    public SceneListAdapter(ArrayList<SceneModel> scenes, Context context){
        super();
        this.scenes = scenes;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        int position=(Integer) v.getTag();
        Object object= getItem(position);
        SceneModel scene = (SceneModel) object;

        //Snackbar.make(v, "Clicked on: "+scene.getName(), Snackbar.LENGTH_LONG).show();
    }

    public ArrayList<SceneModel> getScenes(){
        return scenes;
    }


    @Override
    public int getCount() {
        return scenes.size();
    }

    @Override
    public SceneModel getItem(int position) {
        return scenes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        SceneModel sceneModel = this.getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(this.mContext);
            convertView = inflater.inflate(R.layout.list_row, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.txtStatus = (TextView) convertView.findViewById(R.id.status);
            viewHolder.checkbox = (ImageView) convertView.findViewById(R.id.checkbox);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.txtName.setText(sceneModel.getName());
        viewHolder.txtStatus.setText(sceneModel.getStatus());

        if(sceneModel.isSelected()){
            viewHolder.checkbox.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_circle_green));
        }else{
            viewHolder.checkbox.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_circle_gray));
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
