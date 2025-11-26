#!/bin/bash

docker exec -i mongodb mongosh -u root -p password \
  --authenticationDatabase admin \
  admin --eval 'db.getSiblingDB("sentiment_db").api_data.countDocuments()' --quiet
