$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$srcDir = Join-Path $repoRoot "src"
$testDir = Join-Path $repoRoot "test"
$outDir = Join-Path $repoRoot "out"
$testOutDir = Join-Path $outDir "tests"
$libDir = Join-Path $repoRoot "lib"

New-Item -ItemType Directory -Force -Path $libDir | Out-Null
New-Item -ItemType Directory -Force -Path $testDir | Out-Null
New-Item -ItemType Directory -Force -Path $testOutDir | Out-Null

$jarUrl = "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar"
$jarPath = Join-Path $libDir "junit-platform-console-standalone-1.10.2.jar"

if (-not (Test-Path $jarPath)) {
    Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath
}

$sourceFiles = @(
    (Join-Path $srcDir "FileValidator.java"),
    (Join-Path $srcDir "CommandParser.java"),
    (Join-Path $testDir "FileValidatorTest.java"),
    (Join-Path $testDir "CommandParserTest.java")
)

& javac -cp $jarPath -d $testOutDir $sourceFiles
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

& java -jar $jarPath --class-path $testOutDir --scan-class-path
exit $LASTEXITCODE
