variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "db_username" {
  description = "Database username"
  type        = string
}

variable "db_name" {
  description = "Database name"
  type        = string
}

variable "db_host" {
  description = "Database host (RDS endpoint without port)"
  type        = string
}

variable "db_port" {
  description = "Database port"
  type        = string
  default     = "5432"
}
