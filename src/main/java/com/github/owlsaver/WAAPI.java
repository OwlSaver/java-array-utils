package com.github.owlsaver;

public class WAAPI {

}

package com.github.owlsaver;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherApp {

    // Database connection string
    private static final String DB_URL = "jdbc:sqlite:weather.db";
    private static final String DB_FILE_NAME = "weather.db";

    // Internal variables for settings
    private String email = "someone@company.com";
    private double latitude = 39.8283;
    private double longitude = -98.5795;

    public static void main(String[] args) {
        // Create an instance of the app to run it
        new WeatherApp().run();
    }

    /**
     * Main logic to fetch data from the NWS API and save it to the database.
     */
    public void fetchAndSaveForecasts() {
        HttpClient client = HttpClient.newHttpClient();
        String userAgent = String.format("NWSAPIJavaExample/1.0 (%s)", this.email);
        String pointsUrlString = String.format("https://api.weather.gov/points/%.4f,%.4f", this.latitude,
                this.longitude);

        try {
            System.out.println("Fetching data from API for " + pointsUrlString);
            HttpRequest pointsRequest = HttpRequest.newBuilder().uri(URI.create(pointsUrlString))
                    .header("User-Agent", userAgent).build();
            HttpResponse<String> pointsResponse = client.send(pointsRequest, HttpResponse.BodyHandlers.ofString());

            if (pointsResponse.statusCode() != 200) {
                System.out.println("Error: Received status " + pointsResponse.statusCode() + " from points endpoint.");
                System.out.println("Response Body: " + pointsResponse.body());
                return;
            }

            JSONObject pointsJson = new JSONObject(pointsResponse.body());

            JSONObject properties = pointsJson.getJSONObject("properties");
            String forecastUrlString = properties.getString("forecast");
            String gridId = properties.getString("gridId");
            int gridX = properties.getInt("gridX");
            int gridY = properties.getInt("gridY");

            HttpRequest forecastRequest = HttpRequest.newBuilder().uri(URI.create(forecastUrlString))
                    .header("User-Agent", userAgent).build();
            HttpResponse<String> forecastResponse = client.send(forecastRequest, HttpResponse.BodyHandlers.ofString());
            JSONObject forecastJson = new JSONObject(forecastResponse.body());

            saveForecastData(forecastJson, gridId, gridX, gridY);

        } catch (Exception e) {
            System.out.println("Error during API fetch or data save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses the forecast JSON and inserts each period's data into the database.
     */
    public void saveForecastData(JSONObject forecastJson, String gridId, int gridX, int gridY) {
        String locationId = String.format("%s-%d-%d", gridId, gridX, gridY);
        String sql = "INSERT OR REPLACE INTO forecasts(location_id, period_number, name, temperature, temperatureUnit, windSpeed, windDirection, shortForecast, detailedForecast) VALUES(?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            JSONArray periods = forecastJson.getJSONObject("properties").getJSONArray("periods");
            for (int i = 0; i < periods.length(); i++) {
                JSONObject period = periods.getJSONObject(i);
                pstmt.setString(1, locationId);
                pstmt.setInt(2, period.getInt("number"));
                pstmt.setString(3, period.getString("name"));
                pstmt.setInt(4, period.getInt("temperature"));
                pstmt.setString(5, period.getString("temperatureUnit"));
                pstmt.setString(6, period.getString("windSpeed"));
                pstmt.setString(7, period.getString("windDirection"));
                pstmt.setString(8, period.getString("shortForecast"));
                pstmt.setString(9, period.getString("detailedForecast"));
                pstmt.addBatch();
            }

            int[] batchResult = pstmt.executeBatch();
            System.out.println("Success: Loaded " + batchResult.length + " forecast records into the database.");

        } catch (Exception e) {
            System.out.println("Error saving forecast data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

