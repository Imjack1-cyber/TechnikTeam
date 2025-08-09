Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

function Show-Tree {
    param(
        [string]$Folder,
        [int]$Level,
        [System.Windows.Forms.TextBox]$OutputBox,
        [string[]]$ExcludedDirs
    )

    $items = Get-ChildItem -Path $Folder -Force | Sort-Object { !$_.PSIsContainer }, Name

    foreach ($item in $items) {
        if ($item.PSIsContainer -and $ExcludedDirs -contains $item.Name) {
            continue
        }

        $prefix = (" " * $Level * 4) + "|-- "
        $OutputBox.AppendText("$prefix$item`r`n")

        if ($item.PSIsContainer) {
            Show-Tree -Folder $item.FullName -Level ($Level + 1) -OutputBox $OutputBox -ExcludedDirs $ExcludedDirs
        }
    }
}

function Show-ExcludeDialog {
    param (
        [string]$BasePath,
        [string[]]$PreChecked
    )

    $form = New-Object Windows.Forms.Form
    $form.Text = "Select Subdirectories to Exclude"
    $form.Size = New-Object Drawing.Size(400, 400)
    $form.StartPosition = "CenterScreen"

    $checkedListBox = New-Object Windows.Forms.CheckedListBox
    $checkedListBox.Location = New-Object Drawing.Point(10, 10)
    $checkedListBox.Size = New-Object Drawing.Size(360, 300)
    $checkedListBox.CheckOnClick = $true

    $okButton = New-Object Windows.Forms.Button
    $okButton.Text = "OK"
    $okButton.Location = New-Object Drawing.Point(290, 320)
    $okButton.Size = New-Object Drawing.Size(80, 30)

    # Get subdirectories
    $subDirs = Get-ChildItem -Path $BasePath -Directory -Force | Sort-Object Name
    foreach ($dir in $subDirs) {
        $index = $checkedListBox.Items.Add($dir.Name)
        if ($PreChecked -contains $dir.Name) {
            $checkedListBox.SetItemChecked($index, $true)
        }
    }

    $okButton.Add_Click({
        $form.DialogResult = [System.Windows.Forms.DialogResult]::OK
        $form.Close()
    })

    $form.Controls.Add($checkedListBox)
    $form.Controls.Add($okButton)
    $form.Topmost = $true

    $result = $form.ShowDialog()

    $selected = @()
    if ($result -eq [System.Windows.Forms.DialogResult]::OK) {
        foreach ($item in $checkedListBox.CheckedItems) {
            $selected += $item.ToString()
        }
    }

    return $selected
}

# Main Form
$form = New-Object System.Windows.Forms.Form
$form.Text = "Project Tree Viewer"
$form.Size = New-Object System.Drawing.Size(800, 640)
$form.StartPosition = "CenterScreen"

# Buttons
$browseButton = New-Object System.Windows.Forms.Button
$browseButton.Text = "Choose Directory"
$browseButton.Location = New-Object System.Drawing.Point(10,10)
$browseButton.Size = New-Object System.Drawing.Size(120,30)

$excludeButton = New-Object System.Windows.Forms.Button
$excludeButton.Text = "Exclude Subdirs"
$excludeButton.Location = New-Object System.Drawing.Point(140,10)
$excludeButton.Size = New-Object System.Drawing.Size(120,30)

$runButton = New-Object System.Windows.Forms.Button
$runButton.Text = "Run"
$runButton.Location = New-Object System.Drawing.Point(270,10)
$runButton.Size = New-Object System.Drawing.Size(80,30)

$clearButton = New-Object System.Windows.Forms.Button
$clearButton.Text = "Clear Output"
$clearButton.Location = New-Object System.Drawing.Point(360,10)
$clearButton.Size = New-Object System.Drawing.Size(100,30)

# Path TextBox
$pathBox = New-Object System.Windows.Forms.TextBox
$pathBox.Location = New-Object System.Drawing.Point(10,50)
$pathBox.Size = New-Object System.Drawing.Size(760,20)
$pathBox.ReadOnly = $true

# Output TextBox
$outputBox = New-Object System.Windows.Forms.TextBox
$outputBox.Location = New-Object System.Drawing.Point(10,80)
$outputBox.Size = New-Object System.Drawing.Size(760,500)
$outputBox.Multiline = $true
$outputBox.ScrollBars = "Vertical"
$outputBox.ReadOnly = $true

# FolderBrowserDialog
$folderDialog = New-Object System.Windows.Forms.FolderBrowserDialog

# Global to hold path and exclusions
$global:excludedDirsMap = @{}
$global:currentPath = ""

# Browse
$browseButton.Add_Click({
    if ($folderDialog.ShowDialog() -eq "OK") {
        $pathBox.Text = $folderDialog.SelectedPath
        $global:currentPath = $pathBox.Text

        # Initialize exclusions for new path if not already
        if (-not $excludedDirsMap.ContainsKey($currentPath)) {
            $excludedDirsMap[$currentPath] = @()
        }
    }
})

# Exclude subdirs
$excludeButton.Add_Click({
    if (-not (Test-Path $currentPath)) {
        [System.Windows.Forms.MessageBox]::Show("Please select a directory first.", "Error", "OK", "Error")
        return
    }

    $preChecked = $excludedDirsMap[$currentPath]
    $selected = Show-ExcludeDialog -BasePath $currentPath -PreChecked $preChecked
    $excludedDirsMap[$currentPath] = $selected
})

# Run
$runButton.Add_Click({
    if ([string]::IsNullOrWhiteSpace($currentPath)) {
        [System.Windows.Forms.MessageBox]::Show("Please select a directory first.", "Error", "OK", "Error")
        return
    }

    $excludes = $excludedDirsMap[$currentPath]

    $outputBox.Clear()
    $outputBox.AppendText("Project Tree for: $currentPath`r`n")
    if ($excludes.Count -gt 0) {
        $outputBox.AppendText("Excluding: $($excludes -join ', ')`r`n")
    }
    $outputBox.AppendText("`r`n")

    Show-Tree -Folder $currentPath -Level 0 -OutputBox $outputBox -ExcludedDirs $excludes
})

# Clear Output
$clearButton.Add_Click({
    $outputBox.Clear()
})

# Add controls
$form.Controls.Add($browseButton)
$form.Controls.Add($excludeButton)
$form.Controls.Add($runButton)
$form.Controls.Add($clearButton)
$form.Controls.Add($pathBox)
$form.Controls.Add($outputBox)

# Show main form
[void]$form.ShowDialog()
