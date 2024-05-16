package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class SalesResult implements Parcelable {
    public String SystemId;
    public String StatusCode;
    public String CardNo;
    public String Token;
    public eCreditCardType CardType;
    public TNGInfo tnginfo;
    public EMVInfo emvInfo;
    public QRInfo qrInfo;

    public SalesResult(){CardType= eCreditCardType.Unknown;}

    protected SalesResult(Parcel in) {
        SystemId = in.readString();
        StatusCode = in.readString();
        CardNo = in.readString();
        Token = in.readString();
        CardType = in.readParcelable(eCreditCardType.class.getClassLoader());
        tnginfo = in.readParcelable(TNGInfo.class.getClassLoader());
        emvInfo = in.readParcelable(EMVInfo.class.getClassLoader());
        qrInfo = in.readParcelable(QRInfo.class.getClassLoader());
    }

    public static final Creator<SalesResult> CREATOR = new Creator<SalesResult>() {
        @Override
        public SalesResult createFromParcel(Parcel in) {
            return new SalesResult(in);
        }

        @Override
        public SalesResult[] newArray(int size) {
            return new SalesResult[size];
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
        parcel.writeParcelable(CardType,i);
        parcel.writeParcelable(tnginfo, i);
        parcel.writeParcelable(emvInfo, i);
        parcel.writeParcelable(qrInfo, i);
    }
}
