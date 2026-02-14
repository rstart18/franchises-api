output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "db_subnet_group_name" {
  description = "DB subnet group name"
  value       = aws_db_subnet_group.main.name
}

output "db_security_group_id" {
  description = "Security group ID for RDS"
  value       = aws_security_group.db.id
}
