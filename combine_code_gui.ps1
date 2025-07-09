#Requires -Version 5.1
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

# --- GLOBAL SCRIPT VARIABLES ---
$script:stopRequested = $false
$configFile = Join-Path $PSScriptRoot "config.json"

# === HELPER FUNCTIONS ===
function Load-Settings {
    if (Test-Path $configFile) {
        try {
            $settings = Get-Content -Path $configFile | ConvertFrom-Json
            $txtSourceDir.Text = $settings.SourceDirectory
            $txtOutputFile.Text = $settings.OutputFile
            $numMaxSize.Value = $settings.MaxFileSizeKB
            $txtExclude.Text = $settings.ExcludeList
        } catch {
            Write-Warning "Could not load or parse config.json. Using defaults."
        }
    }
}

function Save-Settings {
    $settings = [PSCustomObject]@{
        SourceDirectory = $txtSourceDir.Text
        OutputFile      = $txtOutputFile.Text
        MaxFileSizeKB   = $numMaxSize.Value
        ExcludeList     = $txtExclude.Text
    }
    $settings | ConvertTo-Json | Set-Content -Path $configFile
}

# <-- MODIFIED: This function no longer forces a form refresh, making it much faster.
function Log-Message([string]$message, [System.Windows.Forms.TextBox]$logBox) {
    $logBox.AppendText("$message`r`n")
    $logBox.ScrollToCaret()
}

# === UI FORM SETUP ===
$form = New-Object System.Windows.Forms.Form
$form.Text = "Code Combiner Pro"
$form.Size = New-Object System.Drawing.Size(640, 600)
$form.StartPosition = "CenterScreen"
$form.FormBorderStyle = 'FixedDialog'
$form.MaximizeBox = $false

# --- UI Controls Definition ---
$lblSourceDir = New-Object System.Windows.Forms.Label; $lblSourceDir.Text = "Source Directory:"; $lblSourceDir.Location = New-Object System.Drawing.Point(10, 22); $lblSourceDir.Size = New-Object System.Drawing.Size(120, 20)
$txtSourceDir = New-Object System.Windows.Forms.TextBox; $txtSourceDir.Location = New-Object System.Drawing.Point(140, 20); $txtSourceDir.Size = New-Object System.Drawing.Size(350, 20); $txtSourceDir.Anchor = 'Top, Left, Right'; $txtSourceDir.AllowDrop = $true; $txtSourceDir.Text = "C:\Users\techn\eclipse\workspace\TechnikTeam"
$btnBrowse = New-Object System.Windows.Forms.Button; $btnBrowse.Text = "Browse..."; $btnBrowse.Location = New-Object System.Drawing.Point(500, 18); $btnBrowse.Size = New-Object System.Drawing.Size(110, 25); $btnBrowse.Anchor = 'Top, Right'
$lblOutputFile = New-Object System.Windows.Forms.Label; $lblOutputFile.Text = "Output File Name:"; $lblOutputFile.Location = New-Object System.Drawing.Point(10, 62); $lblOutputFile.Size = New-Object System.Drawing.Size(120, 20)
$txtOutputFile = New-Object System.Windows.Forms.TextBox; $txtOutputFile.Location = New-Object System.Drawing.Point(140, 60); $txtOutputFile.Size = New-Object System.Drawing.Size(470, 20); $txtOutputFile.Anchor = 'Top, Left, Right'; $txtOutputFile.Text = "technikteam.txt"
$lblMaxSize = New-Object System.Windows.Forms.Label; $lblMaxSize.Text = "Max File Size (KB):"; $lblMaxSize.Location = New-Object System.Drawing.Point(10, 102); $lblMaxSize.Size = New-Object System.Drawing.Size(120, 20)
$numMaxSize = New-Object System.Windows.Forms.NumericUpDown; $numMaxSize.Location = New-Object System.Drawing.Point(140, 100); $numMaxSize.Size = New-Object System.Drawing.Size(100, 20); $numMaxSize.Maximum = 10000; $numMaxSize.Value = 400
$lblExclude = New-Object System.Windows.Forms.Label; $lblExclude.Text = "Exclude (comma separated):"; $lblExclude.Location = New-Object System.Drawing.Point(10, 142); $lblExclude.Size = New-Object System.Drawing.Size(250, 20)
$txtExclude = New-Object System.Windows.Forms.TextBox; $txtExclude.Location = New-Object System.Drawing.Point(10, 160); $txtExclude.Size = New-Object System.Drawing.Size(600, 20); $txtExclude.Anchor = 'Top, Left, Right'
$txtExclude.Text = "target,.settings,.metadata,resources,.gitattributes,.project,.classpath,dbScript.txt,README.md,.gitignore,run_combine_src_only.txt,combine_src_only.ps1,technikteam.txt,technikteam1.txt,technikteam2.txt,FileCombiner.exe,combine_code_gui.ps1,pom.xml,config.json"
$btnStart = New-Object System.Windows.Forms.Button; $btnStart.Text = "Start"; $btnStart.Location = New-Object System.Drawing.Point(10, 200); $btnStart.Size = New-Object System.Drawing.Size(90, 30)
$btnStop = New-Object System.Windows.Forms.Button; $btnStop.Text = "Stop"; $btnStop.Location = New-Object System.Drawing.Point(110, 200); $btnStop.Size = New-Object System.Drawing.Size(90, 30); $btnStop.Enabled = $false
$btnClearLog = New-Object System.Windows.Forms.Button; $btnClearLog.Text = "Clear Log"; $btnClearLog.Location = New-Object System.Drawing.Point(210, 200); $btnClearLog.Size = New-Object System.Drawing.Size(90, 30)
$btnOpenDir = New-Object System.Windows.Forms.Button; $btnOpenDir.Text = "Open Output Folder"; $btnOpenDir.Location = New-Object System.Drawing.Point(310, 200); $btnOpenDir.Size = New-Object System.Drawing.Size(140, 30); $btnOpenDir.Visible = $false
$progressBar = New-Object System.Windows.Forms.ProgressBar; $progressBar.Location = New-Object System.Drawing.Point(10, 240); $progressBar.Size = New-Object System.Drawing.Size(600, 10); $progressBar.Anchor = 'Top, Left, Right'
$lblStatus = New-Object System.Windows.Forms.Label; $lblStatus.Text = "Idle. Click 'Start' to begin."; $lblStatus.Location = New-Object System.Drawing.Point(10, 260); $lblStatus.Size = New-Object System.Drawing.Size(600, 20); $lblStatus.Anchor = 'Top, Left, Right'
$txtLog = New-Object System.Windows.Forms.TextBox; $txtLog.Location = New-Object System.Drawing.Point(10, 290); $txtLog.Size = New-Object System.Drawing.Size(600, 250); $txtLog.Multiline = $true; $txtLog.ScrollBars = "Vertical"; $txtLog.Anchor = 'Top, Left, Right, Bottom'; $txtLog.ReadOnly = $true

