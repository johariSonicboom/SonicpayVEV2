package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class QRTransactionResult implements Parcelable {

    public int SystemId;
    public int TerminalId;

    public String Status;
    public QRInfo qrInfo;
    public int QRType;
    public QRTransactionResult(){}
    public QRTransactionResult(Parcel in) {
        SystemId = in.readInt();
        TerminalId = in.readInt();
        Status = in.readString();
        qrInfo = in.readParcelable(QRInfo.class.getClassLoader());
        QRType= in.readInt();
    }

    public static final Creator<QRTransactionResult> CREATOR = new Creator<QRTransactionResult>() {
        @Override
        public QRTransactionResult createFromParcel(Parcel in) {
            return new QRTransactionResult(in);
        }

        @Override
        public QRTransactionResult[] newArray(int size) {
            return new QRTransactionResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(SystemId);
        parcel.writeInt(TerminalId);
        parcel.writeString(Status);
        parcel.writeParcelable(qrInfo,i);
        parcel.writeInt(QRType);
    }
}
