#!/bin/bash

# Transaction Data Population Script
# This script creates a variety of transactions to test fraud detection

BASE_URL="http://localhost:8080/fraud-detection/v1/transactions"
CONTENT_TYPE="Content-Type: application/json"

echo "Starting transaction data population..."

# Function to make a POST request
make_transaction() {
    local account_id=$1
    local amount=$2
    local timestamp=$3
    local location=$4

    echo "Creating transaction: Account=$account_id, Amount=$amount, Time=$timestamp, Location=$location"

    curl -s -X POST "$BASE_URL" \
        -H "$CONTENT_TYPE" \
        -d "{
            \"accountId\": \"$account_id\",
            \"amount\": $amount,
            \"timestamp\": \"$timestamp\",
            \"location\": \"$location\"
        }"

    echo "" # New line for readability
    sleep 0.5 # Small delay to avoid overwhelming the server
}

# Function to make a transaction without location
make_transaction_no_location() {
    local account_id=$1
    local amount=$2
    local timestamp=$3

    echo "Creating transaction (no location): Account=$account_id, Amount=$amount, Time=$timestamp"

    curl -s -X POST "$BASE_URL" \
        -H "$CONTENT_TYPE" \
        -d "{
            \"accountId\": \"$account_id\",
            \"amount\": $amount,
            \"timestamp\": \"$timestamp\"
        }"

    echo "" # New line for readability
    sleep 0.5
}

echo "========================================="
echo "CREATING NORMAL (NON-FRAUDULENT) TRANSACTIONS"
echo "========================================="

# Normal everyday transactions
make_transaction "user-001" 45.99 "2025-08-24T08:30:00" "New York, NY"
make_transaction "user-001" 12.50 "2025-08-24T12:15:00" "New York, NY"
make_transaction "user-001" 89.75 "2025-08-24T18:45:00" "New York, NY"

make_transaction "user-002" 234.50 "2025-08-24T09:15:00" "Los Angeles, CA"
make_transaction "user-002" 67.25 "2025-08-24T14:30:00" "Los Angeles, CA"
make_transaction "user-002" 156.80 "2025-08-24T19:20:00" "Los Angeles, CA"

make_transaction "user-003" 78.90 "2025-08-24T10:45:00" "Chicago, IL"
make_transaction "user-003" 345.60 "2025-08-24T16:10:00" "Chicago, IL"

make_transaction "user-004" 23.45 "2025-08-24T11:20:00" "Miami, FL"
make_transaction "user-004" 567.89 "2025-08-24T15:40:00" "Miami, FL"

# Some higher amounts but still normal business hours
make_transaction "user-005" 2500.00 "2025-08-24T14:00:00" "Seattle, WA"
make_transaction "user-005" 4750.00 "2025-08-24T16:30:00" "Seattle, WA"

make_transaction "user-006" 1200.00 "2025-08-24T13:15:00" "Boston, MA"
make_transaction "user-006" 890.50 "2025-08-24T17:45:00" "Boston, MA"

echo "========================================="
echo "CREATING HIGH AMOUNT TRANSACTIONS"
echo "========================================="

# High amounts (≥$10,000 but <$50,000) - Score: 0.3
make_transaction "high-amount-001" 12000.00 "2025-08-24T14:30:00" "Dallas, TX"
make_transaction "high-amount-002" 15750.50 "2025-08-24T11:45:00" "Denver, CO"
make_transaction "high-amount-003" 25000.00 "2025-08-24T16:20:00" "Phoenix, AZ"
make_transaction "high-amount-004" 35000.00 "2025-08-24T10:15:00" "Atlanta, GA"

# Very high amounts (≥$50,000) - Score: 0.5
make_transaction "very-high-001" 55000.00 "2025-08-24T13:30:00" "Las Vegas, NV"
make_transaction "very-high-002" 75000.00 "2025-08-24T15:45:00" "San Francisco, CA"
make_transaction "very-high-003" 100000.00 "2025-08-24T12:00:00" "Houston, TX"

echo "========================================="
echo "CREATING OFF-HOURS TRANSACTIONS"
echo "========================================="

# Off-hours transactions (2-6 AM) - Score: 0.2
make_transaction "night-owl-001" 150.00 "2025-08-25T02:30:00" "Portland, OR"
make_transaction "night-owl-002" 89.99 "2025-08-25T03:45:00" "Nashville, TN"
make_transaction "night-owl-003" 245.75 "2025-08-25T04:15:00" "Austin, TX"
make_transaction "night-owl-004" 67.50 "2025-08-25T05:30:00" "Charlotte, NC"
make_transaction "night-owl-005" 178.25 "2025-08-25T06:00:00" "Orlando, FL"

echo "========================================="
echo "CREATING FRAUDULENT SCENARIOS"
echo "========================================="

# Very High amount + off-hours = 0.5 + 0.2 = 0.7 (FRAUDULENT)
make_transaction "fraud-combo-001" 60000.00 "2025-08-25T03:20:00" "Unknown Location"
make_transaction "fraud-combo-002" 85000.00 "2025-08-25T05:45:00" "Suspicious Location"

echo "========================================="
echo "CREATING FREQUENCY-BASED SCENARIOS"
echo "========================================="

