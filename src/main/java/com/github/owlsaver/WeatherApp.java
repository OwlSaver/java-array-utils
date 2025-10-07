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
     * Main application loop to handle user input.
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.printf("\nCurrent Settings: Lat=%.4f, Long=%.4f, Email=%s%n", latitude, longitude, email);
            System.out.print("Enter command (Build, Load, Status, Dump, Set, Exit, Help): ");
            String input = scanner.nextLine().trim().toLowerCase();
            String[] parts = input.split("\\s+", 2);
            String command = parts[0];

            switch (command) {
                case "build":
                    buildDatabase();
                    break;
                case "load":
                    loadData();
                    break;
                case "status":
                    showStatus();
                    break;
                case "dump":
                    dumpData();
                    break;
                case "set":
                    handleSetCommand(parts);
                    break;
                case "exit":
                    System.out.println("Exiting program.");
                    scanner.close();
                    return;
                case "help":
                default:
                    displayHelp();
                    break;
            }
        }
    }

    /**
     * Handles the 'Set' command to update internal variables.
     */
    private void handleSetCommand(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Error: 'set' command requires a parameter (e.g., 'set email user@example.com').");
            return;
        }
        String[] args = parts[1].split("\\s+", 2);
        if (args.length < 2) {
            System.out.println("Error: 'set " + args[0] + "' requires a value.");
            return;
        }

        String setting = args[0];
        String value = args[1];

        switch (setting) {
            case "email":
                this.email = value;
                System.out.println("Success: Email updated to " + this.email);
                break;
            case "lat":
                try {
                    this.latitude = Double.parseDouble(value);
                    System.out.printf("Success: Latitude updated to %.4f%n", this.latitude);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid latitude value. Please enter a number.");
                }
                break;
            case "long":
                try {
                    this.longitude = Double.parseDouble(value);
                    System.out.printf("Success: Longitude updated to %.4f%n", this.longitude);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid longitude value. Please enter a number.");
                }
                break;
            default:
                System.out.println("Error: Unknown setting '" + setting + "'. Use 'email', 'lat', or 'long'.");
                break;
        }
    }

    /**
     * Displays the help message with all available commands.
     */
    public void displayHelp() {
        System.out.println("\nAvailable Commands:");
        System.out.println("  Build          - Creates the database and the required table.");
        System.out.println(
                "  Load           - Clears existing data and loads new data from the API using current settings.");
        System.out.println("  Status         - Checks the database status and reports the number of rows.");
        System.out.println("  Dump           - Displays all rows from the forecasts table.");
        System.out
                .println("  Set email ...  - Sets the email for the API user agent (e.g., set email new@example.com).");
        System.out.println("  Set lat ...    - Sets the latitude for the API call (e.g., set lat 40.7128).");
        System.out.println("  Set long ...   - Sets the longitude for the API call (e.g., set long -74.0060).");
        System.out.println("  Exit           - Closes the application.");
        System.out.println("  Help           - Displays this help message.");
    }
}
