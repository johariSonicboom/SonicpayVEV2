package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SettlementResult implements Parcelable {
    public int HostNo;
    public String StatusCode;
    public int BatchNo;
    public int BatchCount;
    public int BatchAmount;
    public int RefundCount;
    public int RefundAmount;
    public SettlementResult(){}
    public SettlementResult(Parcel in) {
        HostNo = in.readInt();
        StatusCode = in.readString();
        BatchNo =  in.readInt();
        BatchCount =  in.readInt();
        BatchAmount =  in.readInt();
        RefundCount =  in.readInt();
        RefundAmount =  in.readInt();
    }

    public static final Creator<SettlementResult> CREATOR = new Creator<SettlementResult>() {
        @Override
        public SettlementResult createFromParcel(Parcel in) {
            return new SettlementResult(in);
        }

        @Override
        public SettlementResult[] newArray(int size) {
            return new SettlementResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(HostNo);
        parcel.writeString(StatusCode);
        parcel.writeInt(BatchNo);
        parcel.writeInt(BatchCount);
        parcel.writeInt(BatchAmount);
        parcel.writeInt(RefundCount);
        parcel.writeInt(RefundAmount);
    }
}
