$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$toolsDirectory = Join-Path $projectRoot "tools"
$junitVersion = "1.10.2"
$junitJar = Join-Path $toolsDirectory "junit-platform-console-standalone-$junitVersion.jar"
$junitUrl = "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/$junitVersion/junit-platform-console-standalone-$junitVersion.jar"

Set-Location $projectRoot

if (!(Test-Path $junitJar)) {
    New-Item -ItemType Directory -Path $toolsDirectory -Force | Out-Null
    Write-Host "Henter JUnit $junitVersion..."
    Invoke-WebRequest -Uri $junitUrl -OutFile $junitJar
}

if (Test-Path "out") {
    Remove-Item -Recurse -Force "out"
}
if (Test-Path "test-out") {
    Remove-Item -Recurse -Force "test-out"
}

New-Item -ItemType Directory -Path "out" | Out-Null
New-Item -ItemType Directory -Path "test-out" | Out-Null

Write-Host "Kompilerer produktionskode..."
& javac -d out src\*.java
if ($LASTEXITCODE -ne 0) {
    throw "Kompilering af produktionskode fejlede."
}

Write-Host "Kompilerer tests..."
& javac -cp "$junitJar;out" -d test-out test\*.java
if ($LASTEXITCODE -ne 0) {
    throw "Kompilering af tests fejlede."
}

Write-Host "Koerer JUnit-tests..."
& java -jar $junitJar execute --class-path "out;test-out" --scan-class-path --details tree
if ($LASTEXITCODE -ne 0) {
    throw "En eller flere tests fejlede."
}

Write-Host "Alle tests bestaaet."
