variable "project_name" {
  description = "Project name"
  type        = string
  default     = "franchises-api"
}

variable "environment" {
  description = "Deployment environment (dev, staging, prod)"
  type        = string
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging or prod."
  }
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "franchises_admin"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "franchises_db"
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones for subnets"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "free_tier" {
  description = "Set true en cuentas AWS Free Tier — deshabilita backups, encryption y auto scaling"
  type        = bool
  default     = false
}
