package kr.giljabi.api.controller;

import com.github.diogoduailibe.lzstring4j.LZString;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.05.22
 * @Description
 */
@Controller
public class VworldController {

    @PostMapping("/vworld")
    public String handlePostRequest(@RequestParam("gpxdata")  String gpxdata,
                                    @RequestParam("labelsData")  String labelsData,
                                    Model model) {
        //String decompressed = LZString.decompressFromBase64(data);
        System.out.println("data length: " + gpxdata.length());
        System.out.println("labelsData: " + labelsData);
        model.addAttribute("gpxdata", gpxdata);
        model.addAttribute("labelsData", labelsData);
        return "vworld";
    }

    @Data
    @Getter
    public class GpxData {
        public String gpxdata;
    }
}
