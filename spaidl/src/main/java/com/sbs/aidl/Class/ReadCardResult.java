package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class ReadCardResult implements Parcelable {
    public String CardNo;
    public String CardUID;
    public String Token;
    public String CardStatus;
    public eCreditCardType CardType;
    public ReadCardResult(){
        CardType= eCreditCardType.Unknown;
    }
    public ReadCardResult(Parcel in) {
        CardNo = in.readString();
        Token = in.readString();
        CardType = in.readParcelable(eCreditCardType.class.getClassLoader());
        CardStatus = in.readString();
        CardUID = in.readString();
    }

    public static final Creator<ReadCardResult> CREATOR = new Creator<ReadCardResult>() {
        @Override
        public ReadCardResult createFromParcel(Parcel in) {
            return new ReadCardResult(in);
        }

        @Override
        public ReadCardResult[] newArray(int size) {
            return new ReadCardResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(CardNo);
        parcel.writeString(Token);
        parcel.writeParcelable(CardType,i);
        parcel.writeString(CardStatus);
        parcel.writeString(CardUID);
    }
}
