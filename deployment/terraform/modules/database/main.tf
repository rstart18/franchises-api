resource "aws_db_instance" "main" {
  identifier        = "${var.project_name}-${var.environment}-db"
  engine            = "postgres"
  engine_version    = "15.16"
  instance_class    = var.db_instance_class
  db_name           = var.db_name
  username          = var.db_username
  password          = var.db_password

  # R2DBC requires PostgreSQL — port 5432
  port                = 5432
  publicly_accessible = true

  vpc_security_group_ids = [var.db_security_group_id]
  db_subnet_group_name   = var.db_subnet_group_name

  allocated_storage     = 20
  max_allocated_storage = var.free_tier ? 20 : 100  # Free Tier no permite auto scaling
  storage_type          = "gp2"                      # Free Tier solo soporta gp2
  storage_encrypted     = var.free_tier ? false : true

  # Free Tier no permite backup_retention_period > 0
  backup_retention_period = var.free_tier ? 0 : 7
  backup_window           = var.free_tier ? null : "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  deletion_protection = var.environment == "prod" ? true : false
  skip_final_snapshot = var.environment != "prod"

  tags = {
    Name = "${var.project_name}-${var.environment}-db"
  }
}
