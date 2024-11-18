package kr.giljabi.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileUtils {
    public static String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        return ""; // 확장자가 없을 때 빈 문자열 반환
    }

    public static String saveFile(String physicalFilePath, String fileName, MultipartFile file) {
        try {
            if (physicalFilePath.startsWith("~")) {
                physicalFilePath = System.getProperty("user.home") + physicalFilePath.substring(1);
            }

            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                if (originalFileName != null && (originalFileName.endsWith(".png") || originalFileName.endsWith(".jpg") || originalFileName.endsWith(".jpeg"))) {
                    Path uploadPath = Paths.get(physicalFilePath);

                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                        log.info("Directory created: " + uploadPath.toString());
                    }

                    Path path = uploadPath.resolve(fileName);
                    file.transferTo(path.toFile());
                    log.info("File saved: " + path.toString());
                    return path.toString();
                }
            }
        } catch (IOException e) {
            log.info("file save error: IOException {}", e.getMessage());
            //e.printStackTrace();
        } catch (Exception e) {
            log.info("file save error: Exception {}", e.getMessage());
            //e.printStackTrace();
        }
        return null;
    }

    public static String saveFile(String physicalFilePath, String fileName, String fileContent) {
        try {
            // 홈 디렉토리 경로 처리
            if (physicalFilePath.startsWith("~")) {
                physicalFilePath = System.getProperty("user.home") + physicalFilePath.substring(1);
            }

            // 파일 내용이 비어 있지 않은 경우 처리
            if (fileContent != null && !fileContent.isEmpty()) {
                Path uploadPath = Paths.get(physicalFilePath);

                // 디렉토리가 존재하지 않으면 생성
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                    log.info("Directory created: " + uploadPath.toString());
                }

                // 파일 저장 경로 생성
                Path path = uploadPath.resolve(fileName);

                // 파일 내용 저장
                Files.write(path, fileContent.getBytes());
                log.info("File saved: " + path.toString());
                return path.toString();
            }
        } catch (IOException e) {
            log.info("file save error: IOException {}", e.getMessage());
        } catch (Exception e) {
            log.info("file save error: Exception {}", e.getMessage());
        }
        return null;
    }

}

