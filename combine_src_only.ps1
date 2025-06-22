# ===================================================================
#           Code Combiner Script for PowerShell (src folder only)
# ===================================================================
#
# Description:
# This script searches for specified file types exclusively within the
# 'src' subdirectory and combines their contents into a single
# text file in the project's root directory.
#
# ===================================================================

# --- Configuration ---
$outputFile = "technikteam.txt"
$fileTypes = @("*.java", "*.jsp", "*.css", "*.js", "*.xml")

# The directory to search within. Changed from "." to "src".
$sourceDirectory = "src" 

# --- Script Start ---
Write-Host "Starting code combination script..." -ForegroundColor Green
Write-Host "Output file will be: $outputFile"
Write-Host "Searching within folder: $sourceDirectory"
Write-Host "Searching for file types: $($fileTypes -join ', ')"

# Check if the source directory exists
if (-not (Test-Path -Path $sourceDirectory -PathType Container)) {
    Write-Host "Error: The source directory '$sourceDirectory' was not found." -ForegroundColor Red
    Write-Host "Please run this script from your project's root directory." -ForegroundColor Yellow
    exit
}

# Remove the old output file if it exists to start fresh
if (Test-Path $outputFile) {
    Remove-Item $outputFile
    Write-Host "Removed existing output file."
}

# Get all files matching the types, recursively, within the specified source directory
$filesToProcess = Get-ChildItem -Path $sourceDirectory -Recurse -Include $fileTypes

if ($filesToProcess.Count -eq 0) {
    Write-Host "Warning: No files found matching the specified types in the '$sourceDirectory' folder." -ForegroundColor Yellow
    exit
}

Write-Host "Found $($filesToProcess.Count) files to process."

# Loop through each file found
foreach ($file in $filesToProcess) {
    Write-Host "Processing: $($file.FullName)"

    # Create a header with the full file path
    $header = @"

========================================================================
FILE: $($file.FullName)
========================================================================

"@

    # Add the header and the file's content to the output file
    Add-Content -Path $outputFile -Value $header
    Get-Content -Path $file.FullName | Add-Content -Path $outputFile
}

Write-Host "Script finished successfully!" -ForegroundColor Green
Write-Host "All content from the 'src' folder has been combined into '$outputFile'"