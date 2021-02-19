## Running a test in K8s


To run a full test you need to:

 - deploy a Nuxeo Cluster
    - with Kafka enabled
    - with the `nuxeo-external-service-core` plugin

 - deploy a container that will "simulate the external service"
    - running the CLI `nuxeo-external-service-cli` is enough

### Simulating the external service

#### Building a Docker image

The provided Dockerfile is an example for running a simple container:

 - JDK (need Sun/Oracle JDK 11)
 - copy "uber-jar" containing the CLI
 - copy schell scripts
 - copy default config

Obviously the `conf/kafka.properties` need to be updated to match your Kafka deployment.
In this current example, we use a multi-tenant Nuxeo deployment based on [this collection of helm charts](https://github.com/tiry/nuxeo-helm-chart).

    kafka.bootstrap.servers=nuxeo-kafka.nx-shared-storage.svc.cluster.local:9092

#### Deploying the container

You can simply run the provided create-pod script

    ./create-pod.sh stream-cli

Here `stream-cli` is the namespace where the "fake" external service will be deployed.
Technically, the only important point is to have access to the same Kafka cluster.

### Deploying the Nuxeo Cluster with the plugin

For this to work, you need to deploy the Nuxeo Cluster:

 - using kafka
 - add the `nuxeo-external-service-core`
    - integrate in one of the Marketplace package
    - or, directly add in the `bundles` directory

In the context of this test, [this custom Docker image is used](https://github.com/tiry/nuxeo-tenant-test-image/blob/master/Dockerfile-stream).

### Running an end-to-end test

#### Send a message from Nuxeo 

Let's start by getting the default-domain document since it is usually there by default:

    curl -X GET -H "X-NXproperties: dublincore" -u Administrator:Administrator  https://company-g.multitenant.nuxeo.com/nuxeo/api/v1/path/default-domain


    {
    "entity-type":"document",
    "repository":"default",
    "uid":"9897f584-1e65-472e-847f-acc975d0ad89",
    "path":"/default-domain",
    "type":"Domain",
    ...
    "title":"Domain",
    "lastModified":"2021-02-18T23:27:12.203Z",
    "properties":{
        "dc:description":null,
        ...
        "dc:created":"2021-02-18T23:27:12.203Z",
        "dc:title":"Domain",
        ...
    }

We will use the Document uuid : here 9897f584-1e65-472e-847f-acc975d0ad89

Just call the Automation Operation via http:

    curl -X POST -H "Content-Type: application/json+nxrequest"   -u Administrator:Administrator -d '{"params":{"serviceName":"externalservice", "command":"doSomething", "parameters": "docId=9897f584-1e65-472e-847f-acc975d0ad89"}}'  https://company-g.multitenant.nuxeo.com/nuxeo/api/v1/automation/Service.External


    {"entity-type":"string","value":"doSomething:65285744-1e48-4963-a0c1-1af8dcd1e116"}

#### Simulate the external service

Get the pod name:

    kubectl get pods -n stream-cli

Enter the container:

    kubectl exec -ti -n stream-cli stream-client-6cdb9b686-b27x2 -- bin/bash

Go the to the working directory

    cd /opt/extService/

Verify that the kafka configuration is correct

    cat kafka.properties 
    
    kafka.bootstrap.servers=nuxeo-kafka.nx-shared-storage.svc.cluster.local:9092

Fetch message from Nuxeo:

    ./getRequestFromNuxeo.sh -p nuxeo-company-g

    {"command":"doSomething","success":false,"sessionId":"7edd91a9-7c12-416c-a7f3-5201dfc2495e","parameters":{"docId":"9897f584-1e65-472e-847f-acc975d0ad89"}}
    
Generate a response message:

    cat <<EOF >> response.json
    {
        "command":"updateDoc",
        "success":true,
        "sessionId":"14101ad9-fcc4-4d61-8602-cdd75907c871",
        "parameters":
            {
            "docId":"9897f584-1e65-472e-847f-acc975d0ad89",
            "repository":"default",            
            "dc:description":"Hello from external service!" 
            }
    }
    EOF

Send the reply to Nuxeo:

    ./sendResponse.sh -p nuxeo-company-g -m response.json

    Message appended to offset externalservice-response-00:+0

Check if Nuxeo has done something:

    curl -X GET -H "X-NXproperties: dublincore" -u Administrator:Administrator  https://company-g.multitenant.nuxeo.com/nuxeo/api/v1/path/default-domain

    {
     "entity-type":"document",
     "repository":"default",
     "uid":"9897f584-1e65-472e-847f-acc975d0ad89",
     "path":"/default-domain",
     "type":"Domain",
     ...
     "lastModified":"2021-02-19T03:49:00.898Z",
     "properties":{
       "dc:description":"Hello from external service!",
     ...

