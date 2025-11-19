#!/bin/bash

curl -X POST http://localhost:8881/analyze -H "Content-Type: application/json" -d '{"text": "I hated this terrible movie"}'
