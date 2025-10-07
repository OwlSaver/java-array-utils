package com.github.owlsaver;

public class WADB {

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
    /**
     * Handles the 'Build' command. Creates the database and table.
     */
    public void buildDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS forecasts (\n"
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    location_id TEXT NOT NULL,\n"
                + "    period_number INTEGER NOT NULL,\n"
                + "    name TEXT NOT NULL,\n"
                + "    temperature INTEGER NOT NULL,\n"
                + "    temperatureUnit TEXT NOT NULL,\n"
                + "    windSpeed TEXT,\n"
                + "    windDirection TEXT,\n"
                + "    shortForecast TEXT,\n"
                + "    detailedForecast TEXT,\n"
                + "    UNIQUE(location_id, period_number)\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Success: Database and table are ready.");
        } catch (Exception e) {
            System.out.println("Error: Could not build database. " + e.getMessage());
        }
    }

    /**
     * Handles the 'Load' command. Fetches API data and populates the table.
     */
    public void loadData() {
        if (!databaseExists()) {
            System.out.println("Error: Database or table not found. Please run 'Build' first.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM forecasts;");
            System.out.println("Cleared existing data from forecasts table.");
        } catch (Exception e) {
            System.out.println("Error: Could not clear table. " + e.getMessage());
            return;
        }

        fetchAndSaveForecasts();
    }

    /**
     * Handles the 'Status' command. Displays row count or "No Database".
     */
    public void showStatus() {
        if (!databaseExists()) {
            System.out.println("Status: No Database");
            return;
        }

        String sql = "SELECT COUNT(*) AS row_count FROM forecasts;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int count = rs.getInt("row_count");
                System.out.println("Status: " + count + " rows in the forecasts table.");
            }
        } catch (Exception e) {
            System.out.println("Error: Could not retrieve database status. " + e.getMessage());
        }
    }

    /**
     * Handles the 'Dump' command. Displays all data from the forecasts table.
     */
    public void dumpData() {
        if (!databaseExists()) {
            System.out.println("Error: Database or table not found. Please run 'Build' and 'Load' first.");
            return;
        }

        String sql = "SELECT period_number, name, temperature, temperatureUnit, windSpeed, shortForecast FROM forecasts ORDER BY period_number;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--------------------------------- FORECAST DATA ---------------------------------");
            System.out.printf("%-8s %-22s %-10s %-15s %s%n", "Period", "Name", "Temp", "Wind", "Forecast");
            System.out.println("-----------------------------------------------------------------------------------");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                String temp = rs.getInt("temperature") + rs.getString("temperatureUnit");
                System.out.printf("%-8d %-22s %-10s %-15s %s%n",
                        rs.getInt("period_number"),
                        rs.getString("name"),
                        temp,
                        rs.getString("windSpeed"),
                        rs.getString("shortForecast"));
            }

            if (!hasRows) {
                System.out.println("No data found in the table. Use the 'Load' command to populate it.");
            }
            System.out.println("-----------------------------------------------------------------------------------");

        } catch (Exception e) {
            System.out.println("Error: Could not retrieve data from table. " + e.getMessage());
        }
    }

    /**
     * Checks if the database file exists.
     */
    private boolean databaseExists() {
        File dbFile = new File(DB_FILE_NAME);
        return dbFile.exists();
    }

    }
}

