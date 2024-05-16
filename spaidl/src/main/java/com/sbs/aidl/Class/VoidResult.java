package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class VoidResult implements Parcelable {
    public String SystemId;
    public String TransactionTrace;
    public int Amount;
    public String RRN;
    public String ApprovalCode;
    public String StatusCode;

    public VoidResult(){}

    protected VoidResult(Parcel in) {
        SystemId = in.readString();
        TransactionTrace = in.readString();
        Amount = in.readInt();
        RRN = in.readString();
        ApprovalCode = in.readString();
        StatusCode = in.readString();
    }

    public static final Creator<VoidResult> CREATOR = new Creator<VoidResult>() {
        @Override
        public VoidResult createFromParcel(Parcel in) {
            return new VoidResult(in);
        }

        @Override
        public VoidResult[] newArray(int size) {
            return new VoidResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(SystemId);
        parcel.writeString(TransactionTrace);
        parcel.writeInt(Amount);
        parcel.writeString(RRN);
        parcel.writeString(ApprovalCode);
        parcel.writeString(StatusCode);
    }
}