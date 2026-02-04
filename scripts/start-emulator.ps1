$avds = emulator -list-avds
if ($null -eq $avds -or $avds.Count -eq 0) {
    Write-Host "No Android Virtual Devices (AVDs) found. Create one in Android Studio Device Manager." -ForegroundColor Yellow
} else {
    Write-Host "Available Emulators:" -ForegroundColor Cyan
    $avds
    Write-Host "`nStarting first available emulator: $($avds[0])..." -ForegroundColor Green
    Start-Process -NoNewWindow -FilePath "emulator" -ArgumentList "-avd $($avds[0])"
}