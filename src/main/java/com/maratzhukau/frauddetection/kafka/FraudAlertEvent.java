package com.maratzhukau.frauddetection.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FraudAlertEvent {
    private Long transactionId;
    private String accountId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String location;
    private double fraudScore;
    private String alertMessage;

    public FraudAlertEvent() {
    }

    public FraudAlertEvent(Long transactionId, String accountId, BigDecimal amount,
                           LocalDateTime timestamp, String location, double fraudScore) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.location = location;
        this.fraudScore = fraudScore;
        this.alertMessage = generateAlertMessage();
    }

    private String generateAlertMessage() {
        return String.format("FRAUD ALERT: Suspicious transaction detected for account %s. " +
                        "Amount: $%s, Location: %s, Fraud Score: %.2f",
                accountId, amount, location != null ? location : "Unknown", fraudScore);
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getFraudScore() {
        return fraudScore;
    }

    public void setFraudScore(double fraudScore) {
        this.fraudScore = fraudScore;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    @Override
    public String toString() {
        return "FraudAlertEvent{" +
                "transactionId=" + transactionId +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", location='" + location + '\'' +
                ", fraudScore=" + fraudScore +
                ", alertMessage='" + alertMessage + '\'' +
                '}';
    }
}
