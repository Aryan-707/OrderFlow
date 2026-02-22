package com.aryanaggarwal.payment;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import com.aryanaggarwal.base.domain.BookingEvent;
import com.aryanaggarwal.base.domain.BookingStatus;
import com.aryanaggarwal.payment.domain.Customer;
import com.aryanaggarwal.payment.repository.CustomerRepository;
import com.aryanaggarwal.payment.service.SagaCoordinator;

@SpringBootApplication
public class PaymentApp {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentApp.class);

    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class, args);
    }

    @Autowired
    SagaCoordinator sagaCoordinator;

    /** Handles new booking events — checks customer funds and reserves if possible */
    @KafkaListener(id = "payment-new", topics = "bookings-new", groupId = "payment")
    public void onNewBooking(BookingEvent event) {
        LOG.info("Received new booking: {}", event);
        sagaCoordinator.reserve(event);
    }

    /** Handles final saga results — commits or rolls back the reservation */
    @KafkaListener(id = "payment-results", topics = "bookings-results", groupId = "payment")
    public void onBookingResult(BookingEvent event) {
        LOG.info("Received booking result: {}", event);
        if (event.getStatus() == BookingStatus.CONFIRMED ||
                event.getStatus() == BookingStatus.ROLLBACK) {
            sagaCoordinator.confirm(event);
        }
    }

    @Autowired
    private CustomerRepository repository;

    /** Seeds realistic customer data on startup */
    @PostConstruct
    public void seedCustomers() {
        String[] names = {
            "Rahul Sharma", "Priya Patel", "Amit Kumar", "Sneha Reddy", "Vikram Singh",
            "Ananya Gupta", "Rohit Verma", "Divya Nair", "Karan Mehta", "Neha Joshi",
            "Aditya Rao", "Pooja Iyer", "Siddharth Das", "Meera Kapoor", "Arjun Malhotra",
            "Kavita Sinha", "Deepak Pandey", "Ritika Bhat", "Nikhil Saxena", "Swati Dubey",
            "Manish Agarwal", "Pallavi Menon", "Gaurav Choudhury", "Tanvi Thakur", "Raj Banerjee",
            "Anjali Tiwari", "Varun Hegde", "Simran Kaur", "Akash Jain", "Bhavna Kulkarni",
            "Vivek Mohan", "Shruti Pillai", "Abhishek Rana", "Nandini Shetty", "Harsh Goel",
            "Ishita Mishra", "Pankaj Deshpande", "Ruchi Chopra", "Sameer Bose", "Tanya Varma",
            "Aman Chauhan", "Komal Bhatt", "Sandeep Naidu", "Megha Patil", "Tarun Rastogi",
            "Aditi Sengupta", "Kunal Datta", "Preeti Saini", "Suresh Nambiar", "Geeta Rajan"
        };

        for (int i = 0; i < names.length; i++) {
            int balance = 200 + (i * 17) % 800; // deterministic but varied balances
            Customer c = new Customer(null, names[i], balance, 0);
            repository.save(c);
        }
        LOG.info("Seeded {} customers", names.length);
    }
}
