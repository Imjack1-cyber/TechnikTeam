#Requires -Version 5.1
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName Microsoft.VisualBasic

# --- GLOBAL SCRIPT VARIABLES ---
$script:stopRequested = $false
$script:excludedDirs = [System.Collections.Generic.List[string]]::new()
$script:excludedFiles = [System.Collections.Generic.List[string]]::new()
$script:extensionFilterList = [System.Collections.Generic.List[string]]::new()
$script:extensionFilterMode = "exclude"
$script:lastSearchIndex = 0
$configFile = Join-Path $PSScriptRoot "config.json"

# === HELPER FUNCTIONS ===

# DIESE FUNKTION LÄDT IHRE EINSTELLUNGEN BEIM START
function Load-Settings {
    if (Test-Path $configFile) {
        try {
            $settings = Get-Content -Path $configFile -Raw -Encoding UTF8 | ConvertFrom-Json
            
            $txtSourceDir.Text = $settings.SourceDirectory
            $txtOutputFile.Text = $settings.OutputFile
            $numMaxSize.Value = $settings.MaxFileSizeKB

            $script:excludedDirs.Clear()
            $script:extensionFilterList.Clear()
            $script:excludedFiles.Clear()

            if ($settings.ExcludedDirectories) { 
                $script:excludedDirs.AddRange([string[]]$settings.ExcludedDirectories) 
            }
            if ($settings.ExcludedFiles) {
                $script:excludedFiles.AddRange([string[]]$settings.ExcludedFiles)
            }
            if ($settings.ExtensionFilterList) { 
                $script:extensionFilterList.AddRange([string[]]$settings.ExtensionFilterList) 
            }
            
            if ($settings.ExtensionFilterMode) { 
                $script:extensionFilterMode = $settings.ExtensionFilterMode 
            } else { 
                $script:extensionFilterMode = "exclude" 
            }

        } catch {
            Write-Warning "Could not load or parse config.json. Using defaults. Error: $($_.Exception.Message)"
        }
    }
}

