package com.sonicboom.sonicpayvui.models;

public class Settlement {

    public static class SettlementRequest{
        public int HostNo;
    }

    public static class SettlementResponse extends SettlementRequest{
        public String StatusCode;
        public int BatchNo;
        public int BatchCount;
        public int BatchAmount;
        public int RefundCount;
        public int RefundAmount;
    }
}
