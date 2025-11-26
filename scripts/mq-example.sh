#!/bin/bash

docker exec rabbitmq rabbitmqadmin --username=root --password=password get queue=movie_reviews_queue count=1 -f raw_json | jq -r '.[] | .payload' | jq .
