package com.sonicboom.sonicpayvui.EVModels;

//- SalesCompletion Status:
//        - "I", Initial value
//        - "S", Success
//        - "F", Failed
//        - "E", No of retries exceeded

import java.sql.Date;
import java.util.Objects;

public class TransactionTableDB {
    public String ComponentCode;
    public String TransactionTrace;
    public String CardNumber;
    public String HashPAN;
    public int SystemPaymentId;
    public int CardType;
    public String PhoneNumber;
    public String MID;
    public String TID;
    public String AuthCode;
    public String AID;
    public String RNN;
    public int Connector;

    public double Amount;
    public int TxId;
    public String CustumErrorMessage;
    public String ChargingPeriod;
    public String Status;
    public int NoOfRetries;

    public String ReceiveSalesCompletionDateTime;

    @Override
    public String toString() {
        return "TransactionTableDB{" +
                "ComponentCode='" + ComponentCode + '\'' +
                ", TransactionTrace='" + TransactionTrace + '\'' +
                ", CardNumber='" + CardNumber + '\'' +
                ", HashPAN='" + HashPAN + '\'' +
                ", SystemPaymentId=" + SystemPaymentId +
                ", CardType=" + CardType +
                ", PhoneNumber='" + PhoneNumber + '\'' +
                ", MID='" + MID + '\'' +
                ", TID='" + TID + '\'' +
                ", AuthCode='" + AuthCode + '\'' +
                ", AID='" + AID + '\'' +
                ", RNN='" + RNN + '\'' +
                ", Connector=" + Connector +
                ", Amount=" + Amount +
                ", TxId=" + TxId +
                ", CustumErrorMessage='" + CustumErrorMessage + '\'' +
                ", ChargingPeriod='" + ChargingPeriod + '\'' +
                ", Status='" + Status + '\'' +
                ", NoOfRetries=" + NoOfRetries +
                ", ReceiveSalesCompletionDateTime=" + ReceiveSalesCompletionDateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionTableDB that = (TransactionTableDB) o;
        return Objects.equals(TransactionTrace, that.TransactionTrace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(TransactionTrace);
    }

}


