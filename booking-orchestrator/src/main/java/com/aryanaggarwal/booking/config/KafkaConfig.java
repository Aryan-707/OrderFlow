package com.aryanaggarwal.booking.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    /**
     * Routes failed messages to a DLQ topic after 3 retry attempts.
     * Prevents silent message loss on processing failures.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        var recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new TopicPartition(
                        record.topic() + "-dlq", // e.g. bookings-results-dlq
                        record.partition()));
        var backoff = new FixedBackOff(1000L, 3L); // 1s interval, 3 retries
        return new DefaultErrorHandler(recoverer, backoff);
    }
}
