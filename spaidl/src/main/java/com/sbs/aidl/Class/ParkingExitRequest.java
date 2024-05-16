package com.sbs.aidl.Class;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParkingExitRequest {
    public ReadCardResult ReadCardResult;
    public int TotalAmount;
    public int FareAmount;
    public int SurchargeAmount;
    public int SurchargeTaxAmount;
    public int TaxAmount;
    public int PreValidationAmount;
    public String EntryTime;
    public String ExitTime;
    public String TxnId;
    public ParkingExitRequest(){}
    public ParkingExitRequest(ReadCardResult ReadCardResult, int TotalAmount, int FareAmount, int SurchargeAmount, int TaxAmount, int SurchargeTax, int PreValidationAmount, String EntryTime, String ExitTime, String TxnId) {
        this.ReadCardResult = ReadCardResult;
        this.TotalAmount = TotalAmount;
        this.FareAmount = FareAmount;
        this.SurchargeAmount = SurchargeAmount;
        this.TaxAmount = TaxAmount;
        this.PreValidationAmount = PreValidationAmount;
        this.EntryTime = EntryTime;
        this.ExitTime = ExitTime;
        this.TxnId = TxnId;
        this.SurchargeTaxAmount = SurchargeTax;
    }

    public Date GetEntryTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(EntryTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Date GetExitTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        try {
            return sdf.parse(ExitTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
