package com.aryanaggarwal.inventory;

import org.springframework.boot.SpringApplication;

public class SeatInventoryAppTest {
    public static void main(String[] args) {
        SpringApplication.from(SeatInventoryApp::main)
                .with(KafkaContainerDevMode.class)
                .run(args);
    }
}
