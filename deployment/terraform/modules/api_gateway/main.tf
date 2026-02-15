# ── API Gateway HTTP — Punto de entrada público ──────────────────────────────

# ── Security Group para VPC Link ─────────────────────────────────────────────
resource "aws_security_group" "vpc_link" {
  name        = "${var.project_name}-${var.environment}-vpclink-sg"
  description = "Security group for API Gateway VPC Link"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Allow responses from ALB"
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    security_groups = [var.alb_security_group_id]
  }

  egress {
    description = "Allow all outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-vpclink-sg"
  }
}

# ── VPC Link — conecta API Gateway con la VPC ───────────────────────────────
resource "aws_apigatewayv2_vpc_link" "main" {
  name               = "${var.project_name}-${var.environment}-vpc-link"
  subnet_ids         = var.public_subnet_ids
  security_group_ids = [aws_security_group.vpc_link.id]

  tags = {
    Name = "${var.project_name}-${var.environment}-vpc-link"
  }
}

# ── HTTP API ─────────────────────────────────────────────────────────────────
resource "aws_apigatewayv2_api" "main" {
  name          = "${var.project_name}-${var.environment}-api"
  protocol_type = "HTTP"

  tags = {
    Name = "${var.project_name}-${var.environment}-api"
  }
}

# ── Integración con ALB via VPC Link ────────────────────────────────────────
resource "aws_apigatewayv2_integration" "alb" {
  api_id             = aws_apigatewayv2_api.main.id
  integration_type   = "HTTP_PROXY"
  integration_uri    = var.alb_listener_arn
  integration_method = "ANY"
  connection_type    = "VPC_LINK"
  connection_id      = aws_apigatewayv2_vpc_link.main.id
}

# ── Ruta catch-all: ANY /{proxy+} ───────────────────────────────────────────
resource "aws_apigatewayv2_route" "proxy" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "ANY /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.alb.id}"
}

# ── Ruta raíz: ANY / ────────────────────────────────────────────────────────
resource "aws_apigatewayv2_route" "root" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "ANY /"
  target    = "integrations/${aws_apigatewayv2_integration.alb.id}"
}

# ── Stage $default con auto-deploy ──────────────────────────────────────────
resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.main.id
  name        = "$default"
  auto_deploy = true

  tags = {
    Name = "${var.project_name}-${var.environment}-stage"
  }
}
