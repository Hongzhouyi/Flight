# Flight

Java implementation of the flight search results page for COMP2850.

## What this version includes

- Java HTTP server (`HttpServer`) serving:
  - `/` search form page
  - `/search` results page
  - `/static/styles.css` styles
- SQLite database (`flights.db`) initialized and seeded automatically.
- Search filters for origin, destination, and departure date.
- Results table rendering from database records.
- Input validation for IATA code and date format.

## Run

```bash
javac src/FlightServer.java
java -cp src FlightServer
```

Open: <http://127.0.0.1:5000>

## Quick test

```bash
bash tests/test_search.sh
```

## Error and debug trace

See `docs/error-trace.md` for migration/debug steps and captured errors.
