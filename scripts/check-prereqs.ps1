Write-Host "--- Checking Android Development Prerequisites ---" -ForegroundColor Cyan

# Check JDK
if (Get-Command java -ErrorAction SilentlyContinue) {
    Write-Host "[OK] Java JDK found." -ForegroundColor Green
} else {
    Write-Host "[ERROR] Java JDK not found in PATH." -ForegroundColor Red
}

# Check adb
if (Get-Command adb -ErrorAction SilentlyContinue) {
    Write-Host "[OK] adb found." -ForegroundColor Green
} else {
    Write-Host "[ERROR] adb not found. Ensure Android SDK platform-tools is in PATH." -ForegroundColor Red
}

# Check emulator
if (Get-Command emulator -ErrorAction SilentlyContinue) {
    Write-Host "[OK] Android Emulator tool found." -ForegroundColor Green
} else {
    Write-Host "[ERROR] emulator tool not found." -ForegroundColor Red
}