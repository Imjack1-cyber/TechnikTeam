<#
.SYNOPSIS
Combines code files from specified directories into chunked output files, with UTF-8 support and file size limit.

.PARAMETER outputFile
The base output filename.

.PARAMETER sourceDirectory
The main directory to recursively search for source files.

.PARAMETER maxFileSizeKB
The maximum size (in KB) for each output file before splitting.
#>

param (
    [string]$outputFile = "technikteam.txt",
    [string]$sourceDirectory = "src",
    [int]$maxFileSizeKB = 400
)

# --- Constants and Preparation ---
$maxFileSizeInBytes = $maxFileSizeKB * 1024
$baseName = [System.IO.Path]::GetFileNameWithoutExtension($outputFile)
$extension = [System.IO.Path]::GetExtension($outputFile)

Write-Host "`n==================== Script Starting ====================" -ForegroundColor Green
Write-Host "Output file base name: $baseName"
Write-Host "File size limit: $maxFileSizeKB KB"
Write-Host "Main source directory: $sourceDirectory"
Write-Host "--------------------------------------------------------"

# --- Remove Previous Output Files ---
$oldFiles = Get-ChildItem -Path "." -Filter "$baseName*$extension"
if ($oldFiles) {
    $oldFiles | Remove-Item
    Write-Host "Removed previous output files: $($oldFiles.Count)"
}

# --- File Selection ---
$fileTypes = @("*.java", "*.jsp", "*.css", "*.js", "*.xml")
$mainFiles = Get-ChildItem -Path $sourceDirectory -Recurse -Include $fileTypes -File

# Add specific additional files
$additionalFiles = @()

# Include files from jspf directory if it exists
$jspfPath = "TechnikTeam/src/main/webapp/WEB-INF/jspf"
if (Test-Path $jspfPath) {
    $additionalFiles += Get-ChildItem -Path $jspfPath -Recurse -File
    Write-Host "Included files from: $jspfPath"
}

# Include the pom.xml file
$pomPath = "TechnikTeam/pom.xml"
if (Test-Path $pomPath) {
    $additionalFiles += Get-Item $pomPath
    Write-Host "Included file: $pomPath"
}

# Combine and sort files
$allFiles = $mainFiles + $additionalFiles | Sort-Object FullName

if ($allFiles.Count -eq 0) {
    Write-Host "No matching files found. Exiting." -ForegroundColor Yellow
    exit
}

Write-Host "Total files to process: $($allFiles.Count)"
Write-Host "--------------------------------------------------------"

# --- File Processing ---
$fileIndex = 0
$currentOutputFile = $outputFile
$processedCount = 0
$skippedCount = 0

foreach ($file in $allFiles) {
    $processedCount++
    $percent = [math]::Round(($processedCount / $allFiles.Count) * 100, 0)
    Write-Progress -Activity "Combining files..." -Status "$processedCount of $($allFiles.Count)" -PercentComplete $percent
    Write-Host "Processing: $($file.FullName)"

    # Check output file size
    if (Test-Path $currentOutputFile) {
        $currentSize = (Get-Item $currentOutputFile).Length
        if ($currentSize -ge $maxFileSizeInBytes) {
            Write-Host "Splitting file: $currentOutputFile exceeded $maxFileSizeKB KB." -ForegroundColor Cyan
            $fileIndex++
            $currentOutputFile = "$baseName$fileIndex$extension"
            Write-Host "Switched to new output file: $currentOutputFile" -ForegroundColor Cyan
        }
    }

    # Header
    $header = @"
========================================================================
FILE: $($file.FullName)
========================================================================

"@

    try {
        Add-Content -Path $currentOutputFile -Value $header -Encoding utf8
        Get-Content -Path $file.FullName -Raw -Encoding utf8 | Out-File -FilePath $currentOutputFile -Append -Encoding utf8
    } catch {
        $errorMsg = "Error processing file $($file.FullName): $_"
        Write-Host $errorMsg -ForegroundColor Red
        Add-Content -Path $currentOutputFile -Value $errorMsg -Encoding utf8
        $skippedCount++
    }
}

# --- Summary ---
Write-Host "`n==================== Script Completed ====================" -ForegroundColor Green
Write-Host "Files processed: $processedCount"
Write-Host "Files skipped due to errors: $skippedCount"
Write-Host "Output files created:"
Get-ChildItem -Filter "$baseName*$extension" | ForEach-Object {
    Write-Host " - $($_.Name) [$([math]::Round($_.Length / 1024, 2)) KB]"
}
