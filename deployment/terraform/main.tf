module "networking" {
  source = "./modules/networking"

  project_name       = var.project_name
  environment        = var.environment
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
}

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