$form.Controls.AddRange(@($lblSourceDir, $txtSourceDir, $btnBrowse, $lblOutputFile, $txtOutputFile, $lblMaxSize, $numMaxSize, $lblExclude, $txtExclude, $btnStart, $btnStop, $btnClearLog, $btnOpenDir, $progressBar, $lblStatus, $txtLog))

# === EVENT HANDLERS ===
$form.Add_Load({ Load-Settings })
$form.Add_FormClosing({ Save-Settings })
$btnBrowse.Add_Click({ $folderBrowser = New-Object System.Windows.Forms.FolderBrowserDialog; if ($folderBrowser.ShowDialog() -eq "OK") { $txtSourceDir.Text = $folderBrowser.SelectedPath } })
$txtSourceDir.Add_DragEnter({ param($s, $e) if ($e.Data.GetDataPresent([System.Windows.Forms.DataFormats]::FileDrop)) { $e.Effect = 'Copy' } })
$txtSourceDir.Add_DragDrop({ param($s, $e) $files = $e.Data.GetData([System.Windows.Forms.DataFormats]::FileDrop); if ($files.Count -gt 0 -and [System.IO.Directory]::Exists($files[0])) { $txtSourceDir.Text = $files[0] } })
$btnClearLog.Add_Click({ $txtLog.Clear() })
$btnOpenDir.Add_Click({ Invoke-Item . })
$btnStop.Add_Click({ $script:stopRequested = $true; $lblStatus.Text = "Stopping process..."; $btnStop.Enabled = $false })

