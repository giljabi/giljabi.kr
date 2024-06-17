package kr.giljabi.api.controller.google;

import com.google.gson.annotations.SerializedName;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.06.17
 * @Description
 */
public class LatLng {
    @SerializedName("latitude")
    double latitude;
    @SerializedName("longitude")
    double longitude;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}