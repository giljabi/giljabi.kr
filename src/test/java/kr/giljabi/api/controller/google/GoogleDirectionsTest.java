package kr.giljabi.api.controller.google;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class GoogleDirectionsTest {
    private static final String API_KEY = "AIzaSyC6ErfLfV-mKf9EXnR0TfCej_ORM9x_WSY";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json";

    public static void main(String[] args) {
        String origin = "37.566146,126.966224";
        String destination = "37.551211,126.988321";
        String requestUrl = String.format("%s?origin=%s&destination=%s&key=%s&mode=walking",
                BASE_URL, origin, destination, API_KEY);
        System.out.println(requestUrl);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(requestUrl);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String responseBody = EntityUtils.toString(entity);
                parseResponse(responseBody);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseResponse(String responseBody) {
        JsonElement jsonElement = JsonParser.parseString(responseBody);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String status = jsonObject.get("status").getAsString();
        if ("ZERO_RESULTS".equals(status)) {
            System.out.println("No routes found between the specified locations.");
        } else {
            System.out.println(jsonObject);
        }
    }
}
