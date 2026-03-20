import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FlightServer {
    private static final Path DB_PATH = Path.of("flights.db");
    private static final Path CSS_PATH = Path.of("static", "styles.css");
    private static final Pattern IATA_PATTERN = Pattern.compile("^[A-Za-z]{0,3}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^$|^\\d{4}-\\d{2}-\\d{2}$");
    private static final int DEFAULT_PORT = 8000;

    public static void main(String[] args) throws IOException {
        initDatabase();
        int port = resolvePort();
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                send(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                send(exchange, 200, renderIndex(), "text/html; charset=utf-8");
                return;
            }

            if ("/search".equals(path)) {
                handleSearch(exchange);
                return;
            }

            if ("/personal-details".equals(path)) {
                handlePersonalDetails(exchange);
                return;
            }

            if ("/static/styles.css".equals(path)) {
                handleCss(exchange);
                return;
            }

            send(exchange, 404, "<h1>404 Not Found</h1>", "text/html; charset=utf-8");
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Flight server running on http://0.0.0.0:" + port);
    }

    private static int resolvePort() {
        String envPort = System.getenv("PORT");
        if (envPort == null || envPort.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(envPort.trim());
        } catch (NumberFormatException ignored) {
            return DEFAULT_PORT;
        }
    }

    private static void initDatabase() throws IOException {
        runSql("""
            CREATE TABLE IF NOT EXISTS flights (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                flight_number TEXT NOT NULL,
                origin TEXT NOT NULL,
                destination TEXT NOT NULL,
                departure_date TEXT NOT NULL,
                departure_time TEXT NOT NULL,
                arrival_time TEXT NOT NULL,
                price REAL NOT NULL
            );
            """);

        String countOutput = runSqlSingle("SELECT COUNT(*) FROM flights;");
        if ("0".equals(countOutput)) {
            runSql("""
                INSERT INTO flights (flight_number, origin, destination, departure_date, departure_time, arrival_time, price) VALUES
                ('FL101', 'LHR', 'CDG', '2026-03-20', '08:30', '10:50', 120.00),
                ('FL205', 'LHR', 'CDG', '2026-03-20', '14:10', '16:30', 150.00),
                ('FL330', 'LHR', 'AMS', '2026-03-20', '09:40', '12:00', 135.00),
                ('FL411', 'MAN', 'BCN', '2026-03-21', '11:15', '14:35', 180.00),
                ('FL512', 'LHR', 'CDG', '2026-03-21', '07:00', '09:20', 110.00);
                """);
        }
    }

    private static void handleSearch(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI());

        String origin = normalize(params.getOrDefault("origin", ""));
        String destination = normalize(params.getOrDefault("destination", ""));
        String date = params.getOrDefault("departure_date", "").trim();

        if (!IATA_PATTERN.matcher(origin).matches() || !IATA_PATTERN.matcher(destination).matches() || !DATE_PATTERN.matcher(date).matches()) {
            send(exchange, 400, "<h1>400 Bad Request</h1><p>Invalid search input.</p>", "text/html; charset=utf-8");
            return;
        }

        List<Map<String, String>> flights = queryFlights(origin, destination, date);
        send(exchange, 200, renderResults(flights, origin, destination, date), "text/html; charset=utf-8");
    }

    private static void handlePersonalDetails(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI());
        String flightId = params.getOrDefault("flight_id", "").trim();

        send(exchange, 200, renderPersonalDetails(flightId), "text/html; charset=utf-8");
    }

    private static void handleCss(HttpExchange exchange) throws IOException {
        if (!Files.exists(CSS_PATH)) {
            send(exchange, 404, "Not Found", "text/plain; charset=utf-8");
            return;
        }
        send(exchange, 200, Files.readString(CSS_PATH), "text/css; charset=utf-8");
    }

    private static List<Map<String, String>> queryFlights(String origin, String destination, String date) throws IOException {
        StringBuilder sql = new StringBuilder(
            "SELECT id, flight_number, origin, destination, departure_date, departure_time, arrival_time, price FROM flights WHERE 1=1"
        );

        if (!origin.isEmpty()) {
            sql.append(" AND origin = '").append(origin).append("'");
        }
        if (!destination.isEmpty()) {
            sql.append(" AND destination = '").append(destination).append("'");
        }
        if (!date.isEmpty()) {
            sql.append(" AND departure_date = '").append(date).append("'");
        }

        sql.append(" ORDER BY departure_time;");

        String output = runSqlRaw(sql.toString(), true);
        List<Map<String, String>> rows = new ArrayList<>();

        if (output.isBlank()) {
            return rows;
        }

        String[] lines = output.split("\\R");
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\\|", -1);
            if (parts.length != 8) {
                continue;
            }
            Map<String, String> row = new HashMap<>();
            row.put("id", parts[0]);
            row.put("flight_number", parts[1]);
            row.put("origin", parts[2]);
            row.put("destination", parts[3]);
            row.put("departure_date", parts[4]);
            row.put("departure_time", parts[5]);
            row.put("arrival_time", parts[6]);
            row.put("price", parts[7]);
            rows.add(row);
        }

        return rows;
    }

    private static String renderIndex() {
        return """
            <!doctype html>
            <html lang=\"en\">
              <head>
                <meta charset=\"utf-8\">
                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
                <title>Flight Search</title>
                <link rel=\"stylesheet\" href=\"/static/styles.css\">
              </head>
              <body>
                <main class=\"container\">
                  <h1>Flight Search (Java version)</h1>
                  <p>Search available flights and show the results page.</p>
                  <form action=\"/search\" method=\"get\" class=\"search-form\">
                    <label>Origin (IATA)<input type=\"text\" name=\"origin\" maxlength=\"3\" placeholder=\"e.g. LHR\"></label>
                    <label>Destination (IATA)<input type=\"text\" name=\"destination\" maxlength=\"3\" placeholder=\"e.g. CDG\"></label>
                    <label>Departure date<input type=\"date\" name=\"departure_date\"></label>
                    <button type=\"submit\">Search flights</button>
                  </form>
                </main>
              </body>
            </html>
            """;
    }

    private static String renderResults(List<Map<String, String>> flights, String origin, String destination, String date) {
        StringBuilder rows = new StringBuilder();
        for (Map<String, String> flight : flights) {
            rows.append("<tr>")
                .append("<td>").append(escapeHtml(flight.get("flight_number"))).append("</td>")
                .append("<td>").append(escapeHtml(flight.get("origin"))).append(" → ").append(escapeHtml(flight.get("destination"))).append("</td>")
                .append("<td>").append(escapeHtml(flight.get("departure_date"))).append("</td>")
                .append("<td>").append(escapeHtml(flight.get("departure_time"))).append("</td>")
                .append("<td>").append(escapeHtml(flight.get("arrival_time"))).append("</td>")
                .append("<td>£").append(escapeHtml(flight.get("price"))).append("</td>")
                .append("<td><a href=\"/personal-details?flight_id=")
                .append(escapeHtml(flight.get("id")))
                .append("\">Select Flight</a></td>")
                .append("</tr>");
        }

        String table = flights.isEmpty()
            ? "<p class=\"empty\">No matching flights found.</p>"
            : "<table><thead><tr><th>Flight</th><th>Route</th><th>Date</th><th>Depart</th><th>Arrive</th><th>Price</th><th>Action</th></tr></thead><tbody>"
                + rows + "</tbody></table>";

        return """
            <!doctype html>
            <html lang=\"en\">
              <head>
                <meta charset=\"utf-8\">
                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
                <title>Flight Results</title>
                <link rel=\"stylesheet\" href=\"/static/styles.css\">
              </head>
              <body>
                <main class=\"container\">
                  <h1>Flight Results</h1>
                  <p>Showing results for <strong>%s</strong> → <strong>%s</strong> on <strong>%s</strong></p>
                  <a class=\"back-link\" href=\"/\">← Back to search</a>
                  %s
                </main>
              </body>
            </html>
            """.formatted(
            escapeHtml(origin.isBlank() ? "ANY" : origin),
            escapeHtml(destination.isBlank() ? "ANY" : destination),
            escapeHtml(date.isBlank() ? "ANY DATE" : date),
            table
        );
    }

    private static String renderPersonalDetails(String flightId) {
        return """
            <!doctype html>
            <html lang=\"en\">
              <head>
                <meta charset=\"utf-8\">
                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
                <title>Personal Details</title>
                <link rel=\"stylesheet\" href=\"/static/styles.css\">
              </head>
              <body>
                <main class=\"container\">
                  <h1>Personal Details</h1>
                  <p>Selected flight id: <strong>%s</strong></p>
                  <form class=\"search-form\">
                    <label>Full name<input type=\"text\" name=\"full_name\" placeholder=\"Enter full name\"></label>
                    <label>Email<input type=\"email\" name=\"email\" placeholder=\"Enter email\"></label>
                  </form>
                  <a class=\"back-link\" href=\"/\">← Back to search</a>
                </main>
              </body>
            </html>
            """.formatted(escapeHtml(flightId.isBlank() ? "not selected" : flightId));
    }

    private static void send(HttpExchange exchange, int code, String body, String contentType) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(code, payload.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(payload);
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return result;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            result.put(key, value);
        }
        return result;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }

    private static void runSql(String sql) throws IOException {
        runSqlRaw(sql, false);
    }

    private static String runSqlSingle(String sql) throws IOException {
        String output = runSqlRaw(sql, true).trim();
        if (output.contains("\n")) {
            return output.substring(0, output.indexOf('\n'));
        }
        return output;
    }

    private static String runSqlRaw(String sql, boolean captureStdout) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("sqlite3");
        command.add(DB_PATH.toString());
        if (captureStdout) {
            command.add("-separator");
            command.add("|");
        }
        command.add(sql);

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();
        String stdout;
        String stderr;
        try {
            stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("sqlite3 failed: " + stderr.trim());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("sqlite3 interrupted", exception);
        }

        return stdout;
    }
}
