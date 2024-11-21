package kr.giljabi.api.controller;

import io.swagger.annotations.ApiOperation;
import kr.giljabi.api.entity.GiljabiGpsdata;
import kr.giljabi.api.response.Response;
import kr.giljabi.api.response.XmlShareResponse;
import kr.giljabi.api.service.GiljabiGpsDataService;
import kr.giljabi.api.service.MinioService;
import kr.giljabi.api.service.ShareCoursesService;
import kr.giljabi.api.utils.ErrorCode;
import kr.giljabi.api.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @GetMapping("/api/1.0/gpxshare/{fileid}/{version}")
    @ApiOperation(value="경로 공유", notes = "공유경로 정보 api")
    public Response getGpxshare(@PathVariable(required = false) String fileid,
                                @PathVariable(required = false) String version) {
        try {
            Optional<XmlShareResponse> response = null;

            if (version.equals("v1"))
                response = shareService.findByFileHash(fileid);
            else if (version.equals("v2"))
                response = shareService.findByUuidFromGpxdata(fileid);

            return new Response(response);
        } catch (IllegalArgumentException e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), "파일을 찾을 수 없습니다.");
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }
}


