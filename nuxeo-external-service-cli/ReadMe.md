
# nuxeo-external-service-cli

## About

This is simple Command Line Interface to allow an external application to communicate with a Nuxeo Application via `nuxeo-stream`.

The CLI is built with very limited dependencies to Nuxeo and can be used:

 - as a way to communicate with Nuxeo via CLI
 - as a sample code

## Build


Simply run the maven build:

    mvn clean install


Thank to the maven shade plugin the jar found in `target` is an uber-jar containing all the dependencies.

As a result it can be run with:

    java -jar nuxeo-external-service-cli/target/nuxeo-external-service-cli-1.0-SNAPSHOT.jar 

