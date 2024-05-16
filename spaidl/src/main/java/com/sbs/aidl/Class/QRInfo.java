package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class QRInfo implements Parcelable {
    public String TransactionTrace;
    public String TerminalId;
    public String MerchantId;
    public String ApprovalCode;
    public  QRInfo(){}
    protected QRInfo(Parcel in) {
        TransactionTrace = in.readString();
        TerminalId = in.readString();
        MerchantId = in.readString();
        ApprovalCode = in.readString();
    }

    public static final Creator<QRInfo> CREATOR = new Creator<QRInfo>() {
        @Override
        public QRInfo createFromParcel(Parcel in) {
            return new QRInfo(in);
        }

        @Override
        public QRInfo[] newArray(int size) {
            return new QRInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(TransactionTrace);
        parcel.writeString(TerminalId);
        parcel.writeString(MerchantId);
        parcel.writeString(ApprovalCode);
    }
}
