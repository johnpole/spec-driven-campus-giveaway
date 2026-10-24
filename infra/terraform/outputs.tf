output "cloud_run_url" {
  description = "The public HTTPS URL of the deployed Cloud Run service"
  value       = google_cloud_run_v2_service.app.uri
}

output "cloud_sql_connection_name" {
  description = "Connection name for the Cloud SQL PostgreSQL instance"
  value       = google_sql_database_instance.db.connection_name
}

output "gcs_photo_bucket" {
  description = "Name of the Google Cloud Storage bucket for item photos"
  value       = google_storage_bucket.photos.name
}

output "artifact_registry_repo" {
  description = "Docker repository for container builds"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.repo.repository_id}"
}

output "service_account_email" {
  description = "The service account email running Cloud Run"
  value       = google_service_account.run_sa.email
}
