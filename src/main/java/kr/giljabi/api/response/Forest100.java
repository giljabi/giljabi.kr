package kr.giljabi.api.response;

import lombok.Data;

/**
 * @Author : eahn.park@gmail.com
 * @Date : 2024.06.19
 * @Description
 */
@Data
public class Forest100 {
    private String trackname;
    private String trackkorean;

    public Forest100(String trackname, String trackkorean) {
        this.trackname = trackname;
        this.trackkorean = trackkorean;
    }
}
