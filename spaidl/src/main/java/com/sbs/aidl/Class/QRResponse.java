package com.sbs.aidl.Class;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class QRResponse implements Parcelable{
    public String ReferenceNo;
    public String QRCode;
    public QRList[] qrList;
    public int QRType;
    public String QRName;

    public QRResponse(){
        qrList = new QRList[0];
        QRCode ="";
        ReferenceNo="";
    }

    public QRResponse(Parcel in) {

        ReferenceNo = in.readString();
        QRCode = in.readString();

        qrList=in.createTypedArray(QRList.CREATOR);

        QRType = in.readInt();
        QRName = in.readString();
    }

    public static final Parcelable.Creator<QRResponse> CREATOR = new Parcelable.Creator<QRResponse>() {
        @Override
        public QRResponse createFromParcel(Parcel in) {
            return new QRResponse(in);
        }

        @Override
        public QRResponse[] newArray(int size) {
            return new QRResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(ReferenceNo);
        parcel.writeString(QRCode);
        parcel.writeTypedArray(qrList,i);
        parcel.writeInt(QRType);
        parcel.writeString(QRName);
    }
}
