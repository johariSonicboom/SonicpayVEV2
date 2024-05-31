package com.sonicboom.sonicpayvui.EVModels;

import java.util.Date;
import java.util.List;

public class Component {
    public int ComponentId;
    public String ComponentCode;
    public String ComponentName;
    public String Aegment;
//    public String Status;
    public String TerminalIP;
    public int TerminalPort;
    public int Zone;
    public String Command;
    public double PreAuthAmount;
    public List<Connector> Connectors;
    public Date StartChargeTime;
    public int SelectedConnector;
    public String FareChargeText;
    public String FareChargeDescription;
    public Component() {
        StartChargeTime =null;
        // Default constructor
    }
}
