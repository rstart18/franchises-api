# ── SSM Parameter Store — Secretos y configuración de base de datos ──────────

resource "aws_ssm_parameter" "db_password" {
  name        = "/${var.project_name}/${var.environment}/DB_PASSWORD"
  description = "Database password for ${var.project_name} ${var.environment}"
  type        = "SecureString"
  value       = var.db_password

  tags = {
    Name = "${var.project_name}-${var.environment}-db-password"
  }
}

resource "aws_ssm_parameter" "db_username" {
  name        = "/${var.project_name}/${var.environment}/DB_USER"
  description = "Database username for ${var.project_name} ${var.environment}"
  type        = "String"
  value       = var.db_username

  tags = {
    Name = "${var.project_name}-${var.environment}-db-username"
  }
}

resource "aws_ssm_parameter" "db_name" {
  name        = "/${var.project_name}/${var.environment}/DB_NAME"
  description = "Database name for ${var.project_name} ${var.environment}"
  type        = "String"
  value       = var.db_name

  tags = {
    Name = "${var.project_name}-${var.environment}-db-name"
  }
}

resource "aws_ssm_parameter" "db_host" {
  name        = "/${var.project_name}/${var.environment}/DB_HOST"
  description = "Database host for ${var.project_name} ${var.environment}"
  type        = "String"
  value       = var.db_host

  tags = {
    Name = "${var.project_name}-${var.environment}-db-host"
  }
}

resource "aws_ssm_parameter" "db_port" {
  name        = "/${var.project_name}/${var.environment}/DB_PORT"
  description = "Database port for ${var.project_name} ${var.environment}"
  type        = "String"
  value       = var.db_port

  tags = {
    Name = "${var.project_name}-${var.environment}-db-port"
  }
}
