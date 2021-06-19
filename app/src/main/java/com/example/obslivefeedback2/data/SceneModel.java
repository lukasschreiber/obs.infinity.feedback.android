package com.example.obslivefeedback2.data;

import android.os.Parcel;
import android.os.Parcelable;

public class SceneModel implements Parcelable {
    String name;
    String status;
    boolean selected;

    public SceneModel(String name, String status, boolean selected){
        this.name = name;
        this.status = status;
        this.selected = selected;
    }

    protected SceneModel(Parcel in) {
        name = in.readString();
        status = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<SceneModel> CREATOR = new Creator<SceneModel>() {
        @Override
        public SceneModel createFromParcel(Parcel in) {
            return new SceneModel(in);
        }

        @Override
        public SceneModel[] newArray(int size) {
            return new SceneModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.status);
        dest.writeInt(this.selected ? 1 : 0);
    }
}
