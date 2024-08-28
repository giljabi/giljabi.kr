package kr.giljabi.api.controller.google;

import com.google.gson.annotations.SerializedName;

/**
 * @Author : eahn.park@gmail.com
 * @Date : 2024.06.17
 * @Description
 */
public class RouteRequest {
    @SerializedName("origin")
    Waypoint origin;
    @SerializedName("destination")
    Waypoint destination;
    @SerializedName("travelMode")
    String travelMode;

    public RouteRequest(Waypoint origin, Waypoint destination, String travelMode) {
        this.origin = origin;
        this.destination = destination;
        this.travelMode = travelMode;
    }
}

