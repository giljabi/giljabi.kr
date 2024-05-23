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
    public String handlePostRequest(@RequestParam("gpxdata")  String data, Model model) {
        //String decompressed = LZString.decompressFromBase64(data);
        System.out.println("data length: " + data.length());
        model.addAttribute("gpxdata", data);
        return "vworld";
    }

    @Data
    @Getter
    public class GpxData {
        public String gpxdata;
    }
}
