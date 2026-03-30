@echo off
echo Starting EC2 instance...
powershell.exe -Command "
$result = aws ec2 start-instances --instance-ids i-0ecb10a2f23c955fa --output json | ConvertFrom-Json
$state = $result.StartingInstances[0].CurrentState.Name
Write-Host \"Instance state: $state\"
Write-Host \"Waiting for instance to be reachable (about 30 seconds)...\"
Start-Sleep -Seconds 35
Write-Host \"\"
Write-Host \"Frontend : http://3.225.155.252\"
Write-Host \"Backend  : http://3.225.155.252:8081\"
Write-Host \"SSH      : ssh -i C:\Users\conta\.ssh\id_rsa ec2-user@3.225.155.252\"
Write-Host \"\"
Write-Host \"Containers start automatically via systemd.\"
"
pause
