
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

The provided implementation of the {{ExternalServiceWrapper}} service is basically an empty shell that provide the "pipes", but no specific business logic for handling responses is implemented.

The {{ExternalServiceWrapper}} service expose an extension point that can be used to configure the target service:

     <extension point="config" target="com.nuxeo.external.service.wrapper">
       <externalService id="externalservice" name="externalservice">
	     <namespace>externalservice</namespace>
       </externalService>
     </extension>

The `name` attribute is used to identify the target service when using the API `ExternalServiceWrapper.postMessage(serviceName, message)`.

The `namespace` is used to define the Nuxeo Stream (kafka) namespace used to communicate with the corresponding the external service.

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

