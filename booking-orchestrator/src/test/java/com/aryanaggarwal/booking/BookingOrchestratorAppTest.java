package com.aryanaggarwal.booking;

import org.springframework.boot.SpringApplication;

public class BookingOrchestratorAppTest {
    public static void main(String[] args) {
        SpringApplication.from(BookingOrchestratorApp::main)
                .with(KafkaContainerDevMode.class)
                .run(args);
    }
}
