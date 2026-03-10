param(
    [switch]$SkipBackendBuild,
    [switch]$SkipFrontendBuild
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $ScriptDir)
$ReleaseDir = Join-Path $ProjectRoot 'release\student-management'
$BackendDir = Join-Path $ProjectRoot 'backend'
$FrontendDir = Join-Path $ProjectRoot 'frontend'
$DatabaseDir = Join-Path $ProjectRoot 'database'
$CentosDeployDir = Join-Path $ProjectRoot 'deploy\centos'

Write-Host "Preparing release directory: $ReleaseDir"
if (Test-Path $ReleaseDir) {
    Remove-Item -Recurse -Force $ReleaseDir
}

$null = New-Item -ItemType Directory -Force -Path (Join-Path $ReleaseDir 'backend')
$null = New-Item -ItemType Directory -Force -Path (Join-Path $ReleaseDir 'frontend')
$null = New-Item -ItemType Directory -Force -Path (Join-Path $ReleaseDir 'database')
$null = New-Item -ItemType Directory -Force -Path (Join-Path $ReleaseDir 'scripts')
$null = New-Item -ItemType Directory -Force -Path (Join-Path $ReleaseDir 'nginx')

if (-not $SkipBackendBuild) {
    Push-Location $BackendDir
    try {
        & mvn '-DskipTests' 'clean' 'package'
        if ($LASTEXITCODE -ne 0) {
            throw "Backend build failed with exit code $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
}

$JarFile = Get-ChildItem -Path (Join-Path $BackendDir 'target') -Filter *.jar -File |
    Where-Object { $_.Name -notlike '*.original' } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $JarFile) {
    throw 'Cannot find packaged jar in backend\target'
}

Copy-Item $JarFile.FullName (Join-Path $ReleaseDir 'backend\student-management.jar') -Force

if (-not $SkipFrontendBuild) {
    Push-Location $FrontendDir
    try {
        & npm 'ci'
        if ($LASTEXITCODE -ne 0) {
            throw "Frontend dependency install failed with exit code $LASTEXITCODE"
        }

        & npm 'run' 'build'
        if ($LASTEXITCODE -ne 0) {
            throw "Frontend build failed with exit code $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
}

$FrontendDist = Join-Path $FrontendDir 'dist'
if (-not (Test-Path $FrontendDist)) {
    throw 'Cannot find frontend\dist'
}

Copy-Item (Join-Path $FrontendDist '*') (Join-Path $ReleaseDir 'frontend') -Recurse -Force
Copy-Item (Join-Path $DatabaseDir 'schema.sql') (Join-Path $ReleaseDir 'database') -Force
Copy-Item (Join-Path $DatabaseDir 'data.sql') (Join-Path $ReleaseDir 'database') -Force

Copy-Item (Join-Path $CentosDeployDir 'app.env.example') (Join-Path $ReleaseDir 'scripts') -Force
Copy-Item (Join-Path $CentosDeployDir 'start-backend.sh') (Join-Path $ReleaseDir 'scripts') -Force
Copy-Item (Join-Path $CentosDeployDir 'stop-backend.sh') (Join-Path $ReleaseDir 'scripts') -Force
Copy-Item (Join-Path $CentosDeployDir 'restart-backend.sh') (Join-Path $ReleaseDir 'scripts') -Force
Copy-Item (Join-Path $CentosDeployDir 'status-backend.sh') (Join-Path $ReleaseDir 'scripts') -Force
Copy-Item (Join-Path $CentosDeployDir 'student-management.service') (Join-Path $ReleaseDir 'scripts') -Force
Copy-Item (Join-Path $CentosDeployDir 'student-management.conf') (Join-Path $ReleaseDir 'nginx') -Force
Copy-Item (Join-Path $CentosDeployDir 'README.md') $ReleaseDir -Force

Write-Host "Release package is ready: $ReleaseDir"
