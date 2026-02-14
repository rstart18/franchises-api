output "db_endpoint" {
  description = "RDS instance endpoint"
  value       = module.database.db_endpoint
}

output "db_port" {
  description = "RDS instance port"
  value       = module.database.db_port
}

output "db_name" {
  description = "Database name"
  value       = var.db_name
}

output "vpc_id" {
  description = "VPC ID"
  value       = module.networking.vpc_id
}
