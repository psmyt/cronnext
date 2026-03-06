# cronnext

A tool for evaluating Spring cron expressions. Unlike other evaluators, this one simply prints future matching dates using [the real thing](https://docs.spring.io/spring-framework/docs/7.0.5/javadoc-api/org/springframework/scheduling/support/CronExpression.html) - the same code that triggers scheduled events in real Spring applications.

## Installation

Download the [latest release](https://github.com/psmyt/cronnext/releases/latest) for your platform, or compile from source:
```shell
mvn -Pnative package
```

Compiling from source requires [GraalVM](https://www.graalvm.org/latest/getting-started/).

## Usage
```shell
cronnext <expression>   # print next matching dates
cronnext help           # show all options
```