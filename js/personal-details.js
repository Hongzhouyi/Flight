const summaryText = document.getElementById("flight-summary-text");

const savedFlight = localStorage.getItem("selectedFlight");

if (!savedFlight) {
  summaryText.textContent = "No flight selected yet. Please go back and choose a flight.";
} else {
  const flight = JSON.parse(savedFlight);

  summaryText.textContent =
    `${flight.airline} ${flight.flightNumber} | ${flight.from} to ${flight.to} | ${flight.departDate} | ${flight.departTime} - ${flight.arrivalTime} | ${flight.cabinClass} | GBP ${flight.price}`;
}
