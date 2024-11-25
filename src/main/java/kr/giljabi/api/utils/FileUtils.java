package kr.giljabi.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
/*
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
*/
    public static String saveFile(String physicalFilePath, String fileName, Object fileContent) {
        try {
            // 홈 디렉토리 경로 처리
            if (physicalFilePath.startsWith("~")) {
                physicalFilePath = System.getProperty("user.home") + physicalFilePath.substring(1);
            }

            // 업로드 경로 생성
            Path uploadPath = Paths.get(physicalFilePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Directory created: " + uploadPath);
            }

            // 파일 저장 경로 설정
            Path path = uploadPath.resolve(fileName);

            // 파일 저장 처리
            if (fileContent instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) fileContent;
                if (file != null && !file.isEmpty()) {
                    String originalFileName = file.getOriginalFilename();

                    if (originalFileName != null && (originalFileName.endsWith(".png") || originalFileName.endsWith(".jpg") || originalFileName.endsWith(".jpeg"))) {
                        file.transferTo(path.toFile());
                        log.info("File saved: " + path);
                        return path.toString();
                    }
                }
            } else if (fileContent instanceof String) {
                String content = (String) fileContent;
                if (content != null && !content.isEmpty()) {
                    Files.write(path, content.getBytes());
                    log.info("File saved: " + path);
                    return path.toString();
                }
            }
        } catch (IOException e) {
            log.info("file save error: IOException {}", e.getMessage());
        } catch (Exception e) {
            log.info("file save error: Exception {}", e.getMessage());
        }
        return null;
    }

    public static String fileReaderByText(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("파일이 존재하지 않습니다: " + filePath);
        }

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        return bufferedReader.readLine();
    }

    // 파일을 삭제하는 메소드
    public static boolean deleteFile(String filePath) {
        try {
            log.info("File deleted: " + filePath);
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.info("file delete error: IOException {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.info("file delete error: Exception {}", e.getMessage());
            return false;
        }
    }
}



