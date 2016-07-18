package com.apigee.diagnosis.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by amar on 17/07/16.
 */
public class RestAPIExecutor {

    //private final static String USER_CREDENTIALS;
    //private final static String ENCODED_USER_AUTHORIZATION;
    //private final static String PROPERTIES_FILE = "resources/config/config.properties";

    private final static String USER_AGENT = "Mozilla/5.0";

    final static String FORWARD_SLASH = "/";

    /*static {
        USER_CREDENTIALS = getUserCredentials();
        ENCODED_USER_AUTHORIZATION = new sun.misc.BASE64Encoder().encode(USER_CREDENTIALS.getBytes());
    }

    private static String getUserCredentials() {
        String userCredentials = null;
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(PROPERTIES_FILE)));
            // get the property values
            userCredentials = prop.getProperty("username") + ":"
                    + prop.getProperty("password");
            ;

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        return userCredentials;
    }*/

    /*
     * Executes the GET Method for the specified API and returns the response
     */
    public static String executeGETAPI(String apiURL, String bearerAccessToken) throws Exception {
        // Create the URL object with the api
        URL urlObj = new URL(apiURL);

        // Create the connection
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

        // Setup the authorization
        String authorization = new sun.misc.BASE64Encoder().encode(bearerAccessToken.getBytes());
        con.setRequestProperty("Authorization", "Bearer " + authorization);

        // Set the REST API Method as GET
        con.setRequestMethod("GET");

        // Add request headers
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Content-Type", "application/json");


        int responseCode = con.getResponseCode();

        // Process the GET API response
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}