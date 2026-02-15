output "api_gateway_url" {
  description = "API Gateway invoke URL"
  value       = aws_apigatewayv2_stage.default.invoke_url
}

output "api_id" {
  description = "API Gateway ID"
  value       = aws_apigatewayv2_api.main.id
}

output "vpc_link_security_group_id" {
  description = "VPC Link security group ID"
  value       = aws_security_group.vpc_link.id
}

