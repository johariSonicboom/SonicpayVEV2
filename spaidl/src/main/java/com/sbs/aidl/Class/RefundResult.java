package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class RefundResult implements Parcelable {
    public String SystemId;
    public String TransactionTrace;
    public int Amount;
    public String RRN;
    public String ApprovalCode;
    public String StatusCode;

    public RefundResult(){}

    protected RefundResult(Parcel in) {
        SystemId = in.readString();
        TransactionTrace = in.readString();
        Amount = in.readInt();
        RRN = in.readString();
        ApprovalCode = in.readString();
        StatusCode = in.readString();
    }

    public static final Creator<RefundResult> CREATOR = new Creator<RefundResult>() {
        @Override
        public RefundResult createFromParcel(Parcel in) {
            return new RefundResult(in);
        }

        @Override
        public RefundResult[] newArray(int size) {
            return new RefundResult[size];
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