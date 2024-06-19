package kr.giljabi.api.response;

import lombok.Data;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.06.19
 * @Description
 */
@Data
public class Mountain100 {
    private String trackname;
    private String trackkorean;

    public Mountain100(String trackname, String trackkorean) {
        this.trackname = trackname;
        this.trackkorean = trackkorean;
    }
}