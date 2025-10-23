package com.maratzhukau.frauddetection.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class FraudAlertProducer {

    private static final Logger logger = LoggerFactory.getLogger(FraudAlertProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${kafka.topic.fraud-alerts:fraud-alerts}")
    private String topic;

    public FraudAlertProducer(KafkaTemplate<String, String> kafkaTemplate,
                              ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendFraudAlert(FraudAlertEvent fraudAlertEvent) {
        try {
            String eventJson = objectMapper.writeValueAsString(fraudAlertEvent);
            String key = fraudAlertEvent.getAccountId();

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, eventJson);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Fraud alert sent successfully: accountId={}, transactionId={}, partition={}, offset={}",
                            fraudAlertEvent.getAccountId(),
                            fraudAlertEvent.getTransactionId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to send fraud alert: accountId={}, transactionId={}",
                            fraudAlertEvent.getAccountId(),
                            fraudAlertEvent.getTransactionId(),
                            ex);
                }
            });

        } catch (JsonProcessingException e) {
            logger.error("Error serializing fraud alert event", e);
        }
    }
}
