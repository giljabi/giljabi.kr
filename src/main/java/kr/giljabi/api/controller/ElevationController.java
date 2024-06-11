package kr.giljabi.api.controller;

import com.github.diogoduailibe.lzstring4j.LZString;
import io.swagger.annotations.ApiOperation;
import kr.giljabi.api.geo.*;
import kr.giljabi.api.request.RequestElevationSaveData;
import kr.giljabi.api.response.Gpx100Response;
import kr.giljabi.api.service.GoogleService;
import kr.giljabi.api.request.RequestElevationData;
import kr.giljabi.api.response.Response;
import kr.giljabi.api.service.MinioService;
import kr.giljabi.api.utils.CommonUtils;
import kr.giljabi.api.utils.ErrorCode;
import kr.giljabi.api.utils.MyHttpUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gpx track 정보를 tcx 변환
 *
 * @author eahn.park@gmail.com
 * 2018.10.26 gpx to tcx Project start....
 * 2021.09.17 Spring boot 2.5.4
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class ElevationController {

    private final GoogleService googleService;
    private final ResourceLoader resourceLoader;

    private final MinioService minioService;

    @Value("${giljabi.mountain100.path}")
    private String mountain100Path;

    @Value("${minio.bucketNameElevation}")
    private String bucketNameElevation;

    @Value("${minio.bucketNameData}")
    private String bucketNameData;

    @Value("${minio.url}")
    private String s3url;

    @PostMapping("/api/1.0/elevation")
    @ApiOperation(value = "고도정보", notes = "google elevation api 이용하여 고도정보를 받아오는 api")
    public Response getElevation(final @Valid @RequestBody RequestElevationData request) {
        ArrayList<Geometry3DPoint> list;
        Response response;
        try {
            if (request.getTrackPoint().size() == 0)
                new Exception("입력된 트랙정보가 없습니다.");

            //tcp socket exception 방지, 개발초기에 간혹 발생했었는데 이제는 이런 문제는 없는듯...
            //googleService.checkGoogle();

            list = googleService.getElevation(request);
            return new Response(list);
//            return getMountainData();
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    /**
     * /api/1.0/mountain100 for mountain100Select combobox
     * @return
     */
    @GetMapping("/api/1.0/mountain100")
    @ApiOperation(value = "산림청 100대 명산",
            notes = "<h3>한국등산트레킹지원센터_산림청 100대명산</h3><br>" +
                    "산림청 100대명산의 POI(관심지점), 갈림길(방면), 노면정보를 제공하는 GPX 포맷의 공간정보 파일데이터<br>" +
                    "https://www.data.go.kr/data/15098177/fileData.do?recommendDataYn=Y<br>")
    public Response getMountainList100() {
        try {
            String filename = String.format("%s/100.txt", mountain100Path);
            //bucketNameData
            List<String> comboData = minioService.readFileContentByList(bucketNameData, filename);

            List<Mountain100> fileList = new ArrayList<>();
            for (String line : comboData) {
                if(line.charAt(0) == '#')
                    continue;
                fileList.add(new Mountain100(line));
            }
            return new Response(fileList);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    /**
     *
    public Response getMountainList100() {
        try {
            ClassPathResource resource = new ClassPathResource("etc/100.txt");
            //jar 파일 내부에서 파일을 읽기 위해서는 InputStream을 사용해야 한다.
            InputStream inputStream = resource.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            List<Mountain100> fileList = new ArrayList<>();
            for (String line : lines) {
                if(line.charAt(0) == '#')
                    continue;
                fileList.add(new Mountain100(line));
            }
            return new Response(fileList);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }
     */

    @Data
    private class Mountain100 {
        public String name;
        public String filename;

        public Mountain100(String lineReader) {
            String[] mountain = lineReader.split(",");
            this.name = mountain[0];
            this.filename = mountain[1];
        }
    }

    /**
     * editor getMountainGpxLists
     * minio 서비스로 변경
     * @param filename + "-*\\.gpx$": abc-*.gpx
     * @return
     */
    @GetMapping("/api/1.0/mountainGpxLists/{filename}")
    @ApiOperation(value = "산림청 100대 명산 이름으로 검색한 gpx 파일 목록은 2개 이상일 수 있어 " +
            "목록을 반환하고 클라이언트에서 파일을 순차적으로 요청한다.")
    public Response getMountainList100Files(@PathVariable String filename) {
        List<String> fileList = new ArrayList<>();
        try {
            //abc-*.gpx, gariwangsan*\\.gpx$
            fileList = minioService.listFiles(bucketNameData,
                    mountain100Path.substring(1) + "/",
                    filename + "*", "gpx");
            return new Response(fileList);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    /**
     *
     * @param filename
     * @return
    public Response getMountainList100Files(@PathVariable String filename) {
        List<String> fileList = new ArrayList<>();
        try {
            File fileDir = new File(mountain100Path);
            String regex = filename + ".*\\.gpx$";

            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().matches(regex);
                }
            };
            File[] files = fileDir.listFiles(filter);

            for (File file : files) {
                fileList.add(file.getName());
            }
            return new Response(fileList);
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }
    */

    public String readFileAsString(String filename) throws IOException {
        String fileFullName = String.format("%s/%s", mountain100Path, filename);
        File file = new File(fileFullName);
        byte[] binaryData = FileCopyUtils.copyToByteArray(file);
        return new String(binaryData, StandardCharsets.UTF_8);
    }

    @GetMapping("/api/1.0/mountainGpx/{directory}/{filename}")
    @ApiOperation(value = "산림청 100대 명산 gpx 경로정보, gpx 파일로 관리하고 압축해서 전송한다 ")
    public Response getMountainList100Gpxfile(@PathVariable String directory,
                                              @PathVariable String filename) {
        try {
            Gpx100Response gpx100Response = new Gpx100Response();
            String gpx = minioService.readFileContentByString(bucketNameData,
                    directory + "/" + filename);
            String xmlData = LZString.compressToUTF16(gpx);
            gpx100Response.setTrackName(filename);
            gpx100Response.setXmlData(xmlData);
            return new Response(Optional.of(gpx100Response));
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    /**
     *
     * @param request
     * @return
     *     public Response getMountainList100Gpxfile(@PathVariable String filename) {
     *         try {
     *             Gpx100Response gpx100Response = new Gpx100Response();
     *
     *             //compressToUTF16가 더 좋은 압축률을 보여주지만(compressToBase64), 송수신시 호환성문제가 있을 수 있음
     *             String xmlData = LZString.compressToUTF16(readFileAsString(filename));
     *             gpx100Response.setTrackName(filename);
     *             gpx100Response.setXmlData(xmlData);
     *             return new Response(Optional.of(gpx100Response));
     *         } catch (Exception e) {
     *             return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
     *         }
     *     }
     */
    @PostMapping("/api/1.0/saveElevation")
    @ApiOperation(value = "GPX정보", notes = "GPX정보를 저장한다.")
    public Response saveElevation(final @Valid @RequestBody RequestElevationSaveData request) {
        try {
            String uuid = CommonUtils.generateUUID().toString();
            String filename = String.format("%s.%s", CommonUtils.getFileLocation(uuid), request.getFileExt());
            String savedFilename = minioService.saveFile(bucketNameElevation, filename, request.getXmlData());

            //DB에 저장 필요

            return new Response(ErrorCode.STATUS_SUCCESS.getStatus(), ErrorCode.STATUS_SUCCESS.getMessage());
        } catch (Exception e) {
            return new Response(ErrorCode.STATUS_EXCEPTION.getStatus(), e.getMessage());
        }
    }

    private Response getMountainData() {
        Response response = new Response(0, "정상 처리 되었습니다.");
        ArrayList<Geometry3DPoint> data = new ArrayList<>();
        data.add(new Geometry3DPoint(127.013472, 37.953889, 153.4));
        data.add(new Geometry3DPoint(127.045492, 37.921015, 107.4));
        data.add(new Geometry3DPoint(127.114628, 37.912896, 325.3));
        data.add(new Geometry3DPoint(127.162436, 37.974824, 205.6));
        data.add(new Geometry3DPoint(127.139509, 37.984367, 265.8));
        response.setData(data);
        return response;
    }
}
