package dev.psmyt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;
import org.springframework.scheduling.support.CronExpression;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.ZoneOffset.UTC;
import static java.util.Optional.ofNullable;


public class Main {
    static final DateTimeFormatter HUMAN_READABLE = DateTimeFormatter.ofPattern("<EEEE MMMM d yyyy HH:mm:ss z>");
    static final Options OPTIONS = new Options()
            .addOption("h", "prints help")
            .addOption("n", true, "how many cron dates to print. default: 5")
            .addOption("z", true, "which zone to use. default: system")
            .addOption("s", true, "the string to use as a separator. default: '\\n'")
            .addOption("f", true, "output format. default: <EEEE MMMM d yyyy HH:mm:ss z>");

    static void main(String[] args) {
        try {
            CommandLine cmd = new DefaultParser().parse(OPTIONS, args);

            if (cmd.hasOption("h") || cmd.getArgList().contains("help")) {
                printHelp();
                System.exit(0);
            }

            int numberOfInstances = ofNullable(cmd.getOptionValue("n"))
                    .map(Integer::parseInt)
                    .orElse(5);
            ZoneId zoneId = ofNullable(cmd.getOptionValue("z"))
                    .map(ZoneId::of)
                    .orElse(ZoneId.systemDefault());
            String separator = cmd.hasOption("s") ? cmd.getOptionValue("s") : "\n";
            DateTimeFormatter formatter = ofNullable(cmd.getOptionValue("f"))
                    .map(DateTimeFormatter::ofPattern)
                    .orElse(HUMAN_READABLE);
            CronExpression cronExpression = CronExpression.parse(cmd.getArgList().getFirst());

            System.out.println(cronnext(cronExpression, zoneId, formatter, numberOfInstances, separator));

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static String cronnext(CronExpression cronExpression, ZoneId zoneId, DateTimeFormatter formatter, int numberOfInstances, String separator) {
        return Stream.iterate(ZonedDateTime.now(zoneId), cronExpression::next)
                .skip(1)
                .limit(numberOfInstances)
                .map(formatter::format)
                .collect(Collectors.joining(separator));
    }

    private static void printHelp() throws IOException {
        String summary = "prints the next n execution dates for a given Spring cron expression";
        String footer = """
                example:
                \033[48;5;236m\
                $ cronnext "10 0 4 */1 * *" -z UTC -n 2 -s ", "\
                \033[49m
                - will print (given that it's currently %s):
                %s
                """
                .formatted(
                        Instant.now().atZone(ZoneId.systemDefault()).format(HUMAN_READABLE),
                        cronnext(CronExpression.parse("10 0 4 */1 * *"), UTC, HUMAN_READABLE, 2, ", ")
                );
        HelpFormatter.builder()
                .setShowSince(false)
                .get()
                .printHelp("cronnext <cron expression>", summary, OPTIONS, footer, false);
    }
}
