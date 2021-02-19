#!/bin/bash

cd ..

mvn -o -DskipTests clean install

cp nuxeo-external-service-cli/target/nuxeo-external-service-cli-1.0-SNAPSHOT.jar externalServicePod/bin/.
cp nuxeo-external-service-cli/scripts/* externalServicePod/bin/.

cd externalServicePod

docker build  -t nuxeo-test/stream-client -f Dockerfile .

IMAGEID=$(docker images -q nuxeo-test/stream-client)

docker tag $IMAGEID gcr.io/jx-preprod/stream-client
docker push gcr.io/jx-preprod/stream-client

