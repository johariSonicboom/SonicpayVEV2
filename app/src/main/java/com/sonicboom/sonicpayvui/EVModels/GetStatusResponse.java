package com.sonicboom.sonicpayvui.EVModels;

import java.util.List;

public class GetStatusResponse {
    public String ComponentCode;
    public int ConnectorId;
    public String Status;
    public String Description;
    public String FareChargeText;
    public String DescriptionText;
    public List<Connector> Connectors;

    // Default constructor
    public GetStatusResponse() {
    }
}
