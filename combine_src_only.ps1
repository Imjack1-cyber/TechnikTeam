# ===================================================================
#           Code Combiner Script for PowerShell (src folder only)
# ===================================================================
#
# Description:
# This script searches for specified file types exclusively within the
# 'src' subdirectory and combines their contents into a single
# text file (or a series of text files if a size limit is reached)
# in the project's root directory.
#
# V2.0 Update:
# - Enforces UTF-8 encoding for both reading source files and writing
#   the output file to prevent character corruption (e.g., German umlauts).
# - Added a try/catch block to handle potential read errors for individual
#   files without stopping the entire script.
#
# ===================================================================

# --- Configuration ---
$outputFile = "technikteam.txt"
$fileTypes = @("*.java", "*.jsp", "*.css", "*.js", "*.xml")
$sourceDirectory = "src" 

# New: Maximum file size for each output file in Kilobytes (KB)
$maxFileSizeKB = 350
# Convert KB to Bytes for comparison
$maxFileSizeInBytes = $maxFileSizeKB * 1024

# --- Script Start ---
Write-Host "Starting code combination script..." -ForegroundColor Green

# Get the base name and extension for creating numbered files later
$baseName = [System.IO.Path]::GetFileNameWithoutExtension($outputFile)
$extension = [System.IO.Path]::GetExtension($outputFile)

Write-Host "Output file base name: $baseName"
Write-Host "File size limit: $($maxFileSizeKB)KB per file"
Write-Host "Searching within folder: $sourceDirectory"
Write-Host "Searching for file types: $($fileTypes -join ', ')"

# Check if the source directory exists
if (-not (Test-Path -Path $sourceDirectory -PathType Container)) {
    Write-Host "Error: The source directory '$sourceDirectory' was not found." -ForegroundColor Red
    Write-Host "Please run this script from your project's root directory." -ForegroundColor Yellow
    exit
}

# Remove all old output files matching the pattern (e.g., technikteam.txt, technikteam1.txt, etc.)
$oldFiles = Get-ChildItem -Path "." -Filter "$($baseName)*$($extension)"
if ($oldFiles) {
    $oldFiles | Remove-Item
    Write-Host "Removed existing output files matching pattern '$($baseName)*$($extension)'."
}

# Get all files matching the types, recursively, within the specified source directory
$filesToProcess = Get-ChildItem -Path $sourceDirectory -Recurse -Include $fileTypes

if ($filesToProcess.Count -eq 0) {
    Write-Host "Warning: No files found matching the specified types in the '$sourceDirectory' folder." -ForegroundColor Yellow
    exit
}

Write-Host "Found $($filesToProcess.Count) files to process."

# --- File Processing with Size Check ---

# Initialize variables for file splitting
$fileIndex = 0
# The first file will not have a number, subsequent files will be technikteam1.txt, technikteam2.txt, etc.
$currentOutputFile = $outputFile 

# Loop through each file found
foreach ($file in $filesToProcess) {

    # --- Check file size and switch to a new file if needed ---
    if (Test-Path $currentOutputFile) {
        $currentSize = (Get-Item -Path $currentOutputFile).Length
        if ($currentSize -ge $maxFileSizeInBytes) {
            Write-Host "File '$currentOutputFile' reached size limit ($([math]::Round($currentSize/1024, 2)) KB)." -ForegroundColor Cyan
            $fileIndex++
            $currentOutputFile = "$baseName$fileIndex$extension"
            Write-Host "Switching to new output file: '$currentOutputFile'" -ForegroundColor Cyan
        }
    }

    Write-Host "Processing: $($file.FullName) -> $currentOutputFile"

    # Create a header with the full file path
    $header = @"

========================================================================
FILE: $($file.FullName)
========================================================================

"@
    
    # Use a try-catch block to handle potential errors with a single file
    try {
        # **UPDATED**: Add header to the output file, explicitly using UTF-8 encoding.
        Add-Content -Path $currentOutputFile -Value $header -Encoding utf8

        # **UPDATED**: Read the source file as UTF-8 and append its content to the output file, also ensuring UTF-8.
        Get-Content -Path $file.FullName -Raw -Encoding utf8 | Out-File -FilePath $currentOutputFile -Append -Encoding utf8
    }
    catch {
        $errorMessage = "Error processing file $($file.FullName): $_"
        Write-Host $errorMessage -ForegroundColor Red
        # Also add the error message to the output file to signify a problem with this specific file
        Add-Content -Path $currentOutputFile -Value $errorMessage -Encoding utf8
    }
}

Write-Host "Script finished successfully!" -ForegroundColor Green
Write-Host "All content from the 'src' folder has been combined into files starting with '$baseName'."