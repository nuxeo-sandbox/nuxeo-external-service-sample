#!/bin/bash

tenant=$1

echo -e "apiVersion: v1\nkind: Namespace\nmetadata:\n  name: ${tenant}" | kubectl apply -f -


kubectl -n $tenant create deployment --image=gcr.io/jx-preprod/stream-client stream-client


