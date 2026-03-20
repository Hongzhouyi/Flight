const paymentFlightSummary = document.getElementById("payment-flight-summary");
const paymentPassengerSummary = document.getElementById("payment-passenger-summary");
const paymentSeatSummary = document.getElementById("payment-seat-summary");

const savedFlight = localStorage.getItem("selectedFlight");
const savedPassenger = localStorage.getItem("passengerDetails");
const savedSeat = localStorage.getItem("selectedSeat");

if (!savedFlight) {
  paymentFlightSummary.textContent = "No flight selected yet.";
} else {
  const flight = JSON.parse(savedFlight);
  paymentFlightSummary.textContent =
    `${flight.airline} ${flight.flightNumber} | ${flight.from} to ${flight.to} | ${flight.departDate}`;
}

if (!savedPassenger) {
  paymentPassengerSummary.textContent = "No passenger details saved yet.";
} else {
  const passenger = JSON.parse(savedPassenger);
  paymentPassengerSummary.textContent =
    `${passenger.fullName} | ${passenger.email}`;
}

if (!savedSeat) {
  paymentSeatSummary.textContent = "No seat selected yet.";
} else {
  paymentSeatSummary.textContent = savedSeat;
}
