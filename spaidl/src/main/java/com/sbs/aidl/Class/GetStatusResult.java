package com.sbs.aidl.Class;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;


public class GetStatusResult  implements Parcelable{
    public eTerminalState TerminalState;
    public boolean IsCardPresent;
    public GetStatusResult(){}

    protected GetStatusResult(Parcel in) {
        TerminalState =  in.readParcelable(eTerminalState.class.getClassLoader());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            IsCardPresent = in.readBoolean();
        }

    }

    public static final Creator<GetStatusResult> CREATOR = new Creator<GetStatusResult>() {
        @Override
        public GetStatusResult createFromParcel(Parcel in) {
            return new GetStatusResult(in);
        }

        @Override
        public GetStatusResult[] newArray(int size) {
            return new GetStatusResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeParcelable(TerminalState,i);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(IsCardPresent);
        }

    }
}