package kr.giljabi.api.controller.google;

import com.google.gson.annotations.SerializedName;

/**
 * @Author : eahn.park@gmail.com
 * @Date : 2024.06.17
 * @Description
 */
public class Location {
    @SerializedName("latLng")
    LatLng latLng;

    public Location(LatLng latLng) {
        this.latLng = latLng;
    }
}