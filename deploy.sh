#!/usr/bin/env bash
# ==============================================================================
# deploy.sh — Google Cloud Run Deployer for Campus Giveaway
# Project: gdg-hudson | Region: us-east1 (strict per AGENTS.md)
# ==============================================================================

set -euo pipefail

# Configuration Defaults
PROJECT_ID="${GCP_PROJECT_ID:-gdg-hudson}"
REGION="${GCP_REGION:-us-east1}"
SERVICE_NAME="${SERVICE_NAME:-campus-giveaway}"
DB_INSTANCE_NAME="${DB_INSTANCE_NAME:-giveaway-db}"
DB_NAME="${DB_NAME:-giveaway}"
DB_USER="${DB_USER:-giveaway_app}"
SECRET_NAME="${SECRET_NAME:-giveaway-db-password}"

# Parse optional command line flags
while [[ $# -gt 0 ]]; do
  case "$1" in
    --project)
      PROJECT_ID="$2"
      shift 2
      ;;
    --region)
      REGION="$2"
      shift 2
      ;;
    --service)
      SERVICE_NAME="$2"
      shift 2
      ;;
    *)
      echo "Unknown flag: $1"
      echo "Usage: ./deploy.sh [--project <id>] [--region <region>] [--service <name>]"
      exit 1
      ;;
  esac
done

PHOTO_BUCKET="${PROJECT_ID}-campusgiveaway-photos"
SA_EMAIL="giveaway-run@${PROJECT_ID}.iam.gserviceaccount.com"
CLOUD_SQL_CONNECTION="${PROJECT_ID}:${REGION}:${DB_INSTANCE_NAME}"
IMAGE_URI="${REGION}-docker.pkg.dev/${PROJECT_ID}/${SERVICE_NAME}/app:latest"

echo "=============================================================================="
echo " Campus Giveaway — Google Cloud Run Deployer"
echo " Project ID:             ${PROJECT_ID}"
echo " Region:                 ${REGION}"
echo " Service Name:           ${SERVICE_NAME}"
echo " Cloud SQL Connection:   ${CLOUD_SQL_CONNECTION}"
echo " Photo Bucket:           ${PHOTO_BUCKET}"
echo " Target Image:           ${IMAGE_URI}"
echo "=============================================================================="

# Ensure active authenticated gcloud session
ACTIVE_ACCOUNT=$(gcloud auth list --filter=status:ACTIVE --format="value(account)" 2>/dev/null || echo "")
if [ -z "${ACTIVE_ACCOUNT}" ]; then
  echo "Error: No active gcloud account detected. Please run: gcloud auth login"
  exit 1
fi
echo "✓ Authenticated as: ${ACTIVE_ACCOUNT}"

echo ""
echo "Step 1: Enabling required Google Cloud APIs..."
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  storage.googleapis.com \
  secretmanager.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com \
  --project="${PROJECT_ID}"

echo ""
echo "Step 2: Ensuring Artifact Registry repository exists..."
if ! gcloud artifacts repositories describe "${SERVICE_NAME}" --location="${REGION}" --project="${PROJECT_ID}" >/dev/null 2>&1; then
  echo "Creating Artifact Registry docker repository: ${SERVICE_NAME} in ${REGION}..."
  gcloud artifacts repositories create "${SERVICE_NAME}" \
    --repository-format=docker \
    --location="${REGION}" \
    --description="Docker repository for Campus Giveaway" \
    --project="${PROJECT_ID}"
fi

echo ""
echo "Step 3: Building unified container image via Google Cloud Build..."
gcloud builds submit \
  --tag="${IMAGE_URI}" \
  --project="${PROJECT_ID}" \
  .

echo ""
echo "Step 4: Deploying container to Google Cloud Run..."
gcloud run deploy "${SERVICE_NAME}" \
  --image="${IMAGE_URI}" \
  --region="${REGION}" \
  --platform="managed" \
  --allow-unauthenticated \
  --service-account="${SA_EMAIL}" \
  --add-cloudsql-instances="${CLOUD_SQL_CONNECTION}" \
  --memory="1Gi" \
  --cpu="1" \
  --min-instances="0" \
  --max-instances="5" \
  --port="8080" \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,GCP_PROJECT=${PROJECT_ID},CLOUD_SQL_CONNECTION_NAME=${CLOUD_SQL_CONNECTION},DB_NAME=${DB_NAME},DB_USER=${DB_USER},PHOTO_BUCKET=${PHOTO_BUCKET}" \
  --set-secrets="DB_PASSWORD=${SECRET_NAME}:latest" \
  --project="${PROJECT_ID}"

echo ""
echo "=============================================================================="
SERVICE_URL=$(gcloud run services describe "${SERVICE_NAME}" --region="${REGION}" --project="${PROJECT_ID}" --format='value(status.url)')
echo " Deployment Complete!"
echo " Live Campus Giveaway URL: ${SERVICE_URL}"
echo " Actuator Health Endpoint: ${SERVICE_URL}/actuator/health"
echo "=============================================================================="
