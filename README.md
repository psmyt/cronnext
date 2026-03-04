# cronnext
## prints the next n execution dates for a given Spring cron expression
### how:
1. install [GraalVM](https://www.graalvm.org/latest/getting-started/)
2. run `mvn -Pnative package`
3. this will produce a binary named cronnext in /target
4. run `cronnext <your expression>` to see future execution dates for the provided cron expression
5. run `cronnext help` to examine options