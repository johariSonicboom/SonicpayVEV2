package com.sonicboom.sonicpayvui.models;

import com.sbs.aidl.Class.EMVInfo;
import com.sbs.aidl.Class.TNGInfo;

public class ParkingEntry {
    public class ParkingEntryRequest{
        public String CardNo;
        public String HashedPAN;
        public String CardType;
        public String EntryTime;
        public String MinTngBalance;
    }

    public static class ParkingEntryResponse{
        public String CardNo;
        public String HashedPAN;
        public int CardType;
        public String StatusCode;
        public TNGInfo TNGInfo;
        public EMVInfo EMVInfo;
    }
}
