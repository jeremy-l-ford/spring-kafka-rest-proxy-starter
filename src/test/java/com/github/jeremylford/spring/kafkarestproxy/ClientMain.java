package com.github.jeremylford.spring.kafkarestproxy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

public class ClientMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder().build();

        String content = "";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
//                .POST(HttpRequest.BodyPublishers.ofString(content))
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .uri(URI.create("http://localhost:8080/proxy/v3/clusters/redpanda.0b19101c-2c57-4c92-913d-f64dc802d109/topics"))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
}