$btnStart.Add_Click({
    $script:stopRequested = $false
    $btnStart.Enabled = $false; $btnBrowse.Enabled = $false; $btnStop.Enabled = $true; $btnOpenDir.Visible = $false
    $txtLog.Text = ""; $progressBar.Value = 0

    $streamWriter = $null # Initialize stream writer variable

    try {
        $sourceDirectory = $txtSourceDir.Text.Trim()
        if (-not (Test-Path $sourceDirectory)) { throw "Source directory does not exist." }
        
        try { $testFile = Join-Path "." "_test_write.tmp"; New-Item -Path $testFile -ItemType File -Force | Out-Null; Remove-Item $testFile -Force } catch { throw "Cannot write to the output directory. Please check permissions." }

        Log-Message "Starting combine from: $sourceDirectory" $txtLog
        $lblStatus.Text = "Searching for files..."; $form.Refresh()

        $maxFileSizeKB = $numMaxSize.Value; $excludeList = $txtExclude.Text -split "," | ForEach-Object { $_.Trim().ToLower() } | Where-Object { $_ }; $fileTypes = @("*.java", "*.jsp", "*.jspf", "*.css", "*.js", "*.xml")
        $allFiles = Get-ChildItem -Path $sourceDirectory -Recurse -File -ErrorAction SilentlyContinue | Where-Object { $file = $_; $typeMatch = $false; foreach ($pattern in $fileTypes) { if ($file.Name -like $pattern) { $typeMatch = $true; break } }; $pathExcluded = $false; $relPath = $file.FullName.Substring($sourceDirectory.Length).TrimStart('\','/'); $pathSegments = $relPath.ToLower() -split '[\\/]'; foreach ($excludeItem in $excludeList) { if ($pathSegments -contains $excludeItem) { $pathExcluded = $true; break } }; ($typeMatch -and (-not $pathExcluded)) }
        if ($allFiles.Count -eq 0) { throw "No matching files found to process." }
        $progressBar.Maximum = $allFiles.Count
        Log-Message "Found $($allFiles.Count) files to process." $txtLog

        $outputFile = $txtOutputFile.Text.Trim(); $maxFileSizeInBytes = $maxFileSizeKB * 1024; $baseName = [System.IO.Path]::GetFileNameWithoutExtension($outputFile); $extension = [System.IO.Path]::GetExtension($outputFile)
        Get-ChildItem -Path "." -Filter "$baseName*$extension" | Remove-Item
        
        $fileIndex = 0; $currentOutputFile = $outputFile; $processedCount = 0; $skippedCount = 0; $currentSize = 0
        
        # <-- OPTIMIZED: Open the first file stream
        $streamWriter = New-Object System.IO.StreamWriter($currentOutputFile, $false, [System.Text.Encoding]::UTF8)
        Log-Message "`r`n--- Writing to: $currentOutputFile ---" $txtLog

        foreach ($file in $allFiles) {
            if ($script:stopRequested) { Log-Message "`r`n*** USER CANCELED OPERATION ***" $txtLog; break }
            $processedCount++
            $lblStatus.Text = "Processing ($processedCount / $($allFiles.Count)): $($file.Name)"
            $progressBar.Value = $processedCount
            if ($processedCount % 10 -eq 0) { $form.Refresh() } # <-- OPTIMIZED: Refresh UI only every 10 files

            if ($currentSize + $file.Length -ge $maxFileSizeInBytes) {
                $streamWriter.Close(); $streamWriter.Dispose() # Close the previous stream
                $fileIndex++; $currentOutputFile = "$baseName$fileIndex$extension"; $currentSize = 0
                $streamWriter = New-Object System.IO.StreamWriter($currentOutputFile, $false, [System.Text.Encoding]::UTF8) # Open the new stream
                Log-Message "`r`n--- Splitting: new file is $currentOutputFile ---" $txtLog
            }
            
            $header = "`r`n========================================================================`r`nFILE: $($file.FullName)`r`n========================================================================`r`n`r`n"
            try {
                $fileContent = Get-Content -Path $file.FullName -Raw -Encoding utf8
                $streamWriter.Write($header)
                $streamWriter.Write($fileContent)
                $currentSize += ($header.Length + $fileContent.Length)
            } catch {
                Log-Message "`r`nERROR processing file: $($file.FullName):`r`n$($_)" $txtLog; $skippedCount++
            }
        }

        if (-not $script:stopRequested) { $lblStatus.Text = "Complete! Processed $processedCount files."; $btnOpenDir.Visible = $true }
        Log-Message "`r`n--- SUMMARY ---" $txtLog; Log-Message "Files processed: $processedCount" $txtLog; Log-Message "Files skipped: $skippedCount" $txtLog; Log-Message "Output files:" $txtLog
        Get-ChildItem -Path "." -Filter "$baseName*$extension" | ForEach-Object { Log-Message " - $($_.Name) ($([math]::Round($_.Length / 1024, 2)) KB)" $txtLog }

    } catch {
        $lblStatus.Text = "Error! See log for details."; Log-Message "`r`nFATAL ERROR: $_" $txtLog
    } finally {
        if ($streamWriter) { $streamWriter.Close(); $streamWriter.Dispose() }
        $btnStart.Enabled = $true; $btnBrowse.Enabled = $true; $btnStop.Enabled = $false
    }
})

# === Show the form ===
[void]$form.ShowDialog()