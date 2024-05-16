package com.sbs.aidl.Class;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParkingEntryRequest
{
    public ReadCardResult ReadCardResult;
    public int MinBalance;
    public String EntryTime;

    public ParkingEntryRequest(){}

    public ParkingEntryRequest( ReadCardResult readCardResult,int minBalance,String entryTime){
        ReadCardResult= readCardResult;
        MinBalance=minBalance;
        EntryTime=entryTime;
    }
    public Date GetEntryTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        try {
            return sdf.parse(EntryTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}

