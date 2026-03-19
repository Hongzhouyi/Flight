#!/usr/bin/env bash
set -euo pipefail

javac src/FlightServer.java
java -cp src FlightServer > /tmp/flight-server.log 2>&1 &
SERVER_PID=$!
trap 'kill $SERVER_PID >/dev/null 2>&1 || true' EXIT

sleep 1

INDEX_CONTENT=$(curl -sS http://127.0.0.1:5000/)
RESULT_CONTENT=$(curl -sS "http://127.0.0.1:5000/search?origin=LHR&destination=CDG&departure_date=2026-03-20")

[[ "$INDEX_CONTENT" == *"Flight Search (Java version)"* ]]
[[ "$RESULT_CONTENT" == *"FL101"* ]]
[[ "$RESULT_CONTENT" == *"FL205"* ]]

echo "search test passed"