# Create multiple transactions for frequency testing
FREQ_ACCOUNT="frequency-test-001"
BASE_TIME="2025-08-25T10"

# 12 transactions in one hour (triggers hourly limit)
for i in {0..11}; do
    minutes=$(printf "%02d" $((i * 5)))
    make_transaction "$FREQ_ACCOUNT" 100.00 "${BASE_TIME}:${minutes}:00" "Frequent Location"
done

# Create daily frequency pattern
DAILY_FREQ_ACCOUNT="daily-frequency-001"
# 52 transactions in one day (triggers daily limit)
for hour in {8..20}; do
    for minute in 00 15 30 45; do
        make_transaction "$DAILY_FREQ_ACCOUNT" 50.00 "2025-08-25T$(printf "%02d" $hour):${minute}:00" "Same Location"
    done
done

echo "========================================="
echo "CREATING LOCATION ANOMALY SCENARIOS"
echo "========================================="

# Multiple locations in one day (4+ locations = +0.3 score)
LOCATION_ACCOUNT="location-jumper-001"

make_transaction "$LOCATION_ACCOUNT" 200.00 "2025-08-24T08:00:00" "New York, NY"
make_transaction "$LOCATION_ACCOUNT" 150.00 "2025-08-24T10:30:00" "Los Angeles, CA"
make_transaction "$LOCATION_ACCOUNT" 300.00 "2025-08-24T13:15:00" "Chicago, IL"
make_transaction "$LOCATION_ACCOUNT" 175.00 "2025-08-24T16:45:00" "Miami, FL"
make_transaction "$LOCATION_ACCOUNT" 225.00 "2025-08-24T19:20:00" "Seattle, WA"

# Another location jumper with higher amounts
LOCATION_ACCOUNT_2="location-jumper-002"
make_transaction "$LOCATION_ACCOUNT_2" 500.00 "2025-08-24T09:00:00" "Boston, MA"
make_transaction "$LOCATION_ACCOUNT_2" 750.00 "2025-08-24T12:30:00" "Dallas, TX"
make_transaction "$LOCATION_ACCOUNT_2" 425.00 "2025-08-24T15:45:00" "Denver, CO"
make_transaction "$LOCATION_ACCOUNT_2" 680.00 "2025-08-24T18:15:00" "Phoenix, AZ"

echo "========================================="
echo "CREATING COMPLEX FRAUD SCENARIOS"
echo "========================================="

# Multiple rules triggered - should definitely be fraudulent
COMPLEX_FRAUD="complex-fraud-001"

# Very high amount + off-hours + multiple locations
make_transaction "$COMPLEX_FRAUD" 95000.00 "2025-08-25T03:30:00" "Suspicious Location A"
make_transaction "$COMPLEX_FRAUD" 87000.00 "2025-08-25T04:15:00" "Suspicious Location B"
make_transaction "$COMPLEX_FRAUD" 76000.00 "2025-08-25T05:00:00" "Suspicious Location C"
make_transaction "$COMPLEX_FRAUD" 92000.00 "2025-08-25T05:45:00" "Suspicious Location D"

echo "========================================="
echo "CREATING EDGE CASES"
echo "========================================="

# Transactions without location
make_transaction_no_location "no-location-001" 500.00 "2025-08-24T14:30:00"
make_transaction_no_location "no-location-002" 15000.00 "2025-08-24T15:45:00"

# Exact threshold amounts
make_transaction "threshold-test-001" 10000.00 "2025-08-24T14:00:00" "Threshold City"
make_transaction "threshold-test-002" 50000.00 "2025-08-24T15:30:00" "Exact Amount City"

# Boundary time testing (just outside off-hours)
make_transaction "boundary-test-001" 1000.00 "2025-08-25T01:59:00" "Just Before Off Hours"
make_transaction "boundary-test-002" 1000.00 "2025-08-25T02:00:00" "Start Off Hours"
make_transaction "boundary-test-003" 1000.00 "2025-08-25T06:00:00" "End Off Hours"
make_transaction "boundary-test-004" 1000.00 "2025-08-25T06:01:00" "Just After Off Hours"

echo "========================================="
echo "CREATING MIXED LEGITIMATE HIGH-VALUE TRANSACTIONS"
echo "========================================="

# Legitimate high-value business transactions (normal hours, consistent location)
make_transaction "business-001" 45000.00 "2025-08-24T14:30:00" "Corporate Office NYC"
make_transaction "business-002" 32000.00 "2025-08-24T11:15:00" "Business District LA"
make_transaction "business-003" 28000.00 "2025-08-24T16:45:00" "Financial District Chicago"

echo "========================================="
echo "DATA POPULATION COMPLETE!"
echo "========================================="
echo "Summary of created transactions:"
echo "- Normal transactions: ~15"
echo "- High amount transactions: ~7"
echo "- Off-hours transactions: ~5"
echo "- Fraudulent combinations: ~2"
echo "- Frequency test transactions: ~64"
echo "- Location anomaly transactions: ~9"
echo "- Complex fraud scenarios: ~4"
echo "- Edge cases: ~6"
echo "- Business transactions: ~3"
echo ""
echo "Total: ~110+ transactions"
echo ""
echo "Check your database and application logs to see the fraud detection results!"