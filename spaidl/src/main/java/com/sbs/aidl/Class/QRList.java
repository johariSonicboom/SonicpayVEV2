package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class QRList implements Parcelable {
    public int QRProviderID;
    public String QRProviderName;
    public QRList(String qrprovidername, int qrproviderid) {
        QRProviderID=qrproviderid;
        QRProviderName=qrprovidername;
    }

    public QRList(Parcel in) {
        QRProviderID = in.readInt();
        QRProviderName = in.readString();


    }

    public static final Parcelable.Creator<QRList> CREATOR = new Parcelable.Creator<QRList>() {
        @Override
        public QRList createFromParcel(Parcel in) {
            return new QRList(in);
        }

        @Override
        public QRList[] newArray(int size) {
            return new QRList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(QRProviderID);

        parcel.writeString(QRProviderName);

    }
}
