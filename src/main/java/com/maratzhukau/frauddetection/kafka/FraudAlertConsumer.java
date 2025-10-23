package com.maratzhukau.frauddetection.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class FraudAlertConsumer {

    private static final Logger logger = LoggerFactory.getLogger(FraudAlertConsumer.class);

    private final ObjectMapper objectMapper;

    public FraudAlertConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${kafka.topic.fraud-alerts:fraud-alerts}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeFraudAlert(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            FraudAlertEvent fraudAlert = objectMapper.readValue(message, FraudAlertEvent.class);
            logger.warn("FRAUD ALERT RECEIVED [Partition: {}, Offset: {}] with TransactionID: {}",
                    partition, offset, fraudAlert.getTransactionId());
            processAlert(fraudAlert);
        } catch (Exception exception) {
            logger.error("Failed to process fraud alert message: {}", message, exception);
        }
    }

    /**
     * The purpose of this method is to take some sort of action of the fraud alert.
     * In a real business we could send a notice to a security team or a fraud team to take action.
     * For the purpose of this project, we will just log the fraud alert.
     *
     * @param fraudAlert the fraud alert event to process
     *
     **/
    private void processAlert(FraudAlertEvent fraudAlert) {
        logger.info("Processing fraud alert for account: {}", fraudAlert.getAccountId());
    }
}
