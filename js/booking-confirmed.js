const confirmedFlightSummary = document.getElementById("confirmed-flight-summary");
const confirmedPassengerSummary = document.getElementById("confirmed-passenger-summary");
const confirmedSeatSummary = document.getElementById("confirmed-seat-summary");

const savedFlight = localStorage.getItem("selectedFlight");
const savedPassenger = localStorage.getItem("passengerDetails");
const savedSeat = localStorage.getItem("selectedSeat");

if (!savedFlight) {
  confirmedFlightSummary.textContent = "No flight selected.";
} else {
  const flight = JSON.parse(savedFlight);
  confirmedFlightSummary.textContent =
    `${flight.airline} ${flight.flightNumber} | ${flight.from} to ${flight.to} | ${flight.departDate}`;
}

if (!savedPassenger) {
  confirmedPassengerSummary.textContent = "No passenger details saved.";
} else {
  const passenger = JSON.parse(savedPassenger);
  confirmedPassengerSummary.textContent =
    `${passenger.fullName} | ${passenger.email}`;
}

if (!savedSeat) {
  confirmedSeatSummary.textContent = "No seat selected.";
} else {
  confirmedSeatSummary.textContent = savedSeat;
}

localStorage.removeItem("selectedFlight");
localStorage.removeItem("passengerDetails");
localStorage.removeItem("selectedSeat");
localStorage.removeItem("paymentDetails");
