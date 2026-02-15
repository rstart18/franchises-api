variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "public_subnet_ids" {
  description = "List of public subnet IDs for VPC Link"
  type        = list(string)
}

variable "alb_listener_arn" {
  description = "ALB HTTP listener ARN"
  type        = string
}

variable "alb_security_group_id" {
  description = "ALB security group ID (kept for reference)"
  type        = string
}

variable "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  type        = string
}