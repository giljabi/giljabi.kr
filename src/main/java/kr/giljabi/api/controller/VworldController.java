package kr.giljabi.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author : eahn.park@gmail.com
 * @Date : 2024.05.22
 * @Description
 */
@Controller
public class VworldController {

    @PostMapping("/vworld")
    public String handlePostRequest(@RequestParam("gpxdata")  String gpxdata,
                                    @RequestParam("labelsData")  String labelsData,
                                    @RequestParam("uuid") String uuid,
                                    Model model) {
        //String decompressed = LZString.decompressFromBase64(data);
        System.out.println("data length: " + gpxdata.length());
        System.out.println("labelsData: " + labelsData);
        model.addAttribute("gpxdata", gpxdata);
        model.addAttribute("labelsData", labelsData);
        model.addAttribute("uuid", uuid);
        return "vworld";
    }

    @GetMapping("/googlemap")
    public String googleMap(Model model) {
//        System.out.println("data length: " + gpxdata.length());
//        System.out.println("labelsData: " + labelsData);
//        model.addAttribute("gpxdata", gpxdata);
//        model.addAttribute("labelsData", labelsData);
//        model.addAttribute("uuid", uuid);

        return "googlemap";
    }


}

