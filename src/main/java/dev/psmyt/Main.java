package dev.psmyt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.scheduling.support.CronExpression;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.ZoneOffset.UTC;
import static java.util.Optional.ofNullable;


public class Main {
    static final String SUMMARY = "prints the next n execution dates for a given Spring cron expression";

    static final DateTimeFormatter HUMAN_READABLE = DateTimeFormatter.ofPattern("<EEE MMM d yyyy HH:mm:ss z>");

    static final Options OPTIONS = new Options()
            .addOption("h", "prints help")
            .addOption("z", true, "which timezone to use. default: system")
            .addOption("f", true, "output format. default: <EEE MMM d yyyy HH:mm:ss z>")
            .addOption("n", true, "how many cron dates to print. default: 5")
            .addOption("s", true, "the string to use as a separator. default: '\\n'");

    record ParsedCommandLine(String arg, ZoneId z, DateTimeFormatter f, int n, String s) {
    }

    static void main(String[] args) {
        try {
            ParsedCommandLine parsed = parse(args);
            System.out.println(cronnext(parsed));
        } catch (Exception e) {
            System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
            System.exit(1);
        }
    }

    private static String cronnext(ParsedCommandLine cmd) {
        CronExpression cronExpression = CronExpression.parse(cmd.arg());
        return Stream.iterate(ZonedDateTime.now(cmd.z()), cronExpression::next)
                .skip(1)
                .limit(cmd.n())
                .map(cmd.f()::format)
                .collect(Collectors.joining(cmd.s()));
    }

    private static ParsedCommandLine parse(String[] args) throws ParseException, IOException {
        CommandLine cmd = new DefaultParser().parse(OPTIONS, args);

        if (cmd.hasOption("h") || cmd.getArgList().contains("help")) {
            printHelp();
            System.exit(0);
        }

        int n = ofNullable(cmd.getOptionValue("n"))
                .map(Integer::parseInt)
                .orElse(5);
        ZoneId z = ofNullable(cmd.getOptionValue("z"))
                .map(ZoneId::of)
                .orElse(ZoneId.systemDefault());
        String s = ofNullable(cmd.getOptionValue("s"))
                .map(StringEscapeUtils::unescapeJava)
                .orElse("\n");
        DateTimeFormatter f = ofNullable(cmd.getOptionValue("f"))
                .map(DateTimeFormatter::ofPattern)
                .orElse(HUMAN_READABLE);
        String cronExpression = cmd.getArgList()
                .stream()
                .reduce((a1, a2) -> {
                    throw new UnsupportedOperationException(
                            "unsupported number of arguments: expected 1 argument, received multiple: %s, %s..."
                                    .formatted(a1, a2)
                    );
                })
                .orElseThrow(() -> new IllegalArgumentException("provide an argument - a valid Spring cron expression"));

        return new ParsedCommandLine(cronExpression, z, f, n, s);
    }

    private static void printHelp() throws IOException {
        String footer = """
                example:
                \033[48;5;236m\
                $ cronnext "10 0 4 */1 * *" -z UTC -n 2 -s ", "\
                \033[49m
                - will print (given that it's currently %s):
                %s
                """
                .formatted(
                        ZonedDateTime.now(ZoneId.systemDefault()).format(HUMAN_READABLE),
                        cronnext(new ParsedCommandLine("10 0 4 */1 * *", UTC, HUMAN_READABLE, 2, ", "))
                );
        HelpFormatter.builder()
                .setShowSince(false)
                .get()
                .printHelp("cronnext <cron arg>", SUMMARY, OPTIONS, footer, false);
    }
}
