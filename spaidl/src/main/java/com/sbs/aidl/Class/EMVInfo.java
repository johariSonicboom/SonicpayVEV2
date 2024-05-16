package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class EMVInfo implements Parcelable {
    public String TransactionType; // S-Sales, P-PreAuth
    public String AID;
    public int HostNo;
    public int BatchNo;
    public String TransactionTrace;
    public String TerminalId;
    public String MerchantId;
    public String InvoiceNo;
    public String TVR;
    public String RRN;
    public String ApprovalCode;
    public String TC;
    public String PosEntryMode;
    public String CardHolderName;
    public String CardLabel;
    public EMVInfo(){}
    protected EMVInfo(Parcel in) {
        TransactionType = in.readString();
        AID = in.readString();
        HostNo = in.readInt();
        BatchNo = in.readInt();
        TransactionTrace = in.readString();
        TerminalId = in.readString();
        MerchantId = in.readString();
        InvoiceNo = in.readString();
        TVR = in.readString();
        RRN = in.readString();
        ApprovalCode = in.readString();
        TC = in.readString();
        PosEntryMode = in.readString();
        CardHolderName = in.readString();
        CardLabel = in.readString();
    }

    public static final Creator<EMVInfo> CREATOR = new Creator<EMVInfo>() {
        @Override
        public EMVInfo createFromParcel(Parcel in) {
            return new EMVInfo(in);
        }

        @Override
        public EMVInfo[] newArray(int size) {
            return new EMVInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(TransactionType);
        parcel.writeString(AID);
        parcel.writeInt(HostNo);
        parcel.writeInt(BatchNo);
        parcel.writeString(TransactionTrace);
        parcel.writeString(TerminalId);
        parcel.writeString(MerchantId);
        parcel.writeString(InvoiceNo);
        parcel.writeString(TVR);
        parcel.writeString(RRN);
        parcel.writeString(ApprovalCode);
        parcel.writeString(TC);
        parcel.writeString(PosEntryMode);
        parcel.writeString(CardHolderName);
        parcel.writeString(CardLabel);
    }
}
