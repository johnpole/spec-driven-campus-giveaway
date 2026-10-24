#!/usr/bin/env bash
# ==============================================================================
# infra/stop.sh — Post-Event Resource Teardown and Cost Halter
# Project: gdg-hudson | Region: us-east1
#
# Use this script 2 days after the GDG talk to stop all billable compute!
# ==============================================================================

set -euo pipefail

PROJECT_ID="${GCP_PROJECT_ID:-gdg-hudson}"
REGION="${GCP_REGION:-us-east1}"
SERVICE_NAME="${SERVICE_NAME:-campus-giveaway}"
DB_INSTANCE_NAME="${DB_INSTANCE_NAME:-giveaway-db}"

ACTION="pause"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --pause)
      ACTION="pause"
      shift
      ;;
    --resume)
      ACTION="resume"
      shift
      ;;
    --destroy)
      ACTION="destroy"
      shift
      ;;
    --project)
      PROJECT_ID="$2"
      shift 2
      ;;
    *)
      echo "Unknown flag: $1"
      echo "Usage: ./infra/stop.sh [--pause | --resume | --destroy] [--project <id>]"
      exit 1
      ;;
  esac
done

echo "=============================================================================="
echo " Campus Giveaway — Post-Event Resource Manager"
echo " Project:       ${PROJECT_ID}"
echo " Region:        ${REGION}"
echo " Action:        ${ACTION}"
echo "=============================================================================="

if [ "${ACTION}" = "pause" ]; then
  echo ""
  echo ">>> Halting billable compute for Cloud SQL (${DB_INSTANCE_NAME})..."
  echo "    Setting activation-policy to NEVER. Disk and data are preserved."
  gcloud sql instances patch "${DB_INSTANCE_NAME}" \
    --activation-policy=NEVER \
    --project="${PROJECT_ID}" \
    --quiet || echo "Warning: Cloud SQL instance could not be patched or does not exist."

  echo ""
  echo ">>> Scaling Cloud Run service (${SERVICE_NAME}) to 0 instances..."
  gcloud run services update "${SERVICE_NAME}" \
    --region="${REGION}" \
    --min-instances=0 \
    --max-instances=0 \
    --project="${PROJECT_ID}" \
    --quiet || echo "Warning: Cloud Run service could not be updated or does not exist."

  echo ""
  echo "=============================================================================="
  echo " Compute Successfully Stopped!"
  echo " - Cloud SQL compute billing: PAUSED ($0.00/hr compute)"
  echo " - Cloud Run instances: 0 (No CPU/RAM charges)"
  echo " - Data preserved in Cloud SQL disk and GCS bucket."
  echo ""
  echo " To resume the application later, run:"
  echo "   ./infra/stop.sh --resume"
  echo ""
  echo " To permanently delete all cloud resources, run:"
  echo "   ./infra/stop.sh --destroy"
  echo "=============================================================================="

elif [ "${ACTION}" = "resume" ]; then
  echo ""
  echo ">>> Resuming Cloud SQL instance (${DB_INSTANCE_NAME})..."
  gcloud sql instances patch "${DB_INSTANCE_NAME}" \
    --activation-policy=ALWAYS \
    --project="${PROJECT_ID}" \
    --quiet

  echo ""
  echo ">>> Resuming Cloud Run service (${SERVICE_NAME}) to max 5 instances..."
  gcloud run services update "${SERVICE_NAME}" \
    --region="${REGION}" \
    --min-instances=0 \
    --max-instances=5 \
    --project="${PROJECT_ID}" \
    --quiet

  echo ""
  echo "=============================================================================="
  echo " Application Successfully Resumed!"
  SERVICE_URL=$(gcloud run services describe "${SERVICE_NAME}" --region="${REGION}" --project="${PROJECT_ID}" --format='value(status.url)' 2>/dev/null || echo "https://<service-url>")
  echo " Live URL: ${SERVICE_URL}"
  echo "=============================================================================="

elif [ "${ACTION}" = "destroy" ]; then
  echo ""
  echo ">>> Permanent Teardown: Running terraform destroy..."
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  cd "${SCRIPT_DIR}/terraform"

  /opt/homebrew/bin/terraform destroy \
    -var="project_id=${PROJECT_ID}" \
    -auto-approve

  echo ""
  echo "=============================================================================="
  echo " All Google Cloud resources permanently deleted."
  echo "=============================================================================="
fi
