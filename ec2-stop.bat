@echo off
echo Stopping EC2 instance...
C:\Users\conta\terraform-bin\terraform.exe -chdir="%~dp0terraform" output -raw instance_public_ip > nul 2>&1
powershell.exe -Command "
$result = aws ec2 stop-instances --instance-ids i-0625ff1b51a5dffef --output json | ConvertFrom-Json
$state = $result.StoppingInstances[0].CurrentState.Name
Write-Host \"Instance state: $state\"
Write-Host \"EC2 is stopping. Compute charges will stop shortly.\"
Write-Host \"Your data and IP (3.225.155.252) are preserved.\"
"
pause
