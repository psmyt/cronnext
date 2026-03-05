import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CronnextIT {

    @Test
    public void noOptionsTest() throws IOException {
        Instant testStarted = Instant.now();
        Process process = executeWithArgs(List.of("0 0/15 * * * ?"));

        assertEquals(0, process.exitValue());

        String output = new String(process.getInputStream().readAllBytes());

        assertEquals(5, output.lines().count());

        List<Instant> dates;
        try {
            dates = output.lines()
                    .map(DateTimeFormatter.ofPattern("<EEE MMM d yyyy HH:mm:ss z>")::parse)
                    .map(Instant::from)
                    .toList();
        } catch (DateTimeParseException e) {
            throw new AssertionError(e);
        }

        assertTrue(
                Duration.between(testStarted, dates.getFirst()).compareTo(Duration.ofMinutes(15)) < 0,
                "expected the first date(%s) to be within 15 min from the test start time(%s)"
                        .formatted(dates.getFirst(), testStarted)
        );

        assertEquals(
                dates.get(0).plus(15, ChronoUnit.MINUTES),
                dates.get(1),
                "expected first(%s) and second(%s) dates to be precisely 15 minutes apart".formatted(dates.get(0), dates.get(1))
        );
    }

    private Process executeWithArgs(List<String> args) throws IOException {
        List<String> allArgs = new ArrayList<>(List.of("target/./cronnext"));
        allArgs.addAll(args);
        Process process = new ProcessBuilder(allArgs).start();
        assertDoesNotThrow(() -> process.waitFor(Duration.ofSeconds(1)));
        return process;
    }
}
