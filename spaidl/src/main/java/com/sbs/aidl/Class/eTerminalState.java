package com.sbs.aidl.Class;

import android.os.Parcel;
import android.os.Parcelable;

public enum eTerminalState implements Parcelable {

        Idle (0),
        WaitingForCard  (10),
        RemoveCard (11),
        ChipCardReading (20),
        ContactlessCardReading (21),
        AppSelecting (30),
        PINEntering (40),
        HostConnecting (50),
        SeePhone (60),
        TapAgain (70),
        PresentOneCard (71),
        TerminalBusy (90);


    private final int TerminalState;
    eTerminalState() {
        this.TerminalState = 0;
    }
    eTerminalState(int terminalState) {
        this.TerminalState = terminalState;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<eTerminalState> CREATOR = new Creator<eTerminalState>() {
        @Override
        public eTerminalState createFromParcel(Parcel in) {
            return eTerminalState.valueOf(in.readString());
        }

        @Override
        public eTerminalState[] newArray(int size) {
            return new eTerminalState[size];
        }
    };

    public int getValue() {
        return TerminalState;
    }



}
