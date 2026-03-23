const summaryText = document.getElementById("flight-summary-text");
const nextButton = document.getElementById("next-button");
const fullNameInput = document.getElementById("full-name");
const emailInput = document.getElementById("email");

const savedFlight = localStorage.getItem("selectedFlight");

if (!savedFlight) {
  summaryText.textContent = "No flight selected yet. Please go back and choose a flight.";
} else {
  const flight = JSON.parse(savedFlight);

  summaryText.textContent =
    `${flight.airline} ${flight.flightNumber} | ${flight.from} to ${flight.to} | ${flight.departDate} | ${flight.departTime} - ${flight.arrivalTime} | ${flight.cabinClass} | GBP ${flight.price}`;
}

nextButton.addEventListener("click", () => {
  const fullName = fullNameInput.value.trim();
  const email = emailInput.value.trim();

  if (!fullName || !email) {
    alert("Please enter your full name and email.");
    return;
  }

  const passengerDetails = {
    fullName,
    email
  };

  localStorage.setItem("passengerDetails", JSON.stringify(passengerDetails));
  window.location.href = "seat-selection.html";
});
