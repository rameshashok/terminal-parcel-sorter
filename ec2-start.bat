@echo off
echo Starting EC2 instance...
powershell.exe -Command "
$result = aws ec2 start-instances --instance-ids i-0625ff1b51a5dffef --output json | ConvertFrom-Json
$state = $result.StartingInstances[0].CurrentState.Name
Write-Host \"Instance state: $state\"
Write-Host \"Waiting for instance and containers to be ready (~60 seconds)...\"
Start-Sleep -Seconds 60
Write-Host \"\"
Write-Host \"App      : https://3-225-155-252.sslip.io\"
Write-Host \"Backend  : http://3.225.155.252:8081\"
Write-Host \"SSH      : ssh -i C:\Users\conta\.ssh\id_rsa ec2-user@3.225.155.252\"
Write-Host \"\"
Write-Host \"Containers start automatically on boot (restart: unless-stopped).\"
"
pause
