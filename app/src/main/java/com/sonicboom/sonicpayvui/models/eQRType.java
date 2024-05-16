package com.sonicboom.sonicpayvui.models;

public enum eQRType {
    AliPay(51),
    WeChatPay(52),
    Boost(53),
    GrabPay(54),
    TouchNGo(55),
    vcash(56),
    MaybankPay(57),
    RazerPay(58),
    BigPay(59),
    GigiPay(60),
    Mcash(61),
    UnionPay(62),
    Nets(63),
    CIMBPay(64),
    AeonQR(65),
    AeonPH(66),
    SPayGlobal(67),
    DuitNow(69);

    private final int value;

    private eQRType(int s) {
        value = s;
    }

    public int getValue() {
        return this.value;
    }
}
