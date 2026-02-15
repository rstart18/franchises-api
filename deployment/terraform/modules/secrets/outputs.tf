output "db_password_arn" {
  description = "ARN of the DB_PASSWORD SSM parameter"
  value       = aws_ssm_parameter.db_password.arn
}

output "db_username_arn" {
  description = "ARN of the DB_USER SSM parameter"
  value       = aws_ssm_parameter.db_username.arn
}

output "db_name_arn" {
  description = "ARN of the DB_NAME SSM parameter"
  value       = aws_ssm_parameter.db_name.arn
}

output "db_host_arn" {
  description = "ARN of the DB_HOST SSM parameter"
  value       = aws_ssm_parameter.db_host.arn
}

output "db_port_arn" {
  description = "ARN of the DB_PORT SSM parameter"
  value       = aws_ssm_parameter.db_port.arn
}