# DIESE FUNKTION SPEICHERT IHRE EINSTELLUNGEN BEIM SCHLIESSEN
function Save-Settings {
    $settings = [PSCustomObject]@{
        SourceDirectory      = $txtSourceDir.Text
        OutputFile           = $txtOutputFile.Text
        MaxFileSizeKB        = $numMaxSize.Value
        ExcludedDirectories  = $script:excludedDirs
        ExcludedFiles        = $script:excludedFiles
        ExtensionFilterList  = $script:extensionFilterList
        ExtensionFilterMode  = $script:extensionFilterMode
    }
    try {
        $settings | ConvertTo-Json -Depth 5 | Set-Content -Path $configFile -Force -Encoding UTF8
    } catch {
        $errorMessage = "FATAL: Could not save settings to `"$configFile`".`n`nPlease check file permissions.`n`nError details: $($_.Exception.Message)"
        [System.Windows.Forms.MessageBox]::Show($errorMessage, "Save Error", "OK", "Error")
    }
}

function Log-Message([string]$message, [System.Windows.Forms.TextBox]$logBox) {
    if ($logBox.IsDisposed) { return }
    $logBox.AppendText("$message`r`n")
    $logBox.ScrollToCaret()
}

# === UI FORM SETUP ===
$form = New-Object System.Windows.Forms.Form
$form.Text = "TechnikTeam Code Combiner"; $form.Size = New-Object System.Drawing.Size(640, 600); $form.StartPosition = "CenterScreen"; $form.FormBorderStyle = 'FixedDialog'; $form.MaximizeBox = $false

# --- UI Controls Definition ---
$lblSourceDir = New-Object System.Windows.Forms.Label; $lblSourceDir.Text = "Project Root Directory:"; $lblSourceDir.Location = New-Object System.Drawing.Point(10, 22); $lblSourceDir.Size = New-Object System.Drawing.Size(120, 20)
$txtSourceDir = New-Object System.Windows.Forms.TextBox; $txtSourceDir.Location = New-Object System.Drawing.Point(140, 20); $txtSourceDir.Size = New-Object System.Drawing.Size(350, 20); $txtSourceDir.Anchor = 'Top, Left, Right'; $txtSourceDir.AllowDrop = $true
$btnBrowse = New-Object System.Windows.Forms.Button; $btnBrowse.Text = "Browse..."; $btnBrowse.Location = New-Object System.Drawing.Point(500, 18); $btnBrowse.Size = New-Object System.Drawing.Size(110, 25); $btnBrowse.Anchor = 'Top, Right'
$lblOutputFile = New-Object System.Windows.Forms.Label; $lblOutputFile.Text = "Output File Name:"; $lblOutputFile.Location = New-Object System.Drawing.Point(10, 62); $lblOutputFile.Size = New-Object System.Drawing.Size(120, 20)
$txtOutputFile = New-Object System.Windows.Forms.TextBox; $txtOutputFile.Location = New-Object System.Drawing.Point(140, 60); $txtOutputFile.Size = New-Object System.Drawing.Size(470, 20); $txtOutputFile.Anchor = 'Top, Left, Right'; $txtOutputFile.Text = "combined-code.txt"
$lblMaxSize = New-Object System.Windows.Forms.Label; $lblMaxSize.Text = "Max File Size (KB):"; $lblMaxSize.Location = New-Object System.Drawing.Point(10, 102); $lblMaxSize.Size = New-Object System.Drawing.Size(120, 20)
$numMaxSize = New-Object System.Windows.Forms.NumericUpDown; $numMaxSize.Location = New-Object System.Drawing.Point(140, 100); $numMaxSize.Size = New-Object System.Drawing.Size(100, 20); $numMaxSize.Maximum = 10000; $numMaxSize.Value = 400
$btnManageExclusions = New-Object System.Windows.Forms.Button; $btnManageExclusions.Text = "Filter verwalten..."; $btnManageExclusions.Location = New-Object System.Drawing.Point(440, 98); $btnManageExclusions.Size = New-Object System.Drawing.Size(170, 25); $btnManageExclusions.Anchor = 'Top, Right'
$btnStart = New-Object System.Windows.Forms.Button; $btnStart.Text = "Start"; $btnStart.Location = New-Object System.Drawing.Point(10, 150); $btnStart.Size = New-Object System.Drawing.Size(90, 30)
$btnStop = New-Object System.Windows.Forms.Button; $btnStop.Text = "Stop"; $btnStop.Location = New-Object System.Drawing.Point(110, 150); $btnStop.Size = New-Object System.Drawing.Size(90, 30); $btnStop.Enabled = $false
$btnClearLog = New-Object System.Windows.Forms.Button; $btnClearLog.Text = "Clear Log"; $btnClearLog.Location = New-Object System.Drawing.Point(210, 150); $btnClearLog.Size = New-Object System.Drawing.Size(90, 30)
$btnOpenDir = New-Object System.Windows.Forms.Button; $btnOpenDir.Text = "Open Output Folder"; $btnOpenDir.Location = New-Object System.Drawing.Point(310, 150); $btnOpenDir.Size = New-Object System.Drawing.Size(140, 30); $btnOpenDir.Visible = $false
$progressBar = New-Object System.Windows.Forms.ProgressBar; $progressBar.Location = New-Object System.Drawing.Point(10, 190); $progressBar.Size = New-Object System.Drawing.Size(600, 10); $progressBar.Anchor = 'Top, Left, Right'
$lblStatus = New-Object System.Windows.Forms.Label; $lblStatus.Text = "Idle. Click 'Start' to begin."; $lblStatus.Location = New-Object System.Drawing.Point(10, 210); $lblStatus.Size = New-Object System.Drawing.Size(600, 20); $lblStatus.Anchor = 'Top, Left, Right'
$lblSearch = New-Object System.Windows.Forms.Label; $lblSearch.Text = "Search Log:"; $lblSearch.Location = New-Object System.Drawing.Point(10, 242); $lblSearch.Size = New-Object System.Drawing.Size(70, 20)
$txtSearch = New-Object System.Windows.Forms.TextBox; $txtSearch.Location = New-Object System.Drawing.Point(90, 240); $txtSearch.Size = New-Object System.Drawing.Size(420, 20); $txtSearch.Anchor = 'Top, Left, Right'
$btnSearch = New-Object System.Windows.Forms.Button; $btnSearch.Text = "Find Next"; $btnSearch.Location = New-Object System.Drawing.Point(520, 238); $btnSearch.Size = New-Object System.Drawing.Size(90, 25); $btnSearch.Anchor = 'Top, Right'
$txtLog = New-Object System.Windows.Forms.TextBox; $txtLog.Location = New-Object System.Drawing.Point(10, 270); $txtLog.Size = New-Object System.Drawing.Size(600, 270); $txtLog.Multiline = $true; $txtLog.ScrollBars = "Vertical"; $txtLog.Anchor = 'Top, Left, Right, Bottom'; $txtLog.ReadOnly = $true

$form.Controls.AddRange(@($lblSourceDir, $txtSourceDir, $btnBrowse, $lblOutputFile, $txtOutputFile, $lblMaxSize, $numMaxSize, $btnManageExclusions, $btnStart, $btnStop, $btnClearLog, $btnOpenDir, $progressBar, $lblStatus, $lblSearch, $txtSearch, $btnSearch, $txtLog))

# === EVENT HANDLERS ===
$form.Add_Load({ Load-Settings })
$form.Add_FormClosing({ Save-Settings })
$btnBrowse.Add_Click({ $folderBrowser = New-Object System.Windows.Forms.FolderBrowserDialog; if ($folderBrowser.ShowDialog() -eq "OK") { $txtSourceDir.Text = $folderBrowser.SelectedPath } })
$txtSourceDir.Add_DragEnter({ param($s, $e) if ($e.Data.GetDataPresent([System.Windows.Forms.DataFormats]::FileDrop)) { $e.Effect = 'Copy' } })
$txtSourceDir.Add_DragDrop({ param($s, $e) $files = $e.Data.GetData([System.Windows.Forms.DataFormats]::FileDrop); if ($files.Count -gt 0 -and [System.IO.Directory]::Exists($files[0])) { $txtSourceDir.Text = $files[0] } })
$btnClearLog.Add_Click({ $txtLog.Clear(); $script:lastSearchIndex = 0 })
$btnStop.Add_Click({ $script:stopRequested = $true; $lblStatus.Text = "Stopping process..."; $btnStop.Enabled = $false })
$btnOpenDir.Add_Click({ Invoke-Item $PSScriptRoot })

$txtSearch.Add_TextChanged({
    $script:lastSearchIndex = 0
    $txtLog.Select(0, 0)
})

$btnSearch.Add_Click({
    $searchText = $txtSearch.Text
    if ([string]::IsNullOrEmpty($searchText)) { return }

    $foundIndex = $txtLog.Text.IndexOf($searchText, $script:lastSearchIndex, [System.StringComparison]::InvariantCultureIgnoreCase)

    if ($foundIndex -ne -1) {
        $txtLog.Focus()
        $txtLog.Select($foundIndex, $searchText.Length)
        $txtLog.ScrollToCaret()
        $script:lastSearchIndex = $foundIndex + 1
    } else {
        if ($script:lastSearchIndex -gt 0) {
            $result = [System.Windows.Forms.MessageBox]::Show("End of log reached. Search from the beginning?", "Search", "YesNo", "Question")
            if ($result -eq "Yes") {
                $script:lastSearchIndex = 0
                $btnSearch.PerformClick()
            }
        } else {
            [System.Windows.Forms.MessageBox]::Show("Text not found.", "Search", "OK", "Information")
        }
    }
})

$btnManageExclusions.Add_Click({
    $exclusionForm = New-Object System.Windows.Forms.Form
    $exclusionForm.Text = "Manage Filters"; $exclusionForm.Size = New-Object System.Drawing.Size(520, 740); $exclusionForm.StartPosition = "CenterParent"; $exclusionForm.FormBorderStyle = 'FixedDialog'
    
    # Excluded Directories
    $groupDirs = New-Object System.Windows.Forms.GroupBox; $groupDirs.Text = "Excluded Directories"; $groupDirs.Location = New-Object System.Drawing.Point(10, 10); $groupDirs.Size = New-Object System.Drawing.Size(480, 200)
    $lbDirs = New-Object System.Windows.Forms.ListBox; $lbDirs.Location = New-Object System.Drawing.Point(10, 20); $lbDirs.Size = New-Object System.Drawing.Size(460, 140); $lbDirs.Anchor = 'Top, Left, Right, Bottom'
    $script:excludedDirs | ForEach-Object { [void]$lbDirs.Items.Add($_) }
    $btnAddDir = New-Object System.Windows.Forms.Button; $btnAddDir.Text = "Add..."; $btnAddDir.Location = New-Object System.Drawing.Point(10, 165); $btnAddDir.Size = New-Object System.Drawing.Size(90, 25)
    $btnRemoveDir = New-Object System.Windows.Forms.Button; $btnRemoveDir.Text = "Remove"; $btnRemoveDir.Location = New-Object System.Drawing.Point(110, 165); $btnRemoveDir.Size = New-Object System.Drawing.Size(90, 25)
    $groupDirs.Controls.AddRange(@($lbDirs, $btnAddDir, $btnRemoveDir))
    
    # Extension Filters
    $groupExts = New-Object System.Windows.Forms.GroupBox; $groupExts.Text = "Filter by File Extension"; $groupExts.Location = New-Object System.Drawing.Point(10, 220); $groupExts.Size = New-Object System.Drawing.Size(480, 240)
    $radioExcludeExt = New-Object System.Windows.Forms.RadioButton; $radioExcludeExt.Text = "Exclude extensions in this list"; $radioExcludeExt.Location = New-Object System.Drawing.Point(10, 25); $radioExcludeExt.AutoSize = $true
    $radioIncludeExt = New-Object System.Windows.Forms.RadioButton; $radioIncludeExt.Text = "Include ONLY extensions in this list"; $radioIncludeExt.Location = New-Object System.Drawing.Point(10, 50); $radioIncludeExt.AutoSize = $true
    if ($script:extensionFilterMode -eq "include") { $radioIncludeExt.Checked = $true } else { $radioExcludeExt.Checked = $true }
    $radioExcludeExt.Add_CheckedChanged({ if ($this.Checked) { $script:extensionFilterMode = "exclude" } })
    $radioIncludeExt.Add_CheckedChanged({ if ($this.Checked) { $script:extensionFilterMode = "include" } })
    $lbExts = New-Object System.Windows.Forms.ListBox; $lbExts.Location = New-Object System.Drawing.Point(10, 80); $lbExts.Size = New-Object System.Drawing.Size(460, 120); $lbExts.Anchor = 'Top, Left, Right, Bottom'
    $script:extensionFilterList | ForEach-Object { [void]$lbExts.Items.Add($_) }
    $btnAddExt = New-Object System.Windows.Forms.Button; $btnAddExt.Text = "Add..."; $btnAddExt.Location = New-Object System.Drawing.Point(10, 205); $btnAddExt.Size = New-Object System.Drawing.Size(90, 25)
    $btnRemoveExt = New-Object System.Windows.Forms.Button; $btnRemoveExt.Text = "Remove"; $btnRemoveExt.Location = New-Object System.Drawing.Point(110, 205); $btnRemoveExt.Size = New-Object System.Drawing.Size(90, 25)
    $groupExts.Controls.AddRange(@($radioExcludeExt, $radioIncludeExt, $lbExts, $btnAddExt, $btnRemoveExt))

    # Excluded Files
    $groupFiles = New-Object System.Windows.Forms.GroupBox; $groupFiles.Text = "Excluded Individual Files"; $groupFiles.Location = New-Object System.Drawing.Point(10, 470); $groupFiles.Size = New-Object System.Drawing.Size(480, 200)
    $lbFiles = New-Object System.Windows.Forms.ListBox; $lbFiles.Location = New-Object System.Drawing.Point(10, 20); $lbFiles.Size = New-Object System.Drawing.Size(460, 140); $lbFiles.Anchor = 'Top, Left, Right, Bottom'
    $script:excludedFiles | ForEach-Object { [void]$lbFiles.Items.Add($_) }
    $btnAddFile = New-Object System.Windows.Forms.Button; $btnAddFile.Text = "Add..."; $btnAddFile.Location = New-Object System.Drawing.Point(10, 165); $btnAddFile.Size = New-Object System.Drawing.Size(90, 25)
    $btnRemoveFile = New-Object System.Windows.Forms.Button; $btnRemoveFile.Text = "Remove"; $btnRemoveFile.Location = New-Object System.Drawing.Point(110, 165); $btnRemoveFile.Size = New-Object System.Drawing.Size(90, 25)
    $groupFiles.Controls.AddRange(@($lbFiles, $btnAddFile, $btnRemoveFile))
    
    $btnClose = New-Object System.Windows.Forms.Button; $btnClose.Text = "Close"; $btnClose.Location = New-Object System.Drawing.Point(410, 680); $btnClose.Size = New-Object System.Drawing.Size(80, 25); $btnClose.DialogResult = [System.Windows.Forms.DialogResult]::OK
    
    # Event Handlers for Buttons
    $btnAddDir.Add_Click({ $folderBrowser = New-Object System.Windows.Forms.FolderBrowserDialog; $folderBrowser.Description = "Select a directory to exclude"; $folderBrowser.SelectedPath = $txtSourceDir.Text; if ($folderBrowser.ShowDialog() -eq "OK") { $selectedPath = $folderBrowser.SelectedPath; if (-not $script:excludedDirs.Contains($selectedPath)) { $script:excludedDirs.Add($selectedPath); $lbDirs.Items.Add($selectedPath) } } })
    $btnRemoveDir.Add_Click({ if ($lbDirs.SelectedItem) { $itemToRemove = $lbDirs.SelectedItem; $script:excludedDirs.Remove($itemToRemove); $lbDirs.Items.Remove($itemToRemove) } })
    
    $btnAddExt.Add_Click({ $input = [Microsoft.VisualBasic.Interaction]::InputBox("Enter file extension (e.g., .log or log):", "Add Extension"); if (-not [string]::IsNullOrWhiteSpace($input)) { $ext = $input.Trim().ToLower(); if (-not $ext.StartsWith(".")) { $ext = "." + $ext }; if (-not $script:extensionFilterList.Contains($ext)) { $script:extensionFilterList.Add($ext); $lbExts.Items.Add($ext) } } })
    $btnRemoveExt.Add_Click({ if ($lbExts.SelectedItem) { $itemToRemove = $lbExts.SelectedItem; $script:extensionFilterList.Remove($itemToRemove); $lbExts.Items.Remove($itemToRemove) } })

    $btnAddFile.Add_Click({ 
        $fileDialog = New-Object System.Windows.Forms.OpenFileDialog; $fileDialog.Title = "Select a file to exclude"; $fileDialog.InitialDirectory = $txtSourceDir.Text;
        if ($fileDialog.ShowDialog() -eq "OK") { 
            $selectedFile = $fileDialog.FileName; 
            # Use PowerShell's -notcontains operator for case-insensitive comparison
            if ($script:excludedFiles -notcontains $selectedFile) { 
                $script:excludedFiles.Add($selectedFile); 
                $lbFiles.Items.Add($selectedFile) 
            } 
        } 
    })
    $btnRemoveFile.Add_Click({ if ($lbFiles.SelectedItem) { $itemToRemove = $lbFiles.SelectedItem; $script:excludedFiles.Remove($itemToRemove); $lbFiles.Items.Remove($itemToRemove) } })
    
    $exclusionForm.Controls.AddRange(@($groupDirs, $groupExts, $groupFiles, $btnClose)); $exclusionForm.AcceptButton = $btnClose
    [void]$exclusionForm.ShowDialog(); $exclusionForm.Dispose()
})

$btnStart.Add_Click({
    $script:stopRequested = $false
    $btnStart.Enabled = $false; $btnBrowse.Enabled = $false; $btnStop.Enabled = $true; $btnManageExclusions.Enabled = $false; $btnOpenDir.Visible = $false
    $txtLog.Text = ""; $progressBar.Value = 0; $outputDirectory = $PSScriptRoot; $streamWriter = $null; $script:lastSearchIndex = 0
    $successfullyProcessedFiles = [System.Collections.Generic.List[string]]::new()
    try {
        $sourceDirectory = $txtSourceDir.Text.Trim()
        if (-not (Test-Path $sourceDirectory)) { throw "Source directory does not exist." }
        try { $testFile = Join-Path $outputDirectory "_test_write.tmp"; New-Item -Path $testFile -ItemType File -Force | Out-Null; Remove-Item $testFile -Force } catch { throw "Cannot write to the output directory ($outputDirectory). Please check permissions." }
        Log-Message "Starting combine from: $sourceDirectory" $txtLog; $lblStatus.Text = "Searching for all files..."; $form.Refresh()
        $allFoundFiles = Get-ChildItem -Path $sourceDirectory -Recurse -File -ErrorAction SilentlyContinue
        Log-Message "Found $($allFoundFiles.Count) total files. Applying filters..." $txtLog
        
        # Directory Filter
        $filesAfterDirExclusion = $allFoundFiles | Where-Object { $file = $_; $isExcluded = $false; foreach ($excludedDir in $script:excludedDirs) { if ($file.FullName.StartsWith($excludedDir, [System.StringComparison]::InvariantCultureIgnoreCase)) { $isExcluded = $true; break } }; -not $isExcluded }
        $excludedByDirCount = $allFoundFiles.Count - $filesAfterDirExclusion.Count
        if ($excludedByDirCount -gt 0) { Log-Message "$excludedByDirCount files ignored due to directory filter." $txtLog }
        
        # Extension Filter
        Log-Message "Applying file extension filter (Mode: $($script:extensionFilterMode))." $txtLog
        if ($script:extensionFilterMode -eq "include") {
            $filesAfterExtExclusion = $filesAfterDirExclusion | Where-Object { $script:extensionFilterList.Contains($_.Extension.ToLower()) }
        } else {
            $filesAfterExtExclusion = $filesAfterDirExclusion | Where-Object { -not $script:extensionFilterList.Contains($_.Extension.ToLower()) }
        }
        $excludedByExtCount = $filesAfterDirExclusion.Count - $filesAfterExtExclusion.Count
        if ($excludedByExtCount -gt 0) { Log-Message "$excludedByExtCount files ignored due to extension filter." $txtLog }

        # Individual File Filter
        Log-Message "Applying individual file exclusion filter." $txtLog
        # Use PowerShell's -notcontains operator for case-insensitive comparison
        $filesToProcess = $filesAfterExtExclusion | Where-Object { $script:excludedFiles -notcontains $_.FullName }
        $excludedByFileCount = $filesAfterExtExclusion.Count - $filesToProcess.Count
        if ($excludedByFileCount -gt 0) { Log-Message "$excludedByFileCount files ignored due to individual file filter." $txtLog }

        if ($filesToProcess.Count -eq 0) { throw "No files left to process after applying all filters." }
        $progressBar.Maximum = $filesToProcess.Count; Log-Message "Found $($filesToProcess.Count) files to process." $txtLog
        $outputFileName = $txtOutputFile.Text.Trim(); $outputFileFullPath = Join-Path $outputDirectory $outputFileName; $maxFileSizeInBytes = ($numMaxSize.Value) * 1024
        $baseName = [System.IO.Path]::GetFileNameWithoutExtension($outputFileName); $extension = [System.IO.Path]::GetExtension($outputFileName)
        Get-ChildItem -Path $outputDirectory -Filter "$baseName*$extension" | Remove-Item -Force
        $fileIndex = 0; $currentOutputFile = $outputFileFullPath; $processedCount = 0; $skippedCount = 0; $currentSize = 0
        $streamWriter = New-Object System.IO.StreamWriter($currentOutputFile, $false, [System.Text.Encoding]::UTF8)
        Log-Message "`r`n--- Writing to: $currentOutputFile ---" $txtLog

        $sourceDirWithSlash = $sourceDirectory
        if (-not $sourceDirWithSlash.EndsWith([System.IO.Path]::DirectorySeparatorChar)) {
            $sourceDirWithSlash += [System.IO.Path]::DirectorySeparatorChar
        }

        foreach ($file in $filesToProcess) {
            if ($script:stopRequested) { Log-Message "`r`n*** USER CANCELED OPERATION ***" $txtLog; break }
            $processedCount++; $lblStatus.Text = "Processing ($processedCount / $($filesToProcess.Count)): $($file.Name)"; $progressBar.Value = $processedCount
            if ($processedCount % 10 -eq 0) { $form.Refresh() }
            if ($currentSize + $file.Length -ge $maxFileSizeInBytes -and $maxFileSizeInBytes -gt 0 -and $currentSize -ne 0) {
                $streamWriter.Close(); $streamWriter.Dispose(); $fileIndex++; $currentOutputFile = Join-Path $outputDirectory "$baseName-$fileIndex$extension"; $currentSize = 0
                $streamWriter = New-Object System.IO.StreamWriter($currentOutputFile, $false, [System.Text.Encoding]::UTF8); Log-Message "`r`n--- Splitting: new file is $currentOutputFile ---" $txtLog
            }

            $relativePath = $file.FullName.Replace($sourceDirWithSlash, "")
            $header = "`r`n========================================================================`r`nFILE: $($relativePath)`r`n========================================================================`r`n`r`n"
            
            try { 
                $fileContent = Get-Content -Path $file.FullName -Raw -Encoding utf8
                $streamWriter.Write($header)
                $streamWriter.Write($fileContent)
                $currentSize += ($header.Length + $fileContent.Length)
                $successfullyProcessedFiles.Add($relativePath)
            } catch { 
                Log-Message "`r`nERROR processing file: $($file.FullName):`r`n$($_)" $txtLog; $skippedCount++ 
            }
        }
        if (-not $script:stopRequested) { $lblStatus.Text = "Complete! Processed $($successfullyProcessedFiles.Count) files."; $btnOpenDir.Visible = $true }
        
        Log-Message "`r`n--- SUMMARY ---" $txtLog
        Log-Message "Files processed: $($successfullyProcessedFiles.Count)" $txtLog
        Log-Message "Files ignored (directory filter): $excludedByDirCount" $txtLog
        Log-Message "Files ignored (extension filter, mode '$($script:extensionFilterMode)'): $excludedByExtCount" $txtLog
        Log-Message "Files ignored (individual file filter): $excludedByFileCount" $txtLog
        Log-Message "Files skipped (read error): $skippedCount" $txtLog
        Log-Message "Output files:" $txtLog
        Get-ChildItem -Path $outputDirectory -Filter "$baseName*$extension" | ForEach-Object { Log-Message " - $($_.Name) ($([math]::Round($_.Length / 1024, 2)) KB)" $txtLog }
        
        if ($successfullyProcessedFiles.Count -gt 0) {
            Log-Message "`r`n--- LIST OF COMBINED FILES ---" $txtLog
            $successfullyProcessedFiles | ForEach-Object { Log-Message " - $_" $txtLog }
        }

    } catch { $lblStatus.Text = "Error! See log for details."; Log-Message "`r`nFATAL ERROR: $_" $txtLog }
    finally {
        if ($streamWriter -and $streamWriter.BaseStream.CanWrite) { $streamWriter.Close(); $streamWriter.Dispose() }
        $btnStart.Enabled = $true; $btnBrowse.Enabled = $true; $btnStop.Enabled = $false; $btnManageExclusions.Enabled = true
    }
})

# === Show the form ===
[void]$form.ShowDialog()