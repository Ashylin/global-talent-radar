$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    Push-Location frontend
    try {
        npm ci --no-fund --no-audit
        if ($LASTEXITCODE -ne 0) { throw 'Frontend installation failed' }
        npm run build
        if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed' }
    } finally { Pop-Location }
    .\gradlew.bat test bootJar
    if ($LASTEXITCODE -ne 0) { throw 'Backend build or tests failed' }
    Write-Output 'Run: java -jar build/libs/global-talent-radar.jar'
    Write-Output 'Open: http://localhost:8080/app'
} finally { Pop-Location }
