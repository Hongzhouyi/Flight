const form = document.getElementById("search-form");
const resultsContainer = document.getElementById("results");
const selectedFlightBox = document.getElementById("selected-flight");
const selectedFlightText = document.getElementById("selected-flight-text");

let allFlights = [];

async function loadFlights() {
  const response = await fetch("data/flights.json");
  allFlights = await response.json();
  displayFlights(allFlights);
}

function selectFlight(flightId) {
  const flight = allFlights.find((item) => item.id === flightId);

  if (!flight) {
    return;
  }

  localStorage.setItem("selectedFlight", JSON.stringify(flight));

  selectedFlightText.textContent =
    `${flight.airline} ${flight.flightNumber} | ${flight.from} to ${flight.to} | ${flight.departDate} | GBP ${flight.price}`;

  selectedFlightBox.classList.remove("hidden");
  window.location.href = "personal-details.html";
}

window.selectFlight = selectFlight;

function displayFlights(flights) {
  if (!flights.length) {
    resultsContainer.innerHTML = '<p class="empty-state">No matching flights found.</p>';
    return;
  }

  resultsContainer.innerHTML = flights
    .map(
      (flight) => `
        <article class="flight-card">
          <div class="flight-card-top">
            <h3>${flight.airline} ${flight.flightNumber}</h3>
            <span class="price">GBP ${flight.price}</span>
          </div>
          <p><strong>Route:</strong> ${flight.from} to ${flight.to}</p>
          <p><strong>Date:</strong> ${flight.departDate}</p>
          <p><strong>Time:</strong> ${flight.departTime} - ${flight.arrivalTime}</p>
          <p><strong>Cabin:</strong> ${flight.cabinClass}</p>
          <p><strong>Seats left:</strong> ${flight.passengersAvailable}</p>
          <button type="button" onclick="selectFlight(${flight.id})">Select Flight</button>
        </article>
      `
    )
    .join("");
}

form.addEventListener("submit", (event) => {
  event.preventDefault();

  const from = document.getElement
