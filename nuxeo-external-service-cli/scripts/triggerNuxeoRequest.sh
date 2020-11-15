#!/bin/bash

curl -H 'Content-Type:application/json+nxrequest' -X POST \
 -d '{"params":{"serviceName":"externalservice", "command":"doSomething", "parameters" : "docId=xxx" },"context":{}}' \
 -u Administrator:Administrator \
 http://127.0.0.1:8080/nuxeo/api/v1/automation/Service.External

 