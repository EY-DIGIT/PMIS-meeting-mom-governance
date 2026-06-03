<#
.SYNOPSIS
  Bootstrap build script for the UIDAI Meetings & MoM Governance Module.

  Maven is not required to be pre-installed. If a local Maven is not found,
  this script downloads Apache Maven into a project-local ".maven" folder and
  uses it to run the requested goals.

.EXAMPLE
  .\build.ps1                 # defaults to: clean package
  .\build.ps1 test            # run tests
  .\build.ps1 spring-boot:run # run the application
#>
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Goals = @('clean', 'package')
)

$ErrorActionPreference = 'Stop'
$mavenVersion = '3.9.9'
$root = $PSScriptRoot
$mavenHome = Join-Path $root ".maven\apache-maven-$mavenVersion"
$mvnCmd = Join-Path $mavenHome 'bin\mvn.cmd'

if (-not (Test-Path $mvnCmd)) {
    Write-Host "Local Maven $mavenVersion not found. Downloading..." -ForegroundColor Yellow
    $zipUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
    $zipPath = Join-Path $root ".maven\maven.zip"
    New-Item -ItemType Directory -Force -Path (Join-Path $root '.maven') | Out-Null
    Invoke-WebRequest -Uri $zipUrl -OutFile $zipPath
    Expand-Archive -Path $zipPath -DestinationPath (Join-Path $root '.maven') -Force
    Remove-Item $zipPath -Force
}

$env:JAVA_HOME = $env:JAVA_HOME
Write-Host "Running: mvn $($Goals -join ' ')" -ForegroundColor Cyan
& $mvnCmd @Goals
exit $LASTEXITCODE
