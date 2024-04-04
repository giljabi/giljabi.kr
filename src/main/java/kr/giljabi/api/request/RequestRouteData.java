package kr.giljabi.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestRouteData {
    private Location start;
    private Location end;
    private String direction;
    private String remoteAddr;

    public RequestRouteData(Location start, Location end,
                            String direction, String remoteAddr) {
        this.start = start;
        this.end = end;
        this.direction = direction;
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String toString() {
        return "RequestRouteData{" +
                "start=" + start +
                ", end=" + end +
                ", direction='" + direction + '\'' +
                ", remoteAddr='" + remoteAddr + '\'' +
                '}';
    }
}

