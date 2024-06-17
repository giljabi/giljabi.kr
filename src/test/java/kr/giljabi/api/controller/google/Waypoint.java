package kr.giljabi.api.controller.google;

import com.google.gson.annotations.SerializedName;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.06.17
 * @Description
 */
public class Waypoint {
    @SerializedName("location")
    Location location;

    public Waypoint(Location location) {
        this.location = location;
    }
}