/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.group.client;

import java.io.IOException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author jesse
 */
public class FinalProConAi {

    public static String sendPOST(String[] keys) {
//  This file allows for a connection to be made with Ollama as well as outlining paramaters for json objects      
        // Create a new HTTP client instance
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String res = "";
        String url = "http://localhost:11434/api/generate"; //Your API URL
        try {
            // Create the POST request method
            HttpPost postRequest = new HttpPost(url);

            // Create a string entity with data to be sent in the request body
            String params = "{\"model\":\"" + keys[0] + "\","
                    + "\"prompt\":\"" + keys[1] + "\","
                    + "\"stream\":" + Boolean.FALSE + "}";
//            System.out.println("params"+params);
            StringEntity stringEntity = new StringEntity(params);

            // Set the content type of the request to JSON
            postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // Set the entity for the POST request
            postRequest.setEntity(stringEntity);

            // Execute the request and print the response
            HttpResponse response = httpClient.execute(postRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Status Code: " + statusCode);

            // Get the response entity
            res = EntityUtils.toString(response.getEntity());

        } catch (IOException ex) {
            //Logger.getLogger(ApiRequests.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        } catch (ParseException ex) {
            //Logger.getLogger(ApiRequests.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        } finally {
            try {
                // Close the HTTP client instance
                httpClient.close();
            } catch (IOException ex) {
                //Logger.getLogger(ApiRequests.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex);
            }
        }
        return res;
    }

    public static void main(String[] args) {
        String[] key = {"llama3.2", "why is the sky blue"};
        String res = sendPOST(key);
        System.out.println(res);
    }
}
