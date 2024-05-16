package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class FareBaseExitResult implements Parcelable {
    public String StatusCode;
    public String CardNo;
    public String Token;
    public eCreditCardType CardType;
    public int Amount;
    public String SystemId;
    public TNGInfo tnginfo;
    public EMVInfo emvInfo;
    public QRInfo qrInfo;
    public FareBaseExitResult(){

    }
    protected FareBaseExitResult(Parcel in) {
        StatusCode = in.readString();
        CardNo = in.readString();
        Token = in.readString();
        CardType = in.readParcelable(eCreditCardType.class.getClassLoader());
        tnginfo = in.readParcelable(TNGInfo.class.getClassLoader());
        emvInfo = in.readParcelable(EMVInfo.class.getClassLoader());
        qrInfo = in.readParcelable(QRInfo.class.getClassLoader());

    }

    public static final Creator<FareBaseExitResult> CREATOR = new Creator<FareBaseExitResult>() {
        @Override
        public FareBaseExitResult createFromParcel(Parcel in) {
            return new FareBaseExitResult(in);
        }

        @Override
        public FareBaseExitResult[] newArray(int size) {
            return new FareBaseExitResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(StatusCode);
        parcel.writeString(CardNo);
        parcel.writeString(Token);
        parcel.writeParcelable(CardType,i);
        parcel.writeParcelable(tnginfo,i);
        parcel.writeParcelable(emvInfo,i);
        parcel.writeParcelable(qrInfo,i);
    }
}
