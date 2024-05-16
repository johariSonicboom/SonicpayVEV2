package com.sonicboom.sonicpayvui.models;

public class Void {
    public class VoidRequest{
        public int Amount;
        public String TransactionTrace;
    }

    public static class VoidResponse{
        public String SystemId;
        public String StatusCode;
        public int Amount;
        public String TransactionTrace;
        public String ApprovalCode;
        public String RRN;
    }
}
