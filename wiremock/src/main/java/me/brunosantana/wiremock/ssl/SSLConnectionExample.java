package me.brunosantana.wiremock.ssl;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;

public class SSLConnectionExample {

    public static void main(String[] args) {
        // Replace this URL with the one you want to connect to
        String urlString = "https://jsonplaceholder.typicode.com/albums/1/photos";

        try {
            // Create a URL object
            URL url = new URL(urlString);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpsURLConnection) url.openConnection();

            // If you're connecting to an HTTPS URL, cast to HttpsURLConnection
            // HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // Set up SSL properties if needed (e.g., truststore, keystore)

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
            } else {
                System.out.println("Error: " + responseCode);
            }

            // Disconnect the connection
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

