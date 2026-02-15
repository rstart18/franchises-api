# ═══════════════════════════════════════════════════════════════════════════════
# Networking — VPC, Subnets, Internet Gateway, Security Groups
# ═══════════════════════════════════════════════════════════════════════════════

module "networking" {
  source = "./modules/networking"

  project_name       = var.project_name
  environment        = var.environment
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
}

# ═══════════════════════════════════════════════════════════════════════════════
# Database — RDS PostgreSQL
# ═══════════════════════════════════════════════════════════════════════════════

module "database" {
  source = "./modules/database"

  project_name      = var.project_name
  environment       = var.environment
  db_name           = var.db_name
  db_username       = var.db_username
  db_password       = var.db_password
  db_instance_class = var.db_instance_class

  vpc_id               = module.networking.vpc_id
  db_subnet_group_name = module.networking.db_subnet_group_name
  db_security_group_id = module.networking.db_security_group_id
  free_tier            = var.free_tier
}

# ═══════════════════════════════════════════════════════════════════════════════
# Secrets — SSM Parameter Store
# ═══════════════════════════════════════════════════════════════════════════════

module "secrets" {
  source = "./modules/secrets"

  project_name = var.project_name
  environment  = var.environment
  db_password  = var.db_password
  db_username  = var.db_username
  db_name      = var.db_name
  db_host      = module.database.db_host
  db_port      = tostring(module.database.db_port)
}

# ═══════════════════════════════════════════════════════════════════════════════
# ECR — Docker Image Repository
# ═══════════════════════════════════════════════════════════════════════════════

module "ecr" {
  source = "./modules/ecr"

  project_name = var.project_name
  environment  = var.environment
}

# ═══════════════════════════════════════════════════════════════════════════════
# ALB — Application Load Balancer
# ═══════════════════════════════════════════════════════════════════════════════

module "alb" {
  source = "./modules/alb"

  project_name      = var.project_name
  environment       = var.environment
  vpc_id            = module.networking.vpc_id
  public_subnet_ids = module.networking.public_subnet_ids
}

# ═══════════════════════════════════════════════════════════════════════════════
# ECS — Fargate Cluster + Service + Task Definition
# ═══════════════════════════════════════════════════════════════════════════════

module "ecs" {
  source = "./modules/ecs"

  project_name      = var.project_name
  environment       = var.environment
  aws_region        = var.aws_region
  vpc_id            = module.networking.vpc_id
  public_subnet_ids = module.networking.public_subnet_ids

  ecr_repository_url    = module.ecr.repository_url
  alb_target_group_arn  = module.alb.target_group_arn
  alb_security_group_id = module.alb.security_group_id
  alb_listener_arn      = module.alb.listener_arn

  db_host = module.database.db_host
  db_port = tostring(module.database.db_port)
  db_name = var.db_name

  ssm_db_password_arn = module.secrets.db_password_arn
  ssm_db_username_arn = module.secrets.db_username_arn
  ssm_parameter_arns = [
    module.secrets.db_password_arn,
    module.secrets.db_username_arn,
    module.secrets.db_name_arn,
    module.secrets.db_host_arn,
    module.secrets.db_port_arn
  ]

  container_cpu    = var.container_cpu
  container_memory = var.container_memory
  desired_count    = var.app_desired_count
}

# ═══════════════════════════════════════════════════════════════════════════════
# API Gateway — HTTP API (punto de entrada público)
# ═══════════════════════════════════════════════════════════════════════════════

module "api_gateway" {
  source = "./modules/api_gateway"

  project_name          = var.project_name
  environment           = var.environment
  vpc_id                = module.networking.vpc_id
  public_subnet_ids     = module.networking.public_subnet_ids
  alb_listener_arn      = module.alb.listener_arn
  alb_dns_name          = module.alb.alb_dns_name
  alb_security_group_id = module.alb.security_group_id
}

# ═══════════════════════════════════════════════════════════════════════════════
# Security Group Rule — permite tráfico del VPC Link hacia el ALB (puerto 80)
# ═══════════════════════════════════════════════════════════════════════════════

resource "aws_security_group_rule" "alb_from_vpc_link" {
  type                     = "ingress"
  from_port                = 80
  to_port                  = 80
  protocol                 = "tcp"
  security_group_id        = module.alb.security_group_id
  source_security_group_id = module.api_gateway.vpc_link_security_group_id
  description              = "Allow traffic from API Gateway VPC Link"
}
