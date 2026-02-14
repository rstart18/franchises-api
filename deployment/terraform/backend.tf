# Remote state — NEVER commit the .tfstate to the repository.
# Uncomment and configure once the S3 bucket and DynamoDB table are provisioned.
#
# terraform {
#   backend "s3" {
#     bucket         = "franchises-api-terraform-state"
#     key            = "franchises-api/terraform.tfstate"
#     region         = "us-east-1"
#     encrypt        = true
#     dynamodb_table = "terraform-locks"
#   }
# }
