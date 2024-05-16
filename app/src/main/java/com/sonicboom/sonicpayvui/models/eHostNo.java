package com.sonicboom.sonicpayvui.models;

public enum eHostNo {
    Visa_Master(1),
    Amex(2),
    MyDebit(3),
    China_UnionPay(4),
    PBB_Visa(11),
    PBB_Master(12),
    PBB_MCCS(13),
    PBB_Visa_DR(14),
    PBB_Master_DR(15),
    TouchNGo(99);

    private final int value;

    private eHostNo(int s) {
        value = s;
    }

    public int getValue() {
        return this.value;
    }
}
