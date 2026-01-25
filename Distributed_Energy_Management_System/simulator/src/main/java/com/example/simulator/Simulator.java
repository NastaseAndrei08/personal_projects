package com.example.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class Simulator {

    private final static String QUEUE_NAME = "sensor_data_queue";

    public static void main(String[] args) {
        System.out.println("Starting Device Simulator (Dynamic Mode)...");
        try {
            // 1. Load Config (Fallback)
            Properties props = new Properties();
            try (InputStream input = Simulator.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input != null) {
                    props.load(input);
                }
            }

            // 2. Determine Device ID
            // PRIORITY: Environment Variable (Docker) > Config File > Random
            String deviceId = System.getenv("DEVICE_ID");

            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = props.getProperty("device_id"); // Check config.properties
            }

            if (deviceId == null || deviceId.isEmpty()) {
                System.out.println("No Device ID found in Env or Config. Generating Random UUID.");
                deviceId = UUID.randomUUID().toString();
            }

            System.out.println(">>> Simulating Data for Device ID: " + deviceId);

            // 3. Connect RabbitMQ
            ConnectionFactory factory = new ConnectionFactory();
            String host = System.getenv("RABBIT_HOST");
            factory.setHost(host != null ? host : "localhost");
            factory.setPort(5672);

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                // Ensure the queue exists
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);

                ObjectMapper mapper = new ObjectMapper();
                Random random = new Random();

                // Start from a random base load (5.0 - 15.0 kWh)
                double currentLoad = 5.0 + (random.nextDouble() * 10);

                while (true) {
                    long timestamp = System.currentTimeMillis();

                    // Create Payload
                    Map<String, Object> message = new HashMap<>();
                    message.put("timestamp", timestamp);
                    message.put("device_id", deviceId);
                    message.put("measurement_value", currentLoad);

                    String json = mapper.writeValueAsString(message);

                    // Publish to the default exchange, routing key = queue name
                    channel.basicPublish("", QUEUE_NAME, null, json.getBytes());
                    System.out.println(" [x] Device " + deviceId + " sent: " + String.format("%.2f kWh", currentLoad));

                    // Adjust Load (Random Walk)
                    double change = (random.nextDouble() * 2) - 1; // -1.0 to +1.0
                    currentLoad += change;

                    // Bounds
                    if (currentLoad < 0.1) currentLoad = 0.1;
                    if (currentLoad > 20.0) currentLoad = 20.0;

                    // Sleep 3 seconds
                    Thread.sleep(3000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}