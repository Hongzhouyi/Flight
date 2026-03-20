const flightSummaryText = document.getElementById("flight-summary-text");
const passengerSummaryText = document.getElementById("passenger-summary-text");

const savedFlight = localStorage.getItem("selectedFlight");
const savedPassenger = localStorage.getItem("passengerDetails");

if (!savedFlight) {
  flightSummaryText.textContent = "No flight selected yet.";
} else {
  const flight = JSON.parse(savedFlight);
  flightSummaryText.textContent =
    `${flight.airline} ${flight.flightNumber} | ${flight.from} to ${flight.to} | ${flight.departDate}`;
}

if (!savedPassenger) {
  passengerSummaryText.textContent = "No passenger details saved yet.";
} else {
  const passenger = JSON.parse(savedPassenger);
  passengerSummaryText.textContent =
    `${passenger.fullName} | ${passenger.email}`;
}
