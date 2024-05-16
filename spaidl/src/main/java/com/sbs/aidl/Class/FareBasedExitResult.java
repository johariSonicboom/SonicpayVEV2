package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class FareBasedExitResult implements Parcelable {
    public String StatusCode;
    public String CardNo;
    public String Token;
    public eCreditCardType CardType;
    public int Amount;
    public String SystemId;
    public TNGInfo tnginfo;
    public EMVInfo emvInfo;
    public QRInfo qrInfo;
    public FareBasedExitResult(){

    }
    protected FareBasedExitResult(Parcel in) {
        SystemId = in.readString();
        StatusCode = in.readString();
        CardNo = in.readString();
        Token = in.readString();
        Amount = in.readInt();
        CardType = in.readParcelable(eCreditCardType.class.getClassLoader());
        tnginfo = in.readParcelable(TNGInfo.class.getClassLoader());
        emvInfo = in.readParcelable(EMVInfo.class.getClassLoader());
        qrInfo = in.readParcelable(QRInfo.class.getClassLoader());

    }

    public static final Creator<FareBasedExitResult> CREATOR = new Creator<FareBasedExitResult>() {
        @Override
        public FareBasedExitResult createFromParcel(Parcel in) {
            return new FareBasedExitResult(in);
        }

        @Override
        public FareBasedExitResult[] newArray(int size) {
            return new FareBasedExitResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(SystemId);
        parcel.writeString(StatusCode);
        parcel.writeString(CardNo);
        parcel.writeString(Token);
        parcel.writeInt(Amount);
        parcel.writeParcelable(CardType,i);
        parcel.writeParcelable(tnginfo,i);
        parcel.writeParcelable(emvInfo,i);
        parcel.writeParcelable(qrInfo,i);
    }
}
