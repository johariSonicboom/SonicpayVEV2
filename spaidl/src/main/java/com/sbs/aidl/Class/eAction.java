package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public enum eAction implements Parcelable {
    Cancel(-1),
    InstantSales(0),
    DelayedSales(1);

    private final int Action;

    eAction(int action) {
        this.Action = action;
    }
    public static eAction fromId(int id) {
        for (eAction type : values()) {
            if (type.getValue() == id) {
                return type;
            }
        }
        return Cancel; //default return cancel
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<eAction> CREATOR = new Creator<eAction>() {
        @Override
        public eAction createFromParcel(Parcel in) {
            return eAction.valueOf(in.readString());
        }

        @Override
        public eAction[] newArray(int size) {
            return new eAction[size];
        }
    };

    public int getValue() {
        Log.i("TAG", "getValue: " + Action);
        return Action;
    }
}
