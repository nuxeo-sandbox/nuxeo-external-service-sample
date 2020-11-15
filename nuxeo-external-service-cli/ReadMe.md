
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

Example:

    java -jar target/nuxeo-external-service-cli-1.0-SNAPSHOT.jar -c kafka.properties -n externalservice -p nuxeo- -m {"docId":"whatever"}


## Testing

### Setup test environment

Here are some guidelines to run a full setup on a local laptop.

#### Start Kafka

You can use the provided `docker-compose.yaml` to start a local Kafka in single node:

    cd ../docker
    docker-compose up

#### Configure Nuxeo to use your kafka local node     


Update the `nuxeo.conf` to include:

    kafka.enabled=true
    kafka.bootstrap.servers=127.0.0.1:9092
    nuxeo.stream.work.enabled=true
    nuxeo.pubsub.provider=stream

Everything was tested with Nuxeo 11.2.13, you can use [this zip](https://packages.nuxeo.com/#browse/search=keyword%3Dnuxeo-server-tomcat%2011.2.13:d6e4a50aba3c8a19ffc769323dbb2995) or the corresponding Docker image.

### Testing from CLI

*Make Nuxeo Generate a request*

Use Nuxeo Automation http API to call the wrapper service and make Nuxeo post a message
 
    scripts/triggerNuxeoRequest.sh

*Read the message*

    scripts/getRequestFromNuxeo.sh -c kafka.properties  -n externalservice -p nuxeo- 

*Send a response*

    scripts/sendResponse.sh -c kafka.properties  -n externalservice -p nuxeo- -m {"docId":"whatever"}


