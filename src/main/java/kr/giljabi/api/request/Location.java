package kr.giljabi.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Location {
    private double lat;
    private double lng;

    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
    public Double[] toDoubleArray() {
        return new Double[] {this.lng, this.lat};
    }

    @Override
    public String toString() {
        return "Location{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}

