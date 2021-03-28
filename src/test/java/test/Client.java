package test;

import tech.itpark.tests.Cat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Client {
    public static int m = 6;

    public static void main(String[] args) {
        final var client = HttpClient.newBuilder()
                .build();
        final var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9999/api/users"))
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"age\":10,\"name\":\"Sara\"}"))
                .build();
//        final var request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:9999/api/get"))
//                .GET()
//                .build();
        try {
            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response == null) {
                return;
            }
            System.out.println(response.body());
            System.out.println(response.headers());
            System.out.println(response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
