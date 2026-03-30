variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Prefix for all named resources"
  type        = string
  default     = "nps-agent"
}

variable "instance_type" {
  description = "EC2 instance type (t2.micro is free tier eligible)"
  type        = string
  default     = "t2.micro"
}

variable "public_key_path" {
  description = "Path to your SSH public key file"
  type        = string
  default     = "~/.ssh/id_rsa.pub"
}

variable "allowed_ssh_cidr" {
  description = "CIDR allowed to SSH into the instance (restrict to your IP)"
  type        = string
  default     = "0.0.0.0/0"
}

variable "openrouter_api_key" {
  description = "OpenRouter API key for the Quarkus backend"
  type        = string
  sensitive   = true
}

variable "openrouter_model" {
  description = "OpenRouter model ID"
  type        = string
  default     = "google/gemma-3-4b-it:free"
}
