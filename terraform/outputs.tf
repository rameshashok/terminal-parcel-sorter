output "instance_public_ip" {
  description = "Elastic IP of the EC2 instance"
  value       = aws_eip.app.public_ip
}

output "backend_url" {
  description = "Quarkus backend URL"
  value       = "http://${aws_eip.app.public_ip}:8081"
}

output "frontend_url" {
  description = "Angular frontend URL"
  value       = "http://${aws_eip.app.public_ip}"
}

output "ssh_command" {
  description = "SSH command to connect to the instance"
  value       = "ssh -i ~/.ssh/id_rsa ec2-user@${aws_eip.app.public_ip}"
}
