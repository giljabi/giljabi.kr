package kr.giljabi.api.controller;

import io.swagger.annotations.ApiOperation;
import kr.giljabi.api.response.Response;
import kr.giljabi.api.response.XmlShareResponse;
import kr.giljabi.api.service.ShareCoursesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Open Route Service를 이용한 경로탐색
 * @author eahn.park@gmail.com
 * 2018.10.26 gpx to tcx Project start....
 * 2021.01.10 openrouteapi license 변경
 * 2021.09.17 Spring boot 2.5.4
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class RouterShareController {

    private final ShareCoursesService shareService;

    @GetMapping("/api/1.0/gpxshare/{fileid}")
    @ApiOperation(value="경로 공유", notes = "공유경로 정보 api")
    public Response getGpxshare(@PathVariable String fileid) {
        Optional<XmlShareResponse> response = shareService.findByFileHash(fileid);
        return new Response(response);
    }

}
