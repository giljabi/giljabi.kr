package kr.giljabi.api.controller.google;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

@Slf4j
public class GoogleRouteTest {
    private static final String API_KEY = "AIzaSyC6ErfLfV-mKf9EXnR0TfCej_ORM9x_WSY";
    private static final String BASE_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";

    public static void main(String[] args) {
        // <trkpt lat="37.575123" lon="126.983065">
        // <trkpt lat="37.571015" lon="127.009327">
//        String origin = "New York, NY";
//        String destination = "Los Angeles, CA";
            LatLng originLatLng = new LatLng(37.957961, 127.472555);
            LatLng destinationLatLng = new LatLng(37.571015, 127.009327);
//        LatLng originLatLng = new LatLng(40.712776, -74.005974);
//        LatLng destinationLatLng = new LatLng(34.052235, -118.243683);
        Waypoint origin = new Waypoint(new Location(originLatLng));
        Waypoint destination = new Waypoint(new Location(destinationLatLng));
        RouteRequest routeRequest = new RouteRequest(origin, destination, "WALK");

        Gson gson = new Gson();
        String requestBody = gson.toJson(routeRequest);
        System.out.println(requestBody);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("X-Goog-Api-Key", API_KEY);
            request.setHeader("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline");
            request.setEntity(new StringEntity(requestBody));
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

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

        JsonArray routes = jsonObject.getAsJsonArray("routes");
        for (JsonElement routeElement : routes) {
            JsonObject route = routeElement.getAsJsonObject();

            // 거리 추출
            if (route.has("distanceMeters")) {
                int distanceMeters = route.get("distanceMeters").getAsInt();
                System.out.println("Distance (meters): " + distanceMeters);
            }

            // 소요 시간 추출
            if (route.has("duration")) {
                JsonObject duration = route.getAsJsonObject("duration");
                if (duration.has("seconds")) {
                    int durationSeconds = duration.get("seconds").getAsInt();
                    System.out.println("Duration (seconds): " + durationSeconds);
                }
            }

            // 인코딩된 폴리라인 추출
            if (route.has("polyline")) {
                JsonObject polyline = route.getAsJsonObject("polyline");
                if (polyline.has("encodedPolyline")) {
                    String encodedPolyline = polyline.get("encodedPolyline").getAsString();
                    System.out.println("Encoded Polyline: " + encodedPolyline);
                }
            }
        }
    }
}