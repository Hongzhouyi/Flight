const flightSummaryText = document.getElementById("flight-summary-text");
const passengerSummaryText = document.getElementById("passenger-summary-text");
const seatSummaryText = document.getElementById("seat-summary-text");
const nextButton = document.getElementById("next-button");
const seatButtons = document.querySelectorAll(".seat-button");

const savedFlight = localStorage.getItem("selectedFlight");
const savedPassenger = localStorage.getItem("passengerDetails");
const savedSeat = localStorage.getItem("selectedSeat");

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

if (savedSeat) {
  seatSummaryText.textContent = savedSeat;
  nextButton.disabled = false;
}

seatButtons.forEach((button) => {
  if (savedSeat === button.dataset.seat) {
    button.classList.add("selected");
  }

  button.addEventListener("click", () => {
    seatButtons.forEach((item) => item.classList.remove("selected"));
    button.classList.add("selected");

    const seat = button.dataset.seat;
    localStorage.setItem("selectedSeat", seat);
    seatSummaryText.textContent = seat;
    nextButton.disabled = false;
  });
});

nextButton.addEventListener("click", () => {
  window.location.href = "payment.html";
});
