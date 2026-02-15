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

# ── ECR ──────────────────────────────────────────────────────────────────────

output "ecr_repository_url" {
  description = "ECR repository URL (for docker push)"
  value       = module.ecr.repository_url
}

# ── ALB ──────────────────────────────────────────────────────────────────────

output "alb_dns_name" {
  description = "ALB DNS name"
  value       = module.alb.alb_dns_name
}

# ── ECS ──────────────────────────────────────────────────────────────────────

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "ECS service name"
  value       = module.ecs.service_name
}

# ── API Gateway ──────────────────────────────────────────────────────────────

output "api_gateway_url" {
  description = "API Gateway public URL"
  value       = module.api_gateway.api_gateway_url
}
