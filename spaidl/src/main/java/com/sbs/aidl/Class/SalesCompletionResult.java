package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class SalesCompletionResult implements Parcelable {
    public String SystemId;
    public String TransactionTrace;
    public String InvoiceNo;
    public int HostNo;
    public int BatchNo;
    public String RRN;
    public String TerminalId;
    public String MerchantId;
    public String StatusCode;

    public SalesCompletionResult() {}

    protected SalesCompletionResult(Parcel in) {
        SystemId = in.readString();
        TransactionTrace = in.readString();
        InvoiceNo = in.readString();
        HostNo = in.readInt();
        BatchNo = in.readInt();
        RRN = in.readString();
        TerminalId = in.readString();
        MerchantId = in.readString();
        StatusCode = in.readString();
    }

    public static final Creator<SalesCompletionResult> CREATOR = new Creator<SalesCompletionResult>() {
        @Override
        public SalesCompletionResult createFromParcel(Parcel in) {
            return new SalesCompletionResult(in);
        }

        @Override
        public SalesCompletionResult[] newArray(int size) {
            return new SalesCompletionResult[size];
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
        parcel.writeString(InvoiceNo);
        parcel.writeInt(HostNo);
        parcel.writeInt(BatchNo);
        parcel.writeString(RRN);
        parcel.writeString(TerminalId);
        parcel.writeString(MerchantId);
        parcel.writeString(StatusCode);
    }
}