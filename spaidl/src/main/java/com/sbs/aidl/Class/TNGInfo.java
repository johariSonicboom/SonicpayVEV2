package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public final class TNGInfo implements Parcelable {

    public int BatchNo;

    public String TerminalId;
    public int CardBalance;
    public int CardTrans;
    public Date CardExpiryDate;
    public int TxnId;
    public String TransactionTrace;

    public TNGInfo(){}

    protected TNGInfo(Parcel in) {
        BatchNo = in.readInt();
        TerminalId = in.readString();
        CardBalance = in.readInt();
        CardTrans = in.readInt();
        TxnId = in.readInt();
        TransactionTrace = in.readString();
        CardExpiryDate = in.readParcelable(Date.class.getClassLoader());
    }

    public static final Creator<TNGInfo> CREATOR = new Creator<TNGInfo>() {
        @Override
        public TNGInfo createFromParcel(Parcel in) {
            return new TNGInfo(in);
        }

        @Override
        public TNGInfo[] newArray(int size) {
            return new TNGInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(BatchNo);
        parcel.writeString(TerminalId);
        parcel.writeInt(CardBalance);
        parcel.writeInt(CardTrans);
        parcel.writeInt(TxnId);
        parcel.writeString(TransactionTrace);
       // parcel.writeParcelable(CardExpiryDate,i);
    }
}
