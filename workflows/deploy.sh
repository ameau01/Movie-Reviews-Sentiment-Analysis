#!/usr/bin/env bash
set -euo pipefail

VM_NAME="sentiment-vm"
ZONE="us-west2-c"
PROJECT_DIR="/home/ameau/csca-5028-sentiment-analysis"

echo "Building all JARs..."
./gradlew clean bootJar -x test

echo "Deploying all 3 services to $VM_NAME..."

gcloud compute scp --quiet --zone="$ZONE" \
  applications/frontend-server/build/libs/frontend-server-*.jar \
  "$VM_NAME:$PROJECT_DIR/applications/frontend-server/build/libs/app.jar"

gcloud compute scp --quiet --zone="$ZONE" \
  applications/data-analyzer-server/build/libs/data-analyzer-server-*.jar \
  "$VM_NAME:$PROJECT_DIR/applications/data-analyzer-server/build/libs/app.jar"

gcloud compute scp --quiet --zone="$ZONE" \
  applications/data-collector-server/build/libs/data-collector-server-*.jar \
  "$VM_NAME:$PROJECT_DIR/applications/data-collector-server/build/libs/app.jar"

echo "Restarting containers..."
gcloud compute ssh "$VM_NAME" --zone="$ZONE" --quiet --command="cd $PROJECT_DIR && docker compose restart frontend-server data-analyzer data-collector"

echo "All services deployed successfully!"
