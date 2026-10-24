terraform {
  required_version = ">= 1.5.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

# -----------------------------------------------------------------------------
# GCP Services / APIs
# -----------------------------------------------------------------------------
locals {
  apis = [
    "run.googleapis.com",
    "sqladmin.googleapis.com",
    "storage.googleapis.com",
    "secretmanager.googleapis.com",
    "artifactregistry.googleapis.com",
    "cloudbuild.googleapis.com",
    "iam.googleapis.com"
  ]
}

resource "google_project_service" "enabled_apis" {
  for_each           = toset(local.apis)
  project            = var.project_id
  service            = each.key
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Artifact Registry for Container Images
# -----------------------------------------------------------------------------
resource "google_artifact_registry_repository" "repo" {
  depends_on    = [google_project_service.enabled_apis]
  location      = var.region
  repository_id = var.app_name
  description   = "Docker repository for Campus Giveaway"
  format        = "DOCKER"
}

# -----------------------------------------------------------------------------
# Cloud Storage Bucket for Media / Photos
# -----------------------------------------------------------------------------
resource "google_storage_bucket" "photos" {
  depends_on                  = [google_project_service.enabled_apis]
  name                        = "${var.project_id}-campusgiveaway-photos"
  location                    = var.region
  uniform_bucket_level_access = true
  force_destroy               = true

  cors {
    origin          = ["*"]
    method          = ["GET", "HEAD"]
    response_header = ["*"]
    max_age_seconds = 3600
  }
}

# -----------------------------------------------------------------------------
# Cloud SQL: PostgreSQL 17
# -----------------------------------------------------------------------------
resource "random_password" "db_password" {
  length  = 24
  special = false
}

resource "google_secret_manager_secret" "db_password" {
  depends_on = [google_project_service.enabled_apis]
  secret_id  = "giveaway-db-password"

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "db_password_version" {
  secret      = google_secret_manager_secret.db_password.id
  secret_data = random_password.db_password.result
}

resource "google_sql_database_instance" "db" {
  depends_on          = [google_project_service.enabled_apis]
  name                = var.db_instance_name
  database_version    = "POSTGRES_17"
  region              = var.region
  deletion_protection = false

  settings {
    tier              = "db-f1-micro"
    activation_policy = var.activation_policy

    ip_configuration {
      ipv4_enabled = true
    }

    backup_configuration {
      enabled    = false
      start_time = "04:00"
    }
  }
}

resource "google_sql_database" "database" {
  name     = var.db_name
  instance = google_sql_database_instance.db.name
}

resource "google_sql_user" "app_user" {
  name     = var.db_user
  instance = google_sql_database_instance.db.name
  password = random_password.db_password.result
}

# -----------------------------------------------------------------------------
# Service Account & Least-Privilege IAM
# -----------------------------------------------------------------------------
resource "google_service_account" "run_sa" {
  depends_on   = [google_project_service.enabled_apis]
  account_id   = "giveaway-run"
  display_name = "Campus Giveaway Cloud Run Service Account"
}

resource "google_project_iam_member" "cloudsql_client" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.run_sa.email}"
}

resource "google_storage_bucket_iam_member" "storage_admin" {
  bucket = google_storage_bucket.photos.name
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${google_service_account.run_sa.email}"
}

resource "google_secret_manager_secret_iam_member" "secret_accessor" {
  secret_id = google_secret_manager_secret.db_password.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.run_sa.email}"
}

# -----------------------------------------------------------------------------
# Cloud Run: Single Container Application
# -----------------------------------------------------------------------------
resource "google_cloud_run_v2_service" "app" {
  depends_on = [
    google_project_service.enabled_apis,
    google_sql_database_instance.db,
    google_sql_database.database,
    google_sql_user.app_user
  ]
  name     = var.app_name
  location = var.region
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    service_account = google_service_account.run_sa.email

    scaling {
      min_instance_count = 0
      max_instance_count = 5
    }

    volumes {
      name = "cloudsql"
      cloud_sql_instance {
        instances = [google_sql_database_instance.db.connection_name]
      }
    }

    containers {
      image = var.container_image

      resources {
        limits = {
          cpu    = "1"
          memory = "1Gi"
        }
      }

      ports {
        container_port = 8080
      }

      volume_mounts {
        name       = "cloudsql"
        mount_path = "/cloudsql"
      }

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "prod"
      }
      env {
        name  = "GCP_PROJECT"
        value = var.project_id
      }
      env {
        name  = "CLOUD_SQL_CONNECTION_NAME"
        value = google_sql_database_instance.db.connection_name
      }
      env {
        name  = "DB_NAME"
        value = var.db_name
      }
      env {
        name  = "DB_USER"
        value = var.db_user
      }
      env {
        name  = "PHOTO_BUCKET"
        value = google_storage_bucket.photos.name
      }
      env {
        name = "DB_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }
    }
  }
}

# Allow unauthenticated public traffic for campus demo
resource "google_cloud_run_v2_service_iam_member" "public_access" {
  name     = google_cloud_run_v2_service.app.name
  location = google_cloud_run_v2_service.app.location
  role     = "roles/run.invoker"
  member   = "allUsers"
}
