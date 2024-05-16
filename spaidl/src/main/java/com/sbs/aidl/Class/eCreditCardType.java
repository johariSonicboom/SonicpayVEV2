package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public enum eCreditCardType implements Parcelable {
    Unknown(0),
    // cater for sonicpay ver1.2.0 above
    AmericanExpress(11),
    DinersClub(12),
    MasterCard(13),
    VisaCard(14),
    JCBCard(15),
    MyDebitCard(16),
    UnionPay(17),
    TNGCard(31),

    AliPay_QR(51),
    WeChatPay_QR(52),
    Boost_QR(53),
    GrabPay_QR(54),
    TouchNGo_QR(55),
    VCash_QR(56),
    MaybankPay_QR(57),
    RazerPay_QR(58),
    BigPay_QR(59),
    GigiPay_QR(60),
    Mcash_QR(61),
    UnionPay_QR(62),
    Nets_QR(63),
    CIMBPay_QR(64),
    Aeon_QR(65),
    AeonPH_QR(66),
    SPAY_GLOBAL_QR(67),
    DuitNow_QR(69);

    private final int CardType;

    eCreditCardType(int cardType) {
        this.CardType = cardType;
    }
    public static eCreditCardType fromId(int id) {
        for (eCreditCardType type : values()) {
            if (type.getValue() == id) {
                return type;
            }
        }
        return null;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<eCreditCardType> CREATOR = new Creator<eCreditCardType>() {
        @Override
        public eCreditCardType createFromParcel(Parcel in) {
            return eCreditCardType.valueOf(in.readString());
        }

        @Override
        public eCreditCardType[] newArray(int size) {
            return new eCreditCardType[size];
        }
    };

    public int getValue() {
        Log.i("TAG", "getValue: " +CardType);
        return CardType;
    }








}
