package kr.giljabi.api.utils;

import kr.giljabi.api.exception.GiljabiException;
import kr.giljabi.api.request.RequestElevationSaveData;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.util.Base64Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Http Utils
 * @date 2022.03.26
 */
@Slf4j
public class MyHttpUtils {
    public static String httpGetMethod(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url); //POST call
        httpGet.setHeader("Accept", "application/json; charset=utf-8");
        httpGet.setHeader("Content-Type", "application/json; charset=utf-8");

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = httpClient.execute(httpGet);

        String result = "";
        try {
            if (response.getStatusLine().getStatusCode() != 200)
                throw new GiljabiException(response.getStatusLine().getStatusCode(),
                        " 응답 오류입니다.");

            ResponseHandler<String> handler = new BasicResponseHandler();
            result = handler.handleResponse(response);
            //System.out.println("result = " + result);;
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(response != null) response.close();
            if(httpClient != null) httpClient.close();
        }
        return result;
    }

    /**
     *
     * @param url
     * @param urlParameters
     * @return
     * @throws IOException
     */
    public static String httpPostMethod(String url, List<NameValuePair> urlParameters) throws IOException {
        HttpPost httpPost = new HttpPost(url); //POST call
        httpPost.setHeader("Accept", "application/json; charset=utf-8");
        httpPost.setHeader("Content-Type", "application/json; charset=utf-8");

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = httpClient.execute(httpPost);

        String result = "";
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
            if (response.getStatusLine().getStatusCode() != 200)
                throw new GiljabiException(response.getStatusLine().getStatusCode(),
                        " 응답 오류입니다.");

            ResponseHandler<String> handler = new BasicResponseHandler();
            result = handler.handleResponse(response);
            //System.out.println("result = " + result);;
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(response != null) response.close();
            if(httpClient != null) httpClient.close();
        }
        return result;
    }

    /**
     * Open route service post call method
     * @param url
     * @param json
     * @param apikey
     * @return
     * @throws IOException
     * @throws GiljabiException
     */
    public static String httpPostMethod(String url,
                                        JSONObject json,
                                        String apikey) throws IOException, GiljabiException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", apikey);
        httpPost.setHeader("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");
        httpPost.setHeader("Content-Type", "application/json; charset=utf-8");

        StringEntity postEntity = new StringEntity(json.toString());
        httpPost.setEntity(postEntity);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String result;
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            if (response.getStatusLine().getStatusCode() != 200)
                throw new GiljabiException(response.getStatusLine().getStatusCode(),
                        "Openrouteservice 응답 오류입니다.");

            ResponseHandler<String> handler = new BasicResponseHandler();
            result = handler.handleResponse(response);
        } finally {
            if (response != null) response.close();
            if (httpClient != null) httpClient.close();
        }
        //log.info(result);
        return result;
    }

    //LZ string compress
    public static byte[] byteCompress(byte[] data) throws IOException {
        log.info("xml file size:          " + String.format("%, 9dbyte", data.length));
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
        byte[] result = null;
        try {
            Deflater deflater = new Deflater();
            deflater.setLevel(Deflater.BEST_COMPRESSION);
            deflater.setInput(data);
            deflater.finish();

            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                baos.write(buffer, 0, count);
            }
            result = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            baos.close();
        }
        log.info("xml compress file size: " + String.format("%, 9dbyte", result.length));

        return result;
    }


    public static String byteDecompress(byte[] compressedData) throws IOException {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (Exception e) {
            throw new IOException("Failed to decompress data", e);
        } finally {
            inflater.end();
        }
        return outputStream.toString("UTF-8");
    }

    public static void saveXmlFile(RequestElevationSaveData request, String sharePath) throws IOException {
        String xmlData = request.getXmlData();
        byte[] xmlFile = Base64Utils.decodeFromString(xmlData);
        String xmlString = MyHttpUtils.byteDecompress(xmlFile);

        String filename = "";
        String currentMonth = formatDate("yyyyMM");
        String saveFilename = generateUUID();
        filename = String.format("%s/%s/%s.%s", sharePath, currentMonth, saveFilename, request.getFileExt());

        //writeToFile(filename, xmlString);
        writeToFile(filename, xmlData); //압축된 상태로 저장
    }

    public static String formatDate(String pattern) {
        LocalDate date = LocalDate.now();
        if( pattern.isEmpty()) {
            throw new IllegalArgumentException("Date and pattern must not be null or empty");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * filename: 경로를 포함하는 파일명
     * @param filename
     * @param data
     * @throws IOException
     */
    public static void writeToFile(String filename, String data) throws IOException {
        File file = new File(filename);
        File parentDirectory = file.getParentFile();
        if (parentDirectory != null && !parentDirectory.exists()) {
            parentDirectory.mkdirs();
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
            writer.write(data);
        }
    }

    /**
     * Generate UUID, 파일명에 사용
     * @return
     */
    public static String generateUUID() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
