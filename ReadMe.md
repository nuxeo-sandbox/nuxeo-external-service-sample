
# nuxeo-external-service-sample

## About

This repository contains a sample code to manage comunication between a Nuxeo Server and an external service using `nuxeo-stream` (Kafka).

## Principles

![dag](doc/principles.png)

## Sub Modules

### nuxeo-external-service-core

This is a Nuxeo plugin that needs to be deployed in a Nuxeo Server and provides:

 - implementation of the service wrapper
    - send message to the external application
    - process messages from the external application
 - implementation of the Automation Operation
    - expose service wrapper via http
 - implementation of the Computation  
    - read messages from the external application and call the service wrapper

### nuxeo-external-service-cli

Basic CLI built using `nuxeo-stream` and allowing to:

 - read Nuxeo's request from Kafka
 - post response for Nuxeo in Kafka
 - call the service wrapper via Automation

This CLI could be used:

 - as a sample code
 - as a way to interface with Nuxeo

## Build

Simply run the maven build:

    mvn clean install

