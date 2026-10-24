variable "project_id" {
  description = "The Google Cloud Project ID"
  type        = string
  default     = "gdg-hudson"
}

variable "region" {
  description = "The Google Cloud Region for compute and storage"
  type        = string
  default     = "us-east1"
}

variable "app_name" {
  description = "Application base name"
  type        = string
  default     = "campus-giveaway"
}

variable "db_instance_name" {
  description = "Cloud SQL PostgreSQL 17 instance name"
  type        = string
  default     = "giveaway-db"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "giveaway"
}

variable "db_user" {
  description = "Database user for the application"
  type        = string
  default     = "giveaway_app"
}

variable "activation_policy" {
  description = "Activation policy for Cloud SQL: ALWAYS (running) or NEVER (paused)"
  type        = string
  default     = "ALWAYS"
}

variable "container_image" {
  description = "Container image URL to deploy on Cloud Run"
  type        = string
  default     = "us-east1-docker.pkg.dev/gdg-hudson/campus-giveaway/app:latest"
}
